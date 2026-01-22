package com.victorgponce.controller;

import com.victorgponce.service.TelemetryFacade;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class SessionHeartbeatController {

    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            // Optimization: Don't check every tick (lag).
            // We check every 40 ticks (~2 seconds)
            if (tickCounter % 40 == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    TelemetryFacade facade = TelemetryFacade.getInstance();

                    // Track current biome
                    facade.trackPlayerState(player);

                    // Check if its Snapshot time
                    if (com.victorgponce.repository.DataRepository.getInstance().shouldTriggerSnapshot(player.getUuid())) {
                        facade.processSnapshot(player);
                    }

                    facade.scanInventoryForEggs(player);
                }
            }
        });
    }
}