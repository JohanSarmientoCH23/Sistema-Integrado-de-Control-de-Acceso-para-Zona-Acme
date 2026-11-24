package com.zonaacme.sica.application.usecase.visita.strategy;

import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.Persona;
import com.zonaacme.sica.domain.repository.PersonaRepository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Estrategia de validación biométrica (Biometrico_Camara).
 * Consulta PersonaRepository.listarConBiometria(), parsea los vectores numéricos JSON y calcula
 * la similitud coseno usando Java Stream API. Autoriza si supera el umbral configurable (0.85 por defecto)
 * y el estado de acceso del perfil no está prohibido/inactivo.
 */
public class ValidacionBiometricaStrategy implements EstrategiaValidacionAcceso {

    public static final double DEFAULT_UMBRAL_SIMILITUD = 0.85;

    private final PersonaRepository personaRepository;
    private final double umbralSimilitud;

    public ValidacionBiometricaStrategy(PersonaRepository personaRepository) {
        this(personaRepository, DEFAULT_UMBRAL_SIMILITUD);
    }

    public ValidacionBiometricaStrategy(PersonaRepository personaRepository, double umbralSimilitud) {
        this.personaRepository = Objects.requireNonNull(personaRepository, "personaRepository no puede ser nulo");
        if (umbralSimilitud <= 0.0 || umbralSimilitud > 1.0) {
            throw new IllegalArgumentException("El umbral de similitud debe estar en el rango (0.0, 1.0].");
        }
        this.umbralSimilitud = umbralSimilitud;
    }

    @Override
    public ResultadoValidacionAcceso validar(SolicitudAcceso solicitud) {
        if (solicitud == null || solicitud.vectorBiometricoJson() == null || solicitud.vectorBiometricoJson().isBlank()) {
            return new ResultadoValidacionAcceso(false, "El vector biométrico facial es obligatorio.", null, null, null);
        }

        double[] vectorCapturado;
        try {
            vectorCapturado = parsearVectorJson(solicitud.vectorBiometricoJson());
        } catch (IllegalArgumentException e) {
            return new ResultadoValidacionAcceso(false, "Formato de vector biométrico inválido: " + e.getMessage(), null, null, null);
        }

        List<Persona> candidatos = personaRepository.listarConBiometria();
        if (candidatos == null || candidatos.isEmpty()) {
            return new ResultadoValidacionAcceso(false, "No hay registros biométricos en el sistema.", null, null, null);
        }

        record Coincidencia(Persona persona, double similitud) {}

        Optional<Coincidencia> mejorCoincidencia = candidatos.stream()
                .filter(p -> p.getVectorBiometrico() != null && !p.getVectorBiometrico().isBlank())
                .map(persona -> {
                    try {
                        double[] vectorBase = parsearVectorJson(persona.getVectorBiometrico());
                        if (vectorBase.length != vectorCapturado.length) {
                            return new Coincidencia(persona, -1.0);
                        }
                        double sim = calcularSimilitudCoseno(vectorCapturado, vectorBase);
                        return new Coincidencia(persona, sim);
                    } catch (IllegalArgumentException ex) {
                        return new Coincidencia(persona, -1.0);
                    }
                })
                .filter(c -> c.similitud() >= umbralSimilitud)
                .max(Comparator.comparingDouble(Coincidencia::similitud));

        if (mejorCoincidencia.isEmpty()) {
            return new ResultadoValidacionAcceso(
                    false,
                    "No se encontró coincidencia biométrica superior al umbral (" + umbralSimilitud + ").",
                    null,
                    null,
                    null
            );
        }

        Persona personaIdentificada = mejorCoincidencia.get().persona();
        double similitudLograda = mejorCoincidencia.get().similitud();

        Optional<PerfilCompleto> optPerfil = personaRepository.buscarPerfilCompletoPorDocumento(personaIdentificada.getDocumentoIdentidad());
        if (optPerfil.isEmpty()) {
            optPerfil = personaRepository.listarPerfilesCompletos().stream()
                    .filter(p -> p.personaId() == personaIdentificada.getId())
                    .findFirst();
        }

        if (optPerfil.isEmpty()) {
            return new ResultadoValidacionAcceso(
                    false,
                    "Persona identificada biométricamente pero sin perfil activo configurado.",
                    personaIdentificada.getId(),
                    null,
                    null
            );
        }

        PerfilCompleto perfil = optPerfil.get();
        if (!ValidacionPorDocumentoStrategy.esEstadoAccesoPermitido(perfil.estadoAcceso())) {
            return new ResultadoValidacionAcceso(
                    false,
                    "Acceso biométrico denegado para " + perfil.nombrePersona() + ". Estado: " + perfil.estadoAcceso(),
                    perfil.personaId(),
                    perfil,
                    null
            );
        }

        double porcentaje = Math.round(similitudLograda * 1000.0) / 10.0;
        return new ResultadoValidacionAcceso(
                true,
                "Acceso biométrico autorizado para " + perfil.nombrePersona() + " (coincidencia: " + porcentaje + "%)",
                perfil.personaId(),
                perfil,
                null
        );
    }

    public static double[] parsearVectorJson(String json) {
        String limpio = json.trim();
        if (limpio.startsWith("[") && limpio.endsWith("]")) {
            limpio = limpio.substring(1, limpio.length() - 1).trim();
        }
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El vector biométrico está vacío.");
        }
        double[] valores = Arrays.stream(limpio.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToDouble(Double::parseDouble)
                .toArray();
        if (valores.length == 0) {
            throw new IllegalArgumentException("El vector biométrico no contiene elementos numéricos.");
        }
        return valores;
    }

    public static double calcularSimilitudCoseno(double[] v1, double[] v2) {
        if (v1.length != v2.length || v1.length == 0) {
            throw new IllegalArgumentException("Los vectores deben tener la misma dimensión mayor a cero.");
        }
        double productoPunto = IntStream.range(0, v1.length)
                .mapToDouble(i -> v1[i] * v2[i])
                .sum();
        double norma1 = Math.sqrt(Arrays.stream(v1).map(x -> x * x).sum());
        double norma2 = Math.sqrt(Arrays.stream(v2).map(x -> x * x).sum());
        if (norma1 == 0.0 || norma2 == 0.0) {
            return 0.0;
        }
        return productoPunto / (norma1 * norma2);
    }
}
