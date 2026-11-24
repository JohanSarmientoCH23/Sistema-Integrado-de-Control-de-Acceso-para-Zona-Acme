package com.zonaacme.sica.application.usecase.autorizacion;

import com.zonaacme.sica.application.exception.AccessDeniedException;
import com.zonaacme.sica.application.exception.AuthenticationException;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.Permiso;
import com.zonaacme.sica.domain.entity.Rol;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AutorizacionServiceTest {

    private final AutorizacionService autorizacionService = new AutorizacionService();

    @Test
    void debePermitirAccionCuandoUsuarioTienePermisoMedianteStreamApi() {
        Permiso permisoIngreso = new Permiso(11, "registrar_visita_manual", "Permite asentar ingresos manualmente");
        Permiso permisoIncidente = new Permiso(15, "reportar_incidente", "Permite crear un registro de incidente");
        Rol rolGuarda = new Rol(3, "Guarda de Entrada Principal", List.of(permisoIngreso, permisoIncidente));
        Usuario guarda = new Usuario(6, "Jorge Diaz", "jdiaz@seguridad.com", "$2a$10$hash...", true, List.of(rolGuarda));

        assertTrue(autorizacionService.tieneRol(guarda, "Guarda de Entrada Principal"));
        assertTrue(autorizacionService.tienePermiso(guarda, "registrar_visita_manual"));
        assertDoesNotThrow(() -> autorizacionService.validarPermiso(guarda, "registrar_visita_manual"));
    }

    @Test
    void debeLanzarAccessDeniedExceptionCuandoUsuarioNoTienePermisoOEstaInactivo() {
        Permiso permisoVer = new Permiso(4, "ver_usuarios", "Permite listar usuarios");
        Rol rolGuarda = new Rol(3, "Guarda de Entrada Principal", List.of(permisoVer));
        Usuario guarda = new Usuario(6, "Jorge Diaz", "jdiaz@seguridad.com", "$2a$10$hash...", true, List.of(rolGuarda));

        assertFalse(autorizacionService.tienePermiso(guarda, "crear_usuario"));
        assertThrows(AccessDeniedException.class, () -> autorizacionService.validarPermiso(guarda, "crear_usuario"));

        Usuario inactivo = new Usuario(8, "Pedro Ramirez", "pramirez@finanzas.com", "$2a$10$hash...", false, List.of(rolGuarda));
        assertThrows(AccessDeniedException.class, () -> autorizacionService.validarPermiso(inactivo, "ver_usuarios"));
    }

    @Test
    void autenticarUsuarioUseCaseDebeValidarFormatoPasswordDeDataSqlYRegistrarAuditoria() {
        Permiso permiso = new Permiso(1, "crear_usuario", "Permite crear nuevos usuarios");
        Rol rolSuper = new Rol(1, "Superusuario", List.of(permiso));
        Usuario admin = new Usuario(1, "Johan Sarmiento", "admin@zonaacme.com", "$2a$10$hash...", true, List.of(rolSuper));

        List<BitacoraAuditoria> bitacoras = new ArrayList<>();

        UsuarioRepository fakeUsuarioRepo = new UsuarioRepository() {
            @Override
            public Optional<Usuario> buscarPorEmail(String email) {
                return "admin@zonaacme.com".equals(email) ? Optional.of(admin) : Optional.empty();
            }
            @Override public Optional<Usuario> buscarPorId(int id) { return Optional.of(admin); }
            @Override public Usuario guardar(Usuario u) { return u; }
            @Override public void actualizar(Usuario u) {}
            @Override public List<Usuario> listarTodos() { return List.of(admin); }
            @Override public List<Rol> obtenerRolesConPermisos(int usuarioId) { return List.of(rolSuper); }
        };

        BitacoraRepository fakeBitacoraRepo = new BitacoraRepository() {
            @Override
            public BitacoraAuditoria registrar(BitacoraAuditoria bitacora) {
                bitacora.setId(1L);
                bitacoras.add(bitacora);
                return bitacora;
            }
            @Override
            public List<BitacoraAuditoria> listarUltimos(int limite) {
                return bitacoras;
            }
        };

        AutenticarUsuarioUseCase useCase = new AutenticarUsuarioUseCase(fakeUsuarioRepo, fakeBitacoraRepo);
        Usuario autenticado = useCase.autenticar("admin@zonaacme.com", "$2a$10$hash...");

        assertNotNull(autenticado);
        assertEquals("Johan Sarmiento", autenticado.getNombre());
        assertEquals(1, bitacoras.size());
        assertEquals("LOGIN_EXITOSO", bitacoras.get(0).getAccionRealizada());
        assertTrue(SesionUsuario.getInstance().haySesionActiva());

        assertThrows(AuthenticationException.class, () -> useCase.autenticar("admin@zonaacme.com", "clave_erronea"));
        SesionUsuario.getInstance().cerrarSesion();
    }
}
