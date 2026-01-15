package com.victorgponce.utils;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.data_objects.Ivs;

public class DataUtils {

    public static Ivs getTotalIvs(Pokemon pokemon) {
        return new Ivs(
                pokemon.getIvs().get(Stats.HP),
                pokemon.getIvs().get(Stats.ATTACK),
                pokemon.getIvs().get(Stats.DEFENCE),
                pokemon.getIvs().get(Stats.SPECIAL_ATTACK),
                pokemon.getIvs().get(Stats.SPECIAL_DEFENCE),
                pokemon.getIvs().get(Stats.SPEED)
        );
    }

}
