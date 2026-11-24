package com.zonaacme.sica.application.usecase.visita.strategy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Contexto del Patrón Strategy que selecciona dinámicamente la estrategia de validación
 * según el método de ingreso (Manual_Documento, Biometrico_Camara, Tarjeta_RFID, Escaneo_QR, PIN_Digitado).
 */
public class ValidadorAccesoContext {

    public static final int METODO_MANUAL_DOCUMENTO_ID = 1;
    public static final int METODO_BIOMETRICO_CAMARA_ID = 2;
    public static final int METODO_TARJETA_RFID_ID = 3;
    public static final int METODO_ESCANEO_QR_ID = 4;
    public static final int METODO_PIN_DIGITADO_ID = 5;

    private final Map<String, EstrategiaValidacionAcceso> estrategias = new HashMap<>();

    public ValidadorAccesoContext(EstrategiaValidacionAcceso porDocumento,
                                  EstrategiaValidacionAcceso porToken,
                                  EstrategiaValidacionAcceso biometrica) {
        Objects.requireNonNull(porDocumento, "Estrategia por documento no puede ser nula");
        Objects.requireNonNull(porToken, "Estrategia por token no puede ser nula");
        Objects.requireNonNull(biometrica, "Estrategia biométrica no puede ser nula");

        // Claves alineadas a metodos_acceso en data.sql y alias comunes
        registrarEstrategia("MANUAL_DOCUMENTO", porDocumento);
        registrarEstrategia("DOCUMENTO", porDocumento);
        registrarEstrategia("1", porDocumento);

        registrarEstrategia("BIOMETRICO_CAMARA", biometrica);
        registrarEstrategia("BIOMETRIA", biometrica);
        registrarEstrategia("2", biometrica);

        registrarEstrategia("TARJETA_RFID", porToken);
        registrarEstrategia("ESCANEO_QR", porToken);
        registrarEstrategia("PIN_DIGITADO", porToken);
        registrarEstrategia("TOKEN", porToken);
        registrarEstrategia("3", porToken);
        registrarEstrategia("4", porToken);
        registrarEstrategia("5", porToken);
    }

    public void registrarEstrategia(String claveMetodo, EstrategiaValidacionAcceso estrategia) {
        if (claveMetodo != null && !claveMetodo.isBlank() && estrategia != null) {
            estrategias.put(claveMetodo.trim().toUpperCase(Locale.ROOT), estrategia);
        }
    }

    public ResultadoValidacionAcceso validar(SolicitudAcceso solicitud) {
        if (solicitud == null || solicitud.tipoMetodo() == null || solicitud.tipoMetodo().isBlank()) {
            throw new IllegalArgumentException("Debe especificar el método de ingreso en la solicitud de acceso.");
        }
        String clave = solicitud.tipoMetodo().trim().toUpperCase(Locale.ROOT);
        EstrategiaValidacionAcceso estrategia = estrategias.get(clave);
        if (estrategia == null) {
            throw new IllegalArgumentException("No existe una estrategia configurada para el método de ingreso: " + solicitud.tipoMetodo());
        }
        return estrategia.validar(solicitud);
    }

    public static int resolverMetodoAccesoId(String tipoMetodo) {
        if (tipoMetodo == null || tipoMetodo.isBlank()) {
            return METODO_MANUAL_DOCUMENTO_ID;
        }
        return switch (tipoMetodo.trim().toUpperCase(Locale.ROOT)) {
            case "1", "MANUAL_DOCUMENTO", "DOCUMENTO" -> METODO_MANUAL_DOCUMENTO_ID;
            case "2", "BIOMETRICO_CAMARA", "BIOMETRIA" -> METODO_BIOMETRICO_CAMARA_ID;
            case "3", "TARJETA_RFID" -> METODO_TARJETA_RFID_ID;
            case "4", "ESCANEO_QR" -> METODO_ESCANEO_QR_ID;
            case "5", "PIN_DIGITADO", "TOKEN" -> METODO_PIN_DIGITADO_ID;
            default -> METODO_MANUAL_DOCUMENTO_ID;
        };
    }
}
