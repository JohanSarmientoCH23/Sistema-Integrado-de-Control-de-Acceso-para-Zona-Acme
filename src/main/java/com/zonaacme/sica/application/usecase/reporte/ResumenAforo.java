package com.zonaacme.sica.application.usecase.reporte;

import java.util.Map;

/**
 * Record de resumen estadístico de aforo en tiempo real para gráficos JavaFX y reporte de evacuación.
 */
public record ResumenAforo(
        int totalEnSitio,
        Map<String, Long> conteoPorEmpresa,
        Map<String, Long> conteoPorTipoPersona,
        Map<String, Long> conteoPorMetodoIngreso,
        int totalPendientes
) {
}
