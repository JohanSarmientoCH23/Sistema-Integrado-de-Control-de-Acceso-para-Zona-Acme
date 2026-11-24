package com.zonaacme.sica.application.sync;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.VisitaPendiente;

import java.util.List;

/**
 * Patrón de Diseño #3 (Observer): Contrato para observadores que reaccionan
 * ante cambios en el aforo actual o en las visitas pendientes de aprobación.
 */
@FunctionalInterface
public interface AforoObserver {
    void onEstadoActualizado(List<AforoActual> aforoActual, List<VisitaPendiente> visitasPendientes);
}
