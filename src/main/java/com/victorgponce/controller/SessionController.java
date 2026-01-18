package com.victorgponce.controller;

import com.victorgponce.service.TelemetryFacade;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class SessionController {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            TelemetryFacade.getInstance().processSession(handler.getPlayer(), "LOGIN");
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // Generate last snapshot
            TelemetryFacade.getInstance().processSnapshot(handler.getPlayer());

            // Register LogOut Event
            TelemetryFacade.getInstance().processSession(handler.getPlayer(), "LOGOUT");

            // Clean tracker memory
            TelemetryFacade.getInstance().cleanupPlayer(handler.getPlayer());
        });
    }
}