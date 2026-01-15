package com.victorgponce.events;

import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.data_objects.Ivs;
import com.victorgponce.data_objects.PokemonHatched;

import static com.victorgponce.cache.PokemonData.hatchedPokemonBuffer;
import static com.victorgponce.utils.DataUtils.getTotalIvs;

public class OnEggHatched {

    public static void onHatchedEvent(HatchEggEvent.Post event) {
        Pokemon pokemon = event.getPokemon();
        long currentTimestamp = System.currentTimeMillis();

        // Get Ivs
        Ivs ivs = getTotalIvs(pokemon);

        String biome = "Unknown";
        if (event.getPlayer() != null) {
            biome = event.getPlayer().getWorld()
                    .getBiome(event.getPlayer().getBlockPos())
                    .getKey()
                    .map(key -> key.getValue().toString())
                    .orElse("unknown");
        }

        PokemonHatched hatchedData = new PokemonHatched(
                pokemon.getUuid(),
                pokemon.getSpecies().getName(),
                pokemon.getShiny(),
                ivs,
                pokemon.getAbility().getName(),
                pokemon.getNature().getName().getPath(),
                pokemon.getCaughtBall().getName().getPath(),
                event.getPlayer().getUuidAsString(),
                biome,
                currentTimestamp
        );

        hatchedPokemonBuffer.add(hatchedData);

    }

}
