package com.victorgponce.controller;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.victorgponce.service.TelemetryFacade;

public class BattleController {

    public static void register() {
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(event ->
                TelemetryFacade.getInstance().processBattleStart(event.getBattle())
        );

        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            for (BattleActor winner : event.getWinners()) {
                if (winner instanceof PlayerBattleActor p) TelemetryFacade.getInstance().processBattleEnd(event.getBattle(), p, "WIN");
            }
            for (BattleActor loser : event.getLosers()) {
                if (loser instanceof PlayerBattleActor p) TelemetryFacade.getInstance().processBattleEnd(event.getBattle(), p, "LOSS");
            }
        });

        CobblemonEvents.BATTLE_FLED.subscribe(event -> {
            if (event.getPlayer() != null) {
                var playerUuid = event.getPlayer().getUuid();
                for (BattleActor actor : event.getBattle().getActors()) {
                    if (actor instanceof PlayerBattleActor p && p.getUuid().equals(playerUuid)) {
                        TelemetryFacade.getInstance().processBattleEnd(event.getBattle(), p, "FLEE");
                        break;
                    }
                }
            }
        });
    }
}