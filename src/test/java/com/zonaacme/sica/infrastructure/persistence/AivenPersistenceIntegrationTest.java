package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.Rol;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.infrastructure.database.DatabaseConnectionPool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales (solo lectura) contra MySQL 8.4 en Aiven Cloud.
 * No modifica ni ensucia los 20 registros base del esquema.
 */
class AivenPersistenceIntegrationTest {

    @Test
    void databaseConnectionPoolDebeReportarIsHealthyTrue() {
        DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance();
        assertTrue(pool.isHealthy(), "El pool HikariCP debe conectar exitosamente a Aiven MySQL y responder SELECT 1");
    }

    @Test
    void jdbcPersonaRepositoryDebeListarLos20PerfilesCompletosDesdeLaVista() {
        JdbcPersonaRepository repository = new JdbcPersonaRepository();
        List<PerfilCompleto> perfiles = repository.listarPerfilesCompletos();

        assertNotNull(perfiles);
        assertEquals(20, perfiles.size(), "vw_perfiles_completos debe retornar exactamente los 20 perfiles base");

        Optional<PerfilCompleto> primerPerfil = repository.buscarPerfilCompletoPorDocumento("1098000111");
        assertTrue(primerPerfil.isPresent());
        assertEquals("Sebastian Chaves", primerPerfil.get().nombrePersona());
    }

    @Test
    void jdbcVisitaRepositoryDebeLeerVistasYValidarFuncionTieneVisitaAbierta() {
        JdbcVisitaRepository repository = new JdbcVisitaRepository();

        List<AforoActual> aforoActual = repository.listarAforoActual();
        assertNotNull(aforoActual);
        assertFalse(aforoActual.isEmpty(), "vw_aforo_actual debe retornar las personas actualmente Dentro");

        List<VisitaPendiente> visitasPendientes = repository.listarVisitasPendientes();
        assertNotNull(visitasPendientes);
        assertFalse(visitasPendientes.isEmpty(), "vw_visitas_pendientes debe retornar las solicitudes pendientes");

        assertTrue(repository.tieneVisitaAbierta(2), "fn_tiene_visita_abierta(2) debe retornar true para Alberto Ruiz");
    }

    @Test
    void jdbcTokenAccesoRepositoryDebeValidarEstadoTokenMedianteFuncionSql() {
        JdbcTokenAccesoRepository repository = new JdbcTokenAccesoRepository();
        String estado = repository.validarEstadoToken("159263");
        assertEquals("VALIDO", estado, "fn_validar_estado_token('159263') debe retornar 'VALIDO'");
    }

    @Test
    void jdbcUsuarioRepositoryDebeCargarAdminConRolSuperusuarioYPermisos() {
        JdbcUsuarioRepository repository = new JdbcUsuarioRepository();
        Optional<Usuario> optUsuario = repository.buscarPorEmail("admin@zonaacme.com");

        assertTrue(optUsuario.isPresent(), "Debe existir el usuario admin@zonaacme.com");
        Usuario admin = optUsuario.get();
        assertEquals("Johan Sarmiento", admin.getNombre());
        assertTrue(admin.isEstaActivo());

        List<Rol> roles = admin.getRoles();
        assertNotNull(roles);
        assertFalse(roles.isEmpty(), "El usuario admin debe tener al menos un rol asignado");

        Rol rolSuperusuario = roles.get(0);
        assertEquals("Superusuario", rolSuperusuario.getNombreRol());
        assertNotNull(rolSuperusuario.getPermisos());
        assertFalse(rolSuperusuario.getPermisos().isEmpty(), "El rol Superusuario debe tener sus permisos cargados");
    }
}
