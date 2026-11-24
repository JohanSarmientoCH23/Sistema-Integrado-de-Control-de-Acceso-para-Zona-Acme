package com.zonaacme.sica.application.usecase.empresa;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.ContactoEmpresa;
import com.zonaacme.sica.domain.entity.Empresa;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.EmpresaRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso para gestionar empresas del complejo Zona Acme y sus contactos corporativos en 4FN.
 */
public class GestionarEmpresaUseCase {

    public static final String PERMISO_CREAR_EMPRESA = "crear_empresa";
    public static final String PERMISO_EDITAR_EMPRESA = "editar_empresa";
    public static final String PERMISO_VER_EMPRESAS = "ver_empresas";

    private final AutorizacionService autorizacionService;
    private final EmpresaRepository empresaRepository;
    private final BitacoraRepository bitacoraRepository;

    public GestionarEmpresaUseCase(AutorizacionService autorizacionService,
                                   EmpresaRepository empresaRepository,
                                   BitacoraRepository bitacoraRepository) {
        this.autorizacionService = Objects.requireNonNull(autorizacionService, "autorizacionService no puede ser nulo");
        this.empresaRepository = Objects.requireNonNull(empresaRepository, "empresaRepository no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public List<Empresa> listarEmpresas() {
        return empresaRepository.listarTodas();
    }

    public Optional<Empresa> buscarPorId(int id) {
        return empresaRepository.buscarPorId(id);
    }

    public Empresa crearEmpresa(Usuario operador, Empresa empresa) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")) {
            autorizacionService.validarPermiso(operador, PERMISO_CREAR_EMPRESA);
        }
        if (empresa == null || empresa.getNombre() == null || empresa.getNombre().isBlank()) {
            throw new BusinessRuleException("El nombre de la empresa es obligatorio.");
        }

        Empresa guardada = empresaRepository.guardar(empresa);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada("CREACION_EMPRESA")
                .tablaAfectada("empresas")
                .registroIdAfectado(guardada.getId())
                .detallesJson("{\"nombre\": \"" + guardada.getNombre() + "\"}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return guardada;
    }

    public void actualizarEmpresa(Usuario operador, Empresa empresa) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")
                && !autorizacionService.tienePermiso(operador, PERMISO_CREAR_EMPRESA)) {
            autorizacionService.validarPermiso(operador, PERMISO_EDITAR_EMPRESA);
        }
        if (empresa == null || empresa.getId() <= 0 || empresa.getNombre() == null || empresa.getNombre().isBlank()) {
            throw new BusinessRuleException("El id y nombre de la empresa son obligatorios para actualizar.");
        }

        empresaRepository.actualizar(empresa);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada("ACTUALIZACION_EMPRESA")
                .tablaAfectada("empresas")
                .registroIdAfectado(empresa.getId())
                .detallesJson("{\"nombre\": \"" + empresa.getNombre() + "\"}")
                .build();
        bitacoraRepository.registrar(bitacora);
    }

    public ContactoEmpresa registrarContacto(Usuario operador, ContactoEmpresa contacto) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")
                && !autorizacionService.tienePermiso(operador, PERMISO_CREAR_EMPRESA)) {
            autorizacionService.validarPermiso(operador, PERMISO_EDITAR_EMPRESA);
        }
        if (contacto == null || contacto.getEmpresaId() <= 0
                || contacto.getNombreContacto() == null || contacto.getNombreContacto().isBlank()) {
            throw new BusinessRuleException("El empresaId y el nombre del contacto son obligatorios.");
        }

        return empresaRepository.guardarContacto(contacto);
    }
}
