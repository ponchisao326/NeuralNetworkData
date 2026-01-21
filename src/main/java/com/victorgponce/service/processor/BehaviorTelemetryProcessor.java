package com.victorgponce.service.processor;

import com.victorgponce.model.CommandUsage;
import com.victorgponce.model.PlayerDeath;
import com.victorgponce.utils.TelemetryUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BehaviorTelemetryProcessor {

    public CommandUsage createCommandUsage(ServerPlayerEntity player, String command, boolean success) {
        String rootCommand = command.split(" ")[0];

        return new CommandUsage(
                player.getUuidAsString(),
                TelemetryUtils.getBiomeName(player),
                TelemetryUtils.getWorldName(player),
                rootCommand,
                success,
                System.currentTimeMillis()
        );
    }

    public PlayerDeath createDeath(ServerPlayerEntity player, DamageSource source) {
        return new PlayerDeath(
                player.getUuidAsString(),
                source.getName(),
                player.experienceLevel,
                TelemetryUtils.getWorldName(player),
                TelemetryUtils.getBiomeName(player),
                System.currentTimeMillis()
        );
    }
}