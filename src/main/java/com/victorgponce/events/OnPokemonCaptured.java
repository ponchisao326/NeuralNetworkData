package com.victorgponce.events;

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.data_objects.Ivs;
import com.victorgponce.data_objects.PokemonCaught;
import com.victorgponce.utils.BigDataUtils;

import static com.victorgponce.cache.PokemonData.pokemonCaughtArrayList;

public class OnPokemonCaptured {

    public static void onCaptureEvent(PokemonCapturedEvent event) {
        Pokemon pokemonCaptured = event.getPokemon();

        int level = pokemonCaptured.getLevel();
        String nature = pokemonCaptured.getNature().getName().getPath();
        String ability = pokemonCaptured.getAbility().getName();
        boolean shiny = pokemonCaptured.getShiny();

        Ivs ivs = new Ivs(
                pokemonCaptured.getIvs().get(Stats.HP),
                pokemonCaptured.getIvs().get(Stats.ATTACK),
                pokemonCaptured.getIvs().get(Stats.DEFENCE),
                pokemonCaptured.getIvs().get(Stats.SPECIAL_ATTACK),
                pokemonCaptured.getIvs().get(Stats.SPECIAL_DEFENCE),
                pokemonCaptured.getIvs().get(Stats.SPEED)
        );

        String ballUsed = pokemonCaptured.getCaughtBall().getName().getPath();

        float dexPercentage = BigDataUtils.getNationalDexPercentage(event.getPlayer());

        PokemonCaught pokemonCaught = new PokemonCaught(pokemonCaptured.getSpecies().getName(),
                level,
                nature,
                ability,
                shiny,
                ivs,
                ballUsed,
                dexPercentage);

        pokemonCaughtArrayList.add(pokemonCaught);
    }

}
