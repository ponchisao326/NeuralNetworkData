package com.victorgponce.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokedex.PokedexManager;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.data_objects.Ivs;
import com.victorgponce.utils.BigDataUtils;
import net.minecraft.util.Identifier;

import static com.mojang.text2speech.Narrator.LOGGER;

public class OnPokemonCaptured {

    public static void onCaptureEvent(PokemonCapturedEvent event) {
        Pokemon pokemon = event.getPokemon();

        int level = pokemon.getLevel();
        String nature = pokemon.getNature().getName().getPath();
        String ability = pokemon.getAbility().getName();
        boolean shiny = pokemon.getShiny();

        Ivs ivs = new Ivs(
                pokemon.getIvs().get(Stats.HP),
                pokemon.getIvs().get(Stats.ATTACK),
                pokemon.getIvs().get(Stats.DEFENCE),
                pokemon.getIvs().get(Stats.SPECIAL_ATTACK),
                pokemon.getIvs().get(Stats.SPECIAL_DEFENCE),
                pokemon.getIvs().get(Stats.SPEED)
        );

        String ball_used = pokemon.getCaughtBall().getName().getPath();

        float dexPercentage = BigDataUtils.getNationalDexPercentage(event.getPlayer());

        LOGGER.info("BigData Log: Jugador {} capturó {}, lvl: {}, nature: {}, ability: {}, shiny: {}, ivs: {}, ball: {}, dexPercentage: {}%",
                event.getPlayer().getName().getString(),
                pokemon.getSpecies().getName(),
                level,
                nature,
                ability,
                shiny,
                ivs,
                ball_used,
                dexPercentage);
    }

}
