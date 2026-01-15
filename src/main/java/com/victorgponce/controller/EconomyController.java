package com.victorgponce.controller;

import com.victorgponce.service.TelemetryFacade;
import org.pokesplash.gts.api.event.GtsEvents;

import static com.victorgponce.NeuralNetworkData.LOGGER;

public class EconomyController {

    public static void register() {
        GtsEvents.PURCHASE.subscribe(event -> {
            TelemetryFacade.getInstance().processGtsTransaction(event);
        });

        LOGGER.info("CobbleAnalytics: EconomyController hookeado a GTS correctamente.");
    }
}