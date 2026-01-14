package com.victorgponce.cache;

import com.victorgponce.data_objects.CaughtPokemon;
import com.victorgponce.data_objects.ReleasedPokemon;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PokemonData {

    public static ConcurrentLinkedQueue<CaughtPokemon> caughtPokemonBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<ReleasedPokemon> releasedPokemonBuffer = new ConcurrentLinkedQueue<>();

}
