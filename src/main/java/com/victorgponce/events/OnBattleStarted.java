package com.victorgponce.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.cache.PokemonData;
import com.victorgponce.data_objects.RaidMetadata;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class OnBattleStarted {

    public static void onBattleStarted(BattleStartedEvent event) {
        PokemonBattle battle = event.getBattle();

        // Save Timestamp
        PokemonData.battleStartTimestamps.put(battle.getBattleId(), System.currentTimeMillis());

        // Detect if its a Raid
        for (BattleActor actor : battle.getActors()) {
            if (actor instanceof PokemonBattleActor pokemonActor) {
                Entity entity = pokemonActor.getEntity();
                Pokemon pokemon = pokemonActor.getEntity().getPokemon();

                if (pokemon == null) continue;

                boolean isRaid = false;
                String raidTier = "UNKNOWN";

                // A) RaidDens Detection (Aspects)
                if (pokemon.getAspects().contains("raid")) isRaid = true;

                // Manual Detection (Tags)
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

                    // Save the UUID of the physical entity, which is the one that dies in the world.
                    UUID trackingUuid = (entity != null) ? entity.getUuid() : pokemon.getUuid();

                    RaidMetadata metadata = new RaidMetadata(species, raidTier, trackingUuid);

                    PokemonData.raidMetadataCache.put(battle.getBattleId(), metadata);
                    break;
                }
            }
        }
    }
}