package com.zonaacme.sica.application.usecase.visita;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.application.usecase.visita.strategy.SolicitudAcceso;
import com.zonaacme.sica.application.usecase.visita.strategy.ValidacionBiometricaStrategy;
import com.zonaacme.sica.application.usecase.visita.strategy.ValidacionPorDocumentoStrategy;
import com.zonaacme.sica.application.usecase.visita.strategy.ValidacionPorTokenStrategy;
import com.zonaacme.sica.application.usecase.visita.strategy.ValidadorAccesoContext;
import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.PerfilPersona;
import com.zonaacme.sica.domain.entity.Permiso;
import com.zonaacme.sica.domain.entity.Persona;
import com.zonaacme.sica.domain.entity.Rol;
import com.zonaacme.sica.domain.entity.TokenAcceso;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.PersonaRepository;
import com.zonaacme.sica.domain.repository.TokenAccesoRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarIngresoUseCaseTest {

    private Usuario operadorGuarda;
    private Set<Integer> personasConVisitaAbierta;
    private List<Visita> visitasGuardadas;
    private List<BitacoraAuditoria> bitacorasRegistradas;
    private RegistrarIngresoUseCase registrarIngresoUseCase;

    @BeforeEach
    void setUp() {
        Permiso permisoIngreso = new Permiso(11, "registrar_visita_manual", "Permite asentar ingresos manualmente");
        Rol rolGuarda = new Rol(3, "Guarda de Entrada Principal", List.of(permisoIngreso));
        operadorGuarda = new Usuario(6, "Jorge Diaz", "jdiaz@seguridad.com", "$2a$10$hash...", true, List.of(rolGuarda));

        personasConVisitaAbierta = new HashSet<>();
        visitasGuardadas = new ArrayList<>();
        bitacorasRegistradas = new ArrayList<>();

        PerfilCompleto perfilActivo = new PerfilCompleto(
                1, "Sebastian Chaves", "1098000111", "url/foto1.jpg",
                "[-0.1, 0.5, 0.9, 0.2]", 1, "Campus Lands S.A.S.", "Trabajador", "Activo"
        );
        PerfilCompleto perfilProhibido = new PerfilCompleto(
                16, "Pablo Emilio", "1098001666", "url/foto16.jpg",
                "[0.7, -0.2, 0.3, -0.9]", 16, "DataCenters Andinos", "Trabajador", "Prohibicion Definitiva"
        );

        PersonaRepository fakePersonaRepo = new PersonaRepository() {
            @Override public Persona guardar(Persona persona, PerfilPersona perfil) { return persona; }
            @Override public Optional<Persona> buscarPorDocumento(String doc) { return Optional.empty(); }
            @Override public Optional<Persona> buscarPorId(int id) { return Optional.empty(); }
            @Override public List<PerfilCompleto> listarPerfilesCompletos() { return List.of(perfilActivo, perfilProhibido); }
            @Override public Optional<PerfilCompleto> buscarPerfilCompletoPorDocumento(String doc) {
                if ("1098000111".equals(doc)) return Optional.of(perfilActivo);
                if ("1098001666".equals(doc)) return Optional.of(perfilProhibido);
                return Optional.empty();
            }
            @Override public List<Persona> listarConBiometria() { return List.of(); }
            @Override public void actualizarEstadoAcceso(int personaId, int nuevoEstadoAccesoId) {}
        };

        VisitaRepository fakeVisitaRepo = new VisitaRepository() {
            @Override
            public Visita guardar(Visita visita) {
                visita.setId(visitasGuardadas.size() + 1);
                visitasGuardadas.add(visita);
                personasConVisitaAbierta.add(visita.getPersonaId());
                return visita;
            }
            @Override public void actualizar(Visita visita) {}
            @Override public Optional<Visita> buscarPorId(int id) { return Optional.empty(); }
            @Override public boolean tieneVisitaAbierta(int personaId) { return personasConVisitaAbierta.contains(personaId); }
            @Override public void cerrarVisitaPorSistema(int visitaId) {}
            @Override public List<AforoActual> listarAforoActual() { return List.of(); }
            @Override public List<VisitaPendiente> listarVisitasPendientes() { return List.of(); }
        };

        TokenAccesoRepository fakeTokenRepo = new TokenAccesoRepository() {
            @Override public TokenAcceso guardar(TokenAcceso t) { return t; }
            @Override public Optional<TokenAcceso> buscarPorCodigo(String c) { return Optional.empty(); }
            @Override public String validarEstadoToken(String c) { return "TOKEN_NO_EXISTE"; }
            @Override public void actualizarEstado(String c, int e) {}
        };

        BitacoraRepository fakeBitacoraRepo = new BitacoraRepository() {
            @Override
            public BitacoraAuditoria registrar(BitacoraAuditoria bitacora) {
                bitacora.setId(bitacorasRegistradas.size() + 1L);
                bitacorasRegistradas.add(bitacora);
                return bitacora;
            }
            @Override
            public List<BitacoraAuditoria> listarUltimos(int limite) {
                return bitacorasRegistradas;
            }
        };

        ValidadorAccesoContext validadorContext = new ValidadorAccesoContext(
                new ValidacionPorDocumentoStrategy(fakePersonaRepo),
                new ValidacionPorTokenStrategy(fakeTokenRepo, fakeVisitaRepo, fakePersonaRepo),
                new ValidacionBiometricaStrategy(fakePersonaRepo)
        );

        registrarIngresoUseCase = new RegistrarIngresoUseCase(
                new AutorizacionService(),
                validadorContext,
                fakeVisitaRepo,
                fakeTokenRepo,
                fakeBitacoraRepo
        );
    }

    @Test
    void debeRegistrarIngresoExitosoConVehiculoYAuditoriaEnBitacora() {
        SolicitudAcceso solicitud = new SolicitudAcceso("1098000111", null, null, "Manual_Documento");
        Visita visita = registrarIngresoUseCase.ejecutar(operadorGuarda, solicitud, List.of("AAA-111"));

        assertNotNull(visita);
        assertEquals(1, visita.getId());
        assertEquals(1, visita.getPersonaId());
        assertEquals(4, visita.getEstadoVisitaId());
        assertEquals("Dentro", visita.getEstadoVisitaNombre());
        assertEquals(List.of("AAA-111"), visita.getVehiculosPlacas());

        assertEquals(1, bitacorasRegistradas.size());
        assertEquals("CHECK_IN_MANUAL", bitacorasRegistradas.get(0).getAccionRealizada());
    }

    @Test
    void debeBloquearPorAntiPassbackSiLaPersonaYaTieneVisitaAbierta() {
        personasConVisitaAbierta.add(1);
        SolicitudAcceso solicitud = new SolicitudAcceso("1098000111", null, null, "Manual_Documento");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> registrarIngresoUseCase.ejecutar(operadorGuarda, solicitud, List.of()));
        assertTrue(ex.getMessage().contains("Anti-passback"));
        assertTrue(visitasGuardadas.isEmpty());
    }

    @Test
    void debeRechazarPersonaConProhibicionYRegistrarAccesoDenegadoEnBitacora() {
        SolicitudAcceso solicitud = new SolicitudAcceso("1098001666", null, null, "Manual_Documento");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> registrarIngresoUseCase.ejecutar(operadorGuarda, solicitud, List.of()));
        assertTrue(ex.getMessage().contains("Prohibicion Definitiva"));
        assertTrue(visitasGuardadas.isEmpty());

        assertEquals(1, bitacorasRegistradas.size());
        assertEquals("ACCESO_DENEGADO", bitacorasRegistradas.get(0).getAccionRealizada());
    }
}
