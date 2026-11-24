package com.zonaacme.sica.application.port.out;

import com.zonaacme.sica.application.usecase.reporte.ResumenAforo;
import com.zonaacme.sica.domain.entity.AforoActual;

import java.nio.file.Path;
import java.util.List;

/**
 * Puerto de salida para la generación de reportes PDF oficiales de evacuación y aforo en sitio.
 */
public interface GeneradorReportePdfPort {
    Path generarReporteEvacuacion(List<AforoActual> personasEnSitio, ResumenAforo resumen, Path archivoDestino);
}
