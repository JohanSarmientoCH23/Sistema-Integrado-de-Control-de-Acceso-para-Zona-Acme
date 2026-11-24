package com.zonaacme.sica.application.usecase.reporte;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.PerfilPersona;
import com.zonaacme.sica.domain.entity.Persona;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.PersonaRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GenerarEstadisticasAforoUseCaseTest {

    @Test
    void debeAgruparEstadisticasDeAforoConStreamsCorrectamente() {
        LocalDateTime ahora = LocalDateTime.now();
        List<AforoActual> aforoFake = List.of(
                new AforoActual(2, "Alberto Ruiz", "1098000222", "Avicola ERP Solutions", "Trabajador", ahora, "Biometrico_Camara"),
                new AforoActual(4, "Daniela Vargas", "1098000444", "Logistica Acme", "Invitado", ahora, "PIN_Digitado"),
                new AforoActual(6, "Fabian Nunez", "1098000666", "Avicola ERP Solutions", "Trabajador", ahora, "Biometrico_Camara")
        );

        List<VisitaPendiente> pendientesFake = List.of(
                new VisitaPendiente(10, "Julio Cesar", "1098001000", "Seguridad Inteligente", ahora)
        );

        VisitaRepository fakeVisitaRepo = new VisitaRepository() {
            @Override public Visita guardar(Visita v) { return v; }
            @Override public void actualizar(Visita v) {}
            @Override public Optional<Visita> buscarPorId(int id) { return Optional.empty(); }
            @Override public boolean tieneVisitaAbierta(int personaId) { return false; }
            @Override public void cerrarVisitaPorSistema(int visitaId) {}
            @Override public List<AforoActual> listarAforoActual() { return aforoFake; }
            @Override public List<VisitaPendiente> listarVisitasPendientes() { return pendientesFake; }
        };

        PersonaRepository fakePersonaRepo = new PersonaRepository() {
            @Override public Persona guardar(Persona p, PerfilPersona pp) { return p; }
            @Override public Optional<Persona> buscarPorDocumento(String d) { return Optional.empty(); }
            @Override public Optional<Persona> buscarPorId(int id) { return Optional.empty(); }
            @Override public List<PerfilCompleto> listarPerfilesCompletos() {
                return List.of(
                        new PerfilCompleto(1, "Sebastian Chaves", "1098000111", "", "[]", 1, "Campus Lands S.A.S.", "Trabajador", "Activo"),
                        new PerfilCompleto(16, "Pablo Emilio", "1098001666", "", "[]", 16, "DataCenters Andinos", "Trabajador", "Prohibicion Definitiva")
                );
            }
            @Override public Optional<PerfilCompleto> buscarPerfilCompletoPorDocumento(String d) { return Optional.empty(); }
            @Override public List<Persona> listarConBiometria() { return List.of(); }
            @Override public void actualizarEstadoAcceso(int p, int e) {}
        };

        GenerarEstadisticasAforoUseCase useCase = new GenerarEstadisticasAforoUseCase(fakeVisitaRepo, fakePersonaRepo);
        ResumenAforo resumen = useCase.generarResumen();

        assertEquals(3, resumen.totalEnSitio());
        assertEquals(1, resumen.totalPendientes());
        assertEquals(2L, resumen.conteoPorEmpresa().get("Avicola ERP Solutions"));
        assertEquals(1L, resumen.conteoPorEmpresa().get("Logistica Acme"));
        assertEquals(2L, resumen.conteoPorTipoPersona().get("Trabajador"));
        assertEquals(1L, resumen.conteoPorTipoPersona().get("Invitado"));
        assertEquals(2L, resumen.conteoPorMetodoIngreso().get("Biometrico_Camara"));
        assertEquals(1L, resumen.conteoPorMetodoIngreso().get("PIN_Digitado"));

        Map<String, Long> porEstado = useCase.contarPerfilesPorEstadoAcceso();
        assertEquals(1L, porEstado.get("Activo"));
        assertEquals(1L, porEstado.get("Prohibicion Definitiva"));
    }
}
