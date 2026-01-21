package com.victorgponce.service.processor;

import com.victorgponce.model.PlayerSession;
import com.victorgponce.model.SessionSnapshot;
import com.victorgponce.utils.TelemetryUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

import java.util.ArrayList;
import java.util.Set;

public class SessionTelemetryProcessor {

    public PlayerSession createSession(ServerPlayerEntity player, String type) {
        String ip = "unknown";
        try {
            ip = player.getIp();
        } catch (Exception ignored) { }

        return new PlayerSession(
                player.getUuidAsString(),
                player.getName().getString(),
                type,
                ip,
                System.currentTimeMillis()
        );
    }

    public SessionSnapshot createSnapshot(ServerPlayerEntity player, Set<String> visitedBiomes) {
        long walked = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        long sprinted = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM));
        long flown = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.FLY_ONE_CM));
        long total = walked + sprinted + flown;

        return new SessionSnapshot(
                player.getUuidAsString(),
                walked,
                sprinted,
                flown,
                total,
                TelemetryUtils.getWorldName(player),
                new ArrayList<>(visitedBiomes),
                System.currentTimeMillis()
        );
    }
}