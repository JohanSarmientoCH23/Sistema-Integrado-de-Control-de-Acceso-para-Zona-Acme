package com.zonaacme.sica.application.usecase.reporte;

import com.zonaacme.sica.application.port.out.GeneradorReportePdfPort;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para exportar el Reporte Oficial de Evacuación y Aforo en Sitio en formato PDF,
 * validando permiso RBAC y auditando en la bitácora del sistema.
 */
public class ExportarReporteEvacuacionUseCase {

    public static final String PERMISO_GENERAR_REPORTE_VISITAS = "generar_reporte_visitas";

    private final AutorizacionService autorizacionService;
    private final VisitaRepository visitaRepository;
    private final GenerarEstadisticasAforoUseCase estadisticasAforoUseCase;
    private final GeneradorReportePdfPort generadorReportePdfPort;
    private final BitacoraRepository bitacoraRepository;

    public ExportarReporteEvacuacionUseCase(AutorizacionService autorizacionService,
                                            VisitaRepository visitaRepository,
                                            GenerarEstadisticasAforoUseCase estadisticasAforoUseCase,
                                            GeneradorReportePdfPort generadorReportePdfPort,
                                            BitacoraRepository bitacoraRepository) {
        this.autorizacionService = Objects.requireNonNull(autorizacionService, "autorizacionService no puede ser nulo");
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
        this.estadisticasAforoUseCase = Objects.requireNonNull(estadisticasAforoUseCase, "estadisticasAforoUseCase no puede ser nulo");
        this.generadorReportePdfPort = Objects.requireNonNull(generadorReportePdfPort, "generadorReportePdfPort no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public Path ejecutar(Usuario operador, Path archivoDestino) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")) {
            autorizacionService.validarPermiso(operador, PERMISO_GENERAR_REPORTE_VISITAS);
        }

        List<AforoActual> personasEnSitio = visitaRepository.listarAforoActual();
        ResumenAforo resumen = estadisticasAforoUseCase.generarResumen();

        Path generado = generadorReportePdfPort.generarReporteEvacuacion(personasEnSitio, resumen, archivoDestino);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada("EXPORTACION_REPORTE_EVACUACION")
                .tablaAfectada("visitas")
                .registroIdAfectado(null)
                .detallesJson("{\"totalEnSitio\": " + resumen.totalEnSitio() + "}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return generado;
    }
}
