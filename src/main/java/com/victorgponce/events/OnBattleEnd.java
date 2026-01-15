package com.victorgponce.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.cache.PokemonData;
import com.victorgponce.data_objects.BattleResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.victorgponce.cache.PokemonData.battleResultsBuffer;

public class OnBattleEnd {

    // --- VICTORY/DEFEAT MANAGER ---
    public static void onVictory(BattleVictoryEvent event) {
        PokemonBattle battle = event.getBattle();

        // Event give us winner and losers Lists (Actors)
        // Search for real players involved
        for (BattleActor winner : event.getWinners()) {
            if (winner instanceof PlayerBattleActor playerActor) {
                // Player won
                processBattleEnd(battle, playerActor, "WIN");
            }
        }

        for (BattleActor loser : event.getLosers()) {
            if (loser instanceof PlayerBattleActor playerActor) {
                // Player lost
                processBattleEnd(battle, playerActor, "LOSS");
            }
        }
    }

    // --- MANEJADOR DE HUIDA ---
    // --- FLEE Manager ---
    public static void onFlee(BattleFledEvent event) {
        PokemonBattle battle = event.getBattle();

        // Verify if the player is the one fleeing
        if (event.getPlayer() != null) {
            UUID playerUuid = event.getPlayer().getUuid();

            for (BattleActor actor : battle.getActors()) {
                // Is a player type actor?
                if (actor instanceof PlayerBattleActor) {
                    PlayerBattleActor playerActor = (PlayerBattleActor) actor;

                    // The UUID matchs with the once who fleed?
                    if (playerActor.getUuid().equals(playerUuid)) {
                        processBattleEnd(battle, playerActor, "FLEE");
                        break; // Found, no need to continue
                    }
                }
            }
        }
    }

    // --- COMMON DATA EXTRACTION LOGIC ---
    private static void processBattleEnd(PokemonBattle battle, PlayerBattleActor playerActor, String result) {
        long currentTimestamp = System.currentTimeMillis();
        ServerPlayerEntity playerEntity = playerActor.getEntity();

        if (playerEntity == null) return;

        // Calculate duration
        UUID battleId = battle.getBattleId();
        Long startTimestamp = PokemonData.battleStartTimestamps.remove(battleId); // .remove to clean memory

        long durationMs;
        if (startTimestamp != null) {
            // Real time = End Time - Start Time
            durationMs = currentTimestamp - startTimestamp;
        } else {
            // Rescue Fallback (Server restart middle match)
            durationMs = battle.getTime() * 50L;
        }

        // Analyze Opponent
        String opponentType = "UNKNOWN";

        // Iterate over all actors in the battle
        for (BattleActor actor : battle.getActors()) {
            // If the actor is not in the same side as the player, it's an opponent
            if (actor.getSide() != playerActor.getSide()) {
                opponentType = actor.getType().name(); // WILD, PLAYER, NPC...
                break;
            }
        }

        // Analyze Team (Fainted Count & Implicit MVP)
        int faintedCount = 0;
        List<String> teamSnapshot = new ArrayList<>();

        for (BattlePokemon p : playerActor.getPokemonList()) {
            if (p.getHealth() <= 0) {
                faintedCount++;
            }

            double hpPercent = (double) p.getHealth() / p.getMaxHealth() * 100;
            teamSnapshot.add(p.getEffectedPokemon().getSpecies().getName() + String.format(":%.1f%%", hpPercent));
        }

        // Obtain Biome
        String biome = playerEntity.getWorld()
                .getBiome(playerEntity.getBlockPos())
                .getKey()
                .map(k -> k.getValue().toString())
                .orElse("unknown");

        // Create DTO
        BattleResult data = new BattleResult(
                battle.getBattleId().toString(),
                playerEntity.getUuidAsString(),
                result,
                opponentType,
                durationMs,
                faintedCount,
                teamSnapshot.toString(),
                biome,
                currentTimestamp
        );

        battleResultsBuffer.add(data);

        System.out.println("BigData Battle: " + result + " vs " + opponentType + " (Duration: " + (durationMs/1000) + "s)");
    }
}