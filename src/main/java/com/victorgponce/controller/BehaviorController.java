package com.victorgponce.controller;

import com.victorgponce.service.TelemetryFacade;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class BehaviorController {

    public static void register() {
        // Death Listener
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity player) {
                TelemetryFacade.getInstance().processDeath(player, source);
            }
        });
    }
}