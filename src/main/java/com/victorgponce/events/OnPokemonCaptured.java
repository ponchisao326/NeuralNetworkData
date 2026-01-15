package com.victorgponce.events;

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.data_objects.Ivs;
import com.victorgponce.data_objects.CaughtPokemon;
import com.victorgponce.utils.BigDataUtils;

import static com.victorgponce.cache.PokemonData.caughtPokemonBuffer;
import static com.victorgponce.utils.DataUtils.getTotalIvs;

public class OnPokemonCaptured {

    public static void onCapturedEvent(PokemonCapturedEvent event) {
        Pokemon pokemonCaptured = event.getPokemon();
        long currentTimestamp = System.currentTimeMillis();

        int level = pokemonCaptured.getLevel();
        String nature = pokemonCaptured.getNature().getName().getPath();
        String ability = pokemonCaptured.getAbility().getName();
        boolean shiny = pokemonCaptured.getShiny();

        Ivs ivs = getTotalIvs(pokemonCaptured);

        String ballUsed = pokemonCaptured.getCaughtBall().getName().getPath();

        float dexPercentage = BigDataUtils.getNationalDexPercentage(event.getPlayer());

        CaughtPokemon caughtPokemon = new CaughtPokemon(
                pokemonCaptured.getUuid(),
                pokemonCaptured.getSpecies().getName(),
                level,
                nature,
                ability,
                shiny,
                ivs,
                ballUsed,
                dexPercentage,
                currentTimestamp);

        caughtPokemonBuffer.add(caughtPokemon);
    }

}
