package com.victorgponce.events;

import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.victorgponce.cache.PokemonData;

public class OnBattleStarted {

    public static void onBattleStarted(BattleStartedEvent event) {
        // Save the exact start time associated with the battle ID
        PokemonData.battleStartTimestamps.put(
                event.getBattle().getBattleId(),
                System.currentTimeMillis()
        );
    }
}