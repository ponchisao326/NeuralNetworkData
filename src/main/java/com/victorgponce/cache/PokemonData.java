package com.victorgponce.cache;

import com.victorgponce.data_objects.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.victorgponce.NeuralNetworkData.LOGGER;

public class PokemonData {

    // Buffers for output to Database
    public static ConcurrentLinkedQueue<CaughtPokemon> caughtPokemonBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<ReleasedPokemon> releasedPokemonBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PokemonHatched> hatchedPokemonBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<BattleResult> battleResultsBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PlayerSession> sessionBuffer = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<RaidInteraction> raidBuffer = new ConcurrentLinkedQueue<>();

    // Temporal maps for quick calculations
    public static ConcurrentHashMap<UUID, Long> battleStartTimestamps = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Float>> battleDamageTracker = new ConcurrentHashMap<>();

    // --- "Final timestamp" registry for cleanup ---
    public static ConcurrentHashMap<UUID, Long> finishedBattleTimestamps = new ConcurrentHashMap<>();

    /**
     * TODO: Call this periodically (Every DB save)
     * Cleanup battle data finished after 2 minutes.
     */
    public static void cleanUpStaleData() {
        long now = System.currentTimeMillis();
        long expirationTime = 120000; // 2 minutes margin till the fight ends

        finishedBattleTimestamps.forEach((battleId, endTime) -> {
            if (now - endTime > expirationTime) {
                // Security time has ended, delete everything
                battleDamageTracker.remove(battleId);
                battleStartTimestamps.remove(battleId);
                finishedBattleTimestamps.remove(battleId);
                LOGGER.info("Limpieza de Memoria: Batalla " + battleId + " eliminada.");
            }
        });
    }
}
