package com.victorgponce.events;

import com.victorgponce.cache.PokemonData;
import com.victorgponce.data_objects.PlayerSession;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerLogin {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            recordSessionEvent(handler.getPlayer(), "LOGIN");
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            recordSessionEvent(handler.getPlayer(), "LOGOUT");
        });
    }

    private static void recordSessionEvent(ServerPlayerEntity player, String type) {
        long now = System.currentTimeMillis();
        String ip = "unknown";
        try {
            ip = player.getIp();
        } catch (Exception e) {
            // Singleplayer ignore
        }

        PlayerSession session = new PlayerSession(
                player.getUuidAsString(),
                player.getName().getString(),
                type,
                ip,
                now
        );

        PokemonData.sessionBuffer.add(session);
    }
}