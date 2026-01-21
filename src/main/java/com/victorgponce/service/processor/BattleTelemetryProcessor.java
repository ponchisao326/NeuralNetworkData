package com.victorgponce.service.processor;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.model.BattleResult;
import com.victorgponce.model.RaidMetadata;
import com.victorgponce.utils.TelemetryUtils;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.victorgponce.NeuralNetworkData.LOGGER;

public class BattleTelemetryProcessor {

    // DTO simple para devolver múltiples datos al Facade
    public record BattleStartContext(UUID battleId, long timestamp, RaidMetadata raidMetadata) {}

    public BattleStartContext analyzeBattleStart(PokemonBattle battle) {
        UUID battleId = battle.getBattleId();
        long now = System.currentTimeMillis();
        RaidMetadata metadata = null;

        // Raid detection logic
        for (BattleActor actor : battle.getActors()) {
            if (actor instanceof PokemonBattleActor pokemonActor) {
                Entity entity = pokemonActor.getEntity();
                Pokemon pokemon = pokemonActor.getEntity().getPokemon();
                if (pokemon == null) continue;

                boolean isRaid = false;
                String raidTier = "UNKNOWN";

                if (pokemon.getAspects().contains("raid")) isRaid = true;

                if (!isRaid && entity != null) {
                    for (String tag : entity.getCommandTags()) {
                        if (tag.contains("raid") || tag.contains("boss")) {
                            isRaid = true;
                            if (tag.contains("tier")) raidTier = tag;
                        }
                    }
                }

                if (isRaid) {
                    String species = pokemon.getSpecies().getName();
                    UUID trackingUuid = (entity != null) ? entity.getUuid() : pokemon.getUuid();
                    metadata = new RaidMetadata(species, raidTier, trackingUuid);
                    break;
                }
            }
        }
        return new BattleStartContext(battleId, now, metadata);
    }

    public BattleResult analyzeBattleEnd(PokemonBattle battle, PlayerBattleActor playerActor, String result, Long startTimestamp) {
        if (playerActor.getEntity() == null) return null;

        long currentTimestamp = System.currentTimeMillis();
        long durationMs = (startTimestamp != null) ? (currentTimestamp - startTimestamp) : (battle.getTime() * 50L);

        String opponentType = "UNKNOWN";
        for (BattleActor actor : battle.getActors()) {
            if (actor.getSide() != playerActor.getSide()) {
                opponentType = actor.getType().name();
                break;
            }
        }

        int faintedCount = 0;
        List<String> teamSnapshot = new ArrayList<>();
        for (BattlePokemon p : playerActor.getPokemonList()) {
            if (p.getHealth() <= 0) faintedCount++;
            double hpPercent = (double) p.getHealth() / p.getMaxHealth() * 100;
            teamSnapshot.add(p.getEffectedPokemon().getSpecies().getName() + String.format(":%.1f%%", hpPercent));
        }

        LOGGER.info("BigData Battle: {} vs {} (Duration: {}s)", result, opponentType, durationMs / 1000);

        return new BattleResult(
                battle.getBattleId().toString(),
                playerActor.getEntity().getUuidAsString(),
                result,
                opponentType,
                durationMs,
                faintedCount,
                teamSnapshot.toString(),
                TelemetryUtils.getBiomeName(playerActor.getEntity()),
                TelemetryUtils.getWorldName(playerActor.getEntity()),
                currentTimestamp
        );
    }
}