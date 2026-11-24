package com.zonaacme.sica.application.usecase.visita.strategy;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.PerfilPersona;
import com.zonaacme.sica.domain.entity.Persona;
import com.zonaacme.sica.domain.entity.TokenAcceso;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.PersonaRepository;
import com.zonaacme.sica.domain.repository.TokenAccesoRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EstrategiaValidacionAccesoTest {

    private PersonaRepository fakePersonaRepo;
    private TokenAccesoRepository fakeTokenRepo;
    private VisitaRepository fakeVisitaRepo;
    private ValidadorAccesoContext validadorContext;

    private final PerfilCompleto perfilPermitido = new PerfilCompleto(
            1, "Sebastian Chaves", "1098000111", "url/foto1.jpg",
            "[-0.1, 0.5, 0.9, 0.2]", 1, "Campus Lands S.A.S.", "Trabajador", "Activo"
    );

    private final PerfilCompleto perfilProhibido = new PerfilCompleto(
            16, "Pablo Emilio", "1098001666", "url/foto16.jpg",
            "[0.7, -0.2, 0.3, -0.9]", 16, "DataCenters Andinos", "Trabajador", "Prohibicion Definitiva"
    );

    @BeforeEach
    void setUp() {
        List<PerfilCompleto> perfiles = List.of(perfilPermitido, perfilProhibido);
        List<Persona> personasConBio = List.of(
                new Persona(1, "Sebastian Chaves", "1098000111", "url/foto1.jpg", "[-0.1, 0.5, 0.9, 0.2]"),
                new Persona(16, "Pablo Emilio", "1098001666", "url/foto16.jpg", "[0.7, -0.2, 0.3, -0.9]")
        );

        fakePersonaRepo = new PersonaRepository() {
            @Override public Persona guardar(Persona persona, PerfilPersona perfil) { return persona; }
            @Override public Optional<Persona> buscarPorDocumento(String doc) {
                return personasConBio.stream().filter(p -> p.getDocumentoIdentidad().equals(doc)).findFirst();
            }
            @Override public Optional<Persona> buscarPorId(int id) {
                return personasConBio.stream().filter(p -> p.getId() == id).findFirst();
            }
            @Override public List<PerfilCompleto> listarPerfilesCompletos() { return perfiles; }
            @Override public Optional<PerfilCompleto> buscarPerfilCompletoPorDocumento(String doc) {
                return perfiles.stream().filter(p -> p.documentoIdentidad().equals(doc)).findFirst();
            }
            @Override public List<Persona> listarConBiometria() { return personasConBio; }
            @Override public void actualizarEstadoAcceso(int personaId, int nuevoEstadoAccesoId) {}
        };

        fakeVisitaRepo = new VisitaRepository() {
            @Override public Visita guardar(Visita v) { return v; }
            @Override public void actualizar(Visita v) {}
            @Override public Optional<Visita> buscarPorId(int id) {
                if (id == 4) {
                    return Optional.of(Visita.builder()
                            .id(4)
                            .personaId(1)
                            .estadoVisitaId(1)
                            .metodoIngresoId(5)
                            .build());
                }
                return Optional.empty();
            }
            @Override public boolean tieneVisitaAbierta(int personaId) { return false; }
            @Override public void cerrarVisitaPorSistema(int visitaId) {}
            @Override public List<AforoActual> listarAforoActual() { return List.of(); }
            @Override public List<VisitaPendiente> listarVisitasPendientes() { return List.of(); }
        };

        fakeTokenRepo = new TokenAccesoRepository() {
            @Override public TokenAcceso guardar(TokenAcceso t) { return t; }
            @Override public Optional<TokenAcceso> buscarPorCodigo(String codigo) {
                if ("159263".equals(codigo)) {
                    return Optional.of(new TokenAcceso(4L, 4, "159263", 1, 1,
                            LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusHours(2)));
                }
                return Optional.empty();
            }
            @Override public String validarEstadoToken(String codigo) {
                if ("159263".equals(codigo)) return "VALIDO";
                if ("384756".equals(codigo)) return "TOKEN_EXPIRADO";
                return "TOKEN_NO_EXISTE";
            }
            @Override public void actualizarEstado(String codigo, int nuevoEstadoId) {}
        };

        validadorContext = new ValidadorAccesoContext(
                new ValidacionPorDocumentoStrategy(fakePersonaRepo),
                new ValidacionPorTokenStrategy(fakeTokenRepo, fakeVisitaRepo, fakePersonaRepo),
                new ValidacionBiometricaStrategy(fakePersonaRepo, 0.85)
        );
    }

    @Test
    void estrategiaPorDocumentoDebeAutorizarPersonaActivaYRechazarPersonaConProhibicion() {
        SolicitudAcceso solPermitida = new SolicitudAcceso("1098000111", null, null, "Manual_Documento");
        ResultadoValidacionAcceso resPermitido = validadorContext.validar(solPermitida);
        assertTrue(resPermitido.autorizado());
        assertEquals(1, resPermitido.personaId());

        SolicitudAcceso solProhibida = new SolicitudAcceso("1098001666", null, null, "Manual_Documento");
        ResultadoValidacionAcceso resProhibido = validadorContext.validar(solProhibida);
        assertFalse(resProhibido.autorizado());
        assertTrue(resProhibido.mensaje().contains("Prohibicion Definitiva"));
    }

    @Test
    void estrategiaPorTokenDebeAutorizarTokenValidoYRechazarTokenExpirado() {
        SolicitudAcceso solTokenValido = new SolicitudAcceso(null, "159263", null, "PIN_Digitado");
        ResultadoValidacionAcceso resValido = validadorContext.validar(solTokenValido);
        assertTrue(resValido.autorizado());
        assertEquals(1, resValido.personaId());

        SolicitudAcceso solTokenExpirado = new SolicitudAcceso(null, "384756", null, "PIN_Digitado");
        ResultadoValidacionAcceso resExpirado = validadorContext.validar(solTokenExpirado);
        assertFalse(resExpirado.autorizado());
        assertTrue(resExpirado.mensaje().contains("TOKEN_EXPIRADO"));
    }

    @Test
    void estrategiaBiometricaDebeAutorizarVectorCoincidenteYRechazarVectorDistinto() {
        SolicitudAcceso solBioCoincidente = new SolicitudAcceso(null, null, "[-0.11, 0.49, 0.91, 0.19]", "Biometrico_Camara");
        ResultadoValidacionAcceso resBioOk = validadorContext.validar(solBioCoincidente);
        assertTrue(resBioOk.autorizado());
        assertEquals(1, resBioOk.personaId());

        SolicitudAcceso solBioNoCoincidente = new SolicitudAcceso(null, null, "[-0.9, -0.8, -0.7, -0.5]", "Biometrico_Camara");
        ResultadoValidacionAcceso resBioFail = validadorContext.validar(solBioNoCoincidente);
        assertFalse(resBioFail.autorizado());
    }
}
