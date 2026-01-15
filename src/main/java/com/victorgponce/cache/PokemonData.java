package com.victorgponce.cache;

import com.victorgponce.data_objects.BattleResult;
import com.victorgponce.data_objects.CaughtPokemon;
import com.victorgponce.data_objects.PokemonHatched;
import com.victorgponce.data_objects.ReleasedPokemon;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PokemonData {

    public static ConcurrentLinkedQueue<CaughtPokemon> caughtPokemonBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<ReleasedPokemon> releasedPokemonBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PokemonHatched> hatchedPokemonBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<BattleResult> battleResultsBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentHashMap<UUID, Long> battleStartTimestamps = new ConcurrentHashMap<>();

}
