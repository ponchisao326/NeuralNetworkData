package com.victorgponce.repository;

import com.victorgponce.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Singleton repository to store data on memory
 * It replaces the antique static class PokemonData
 * listed on other commits
 */
public class DataRepository {

    private static final DataRepository INSTANCE = new DataRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger("DataRepository");

    // Buffers for output
    private final ConcurrentLinkedQueue<CaughtPokemon> caughtPokemonBuffer = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ReleasedPokemon> releasedPokemonBuffer = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<PokemonHatched> hatchedPokemonBuffer = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<BattleResult> battleResultsBuffer = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<PlayerSession> sessionBuffer = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<RaidInteraction> raidBuffer = new ConcurrentLinkedQueue<>();

    private final ConcurrentLinkedQueue<GtsTransaction> gtsBuffer = new ConcurrentLinkedQueue<>();

    // Temporal maps
    private final ConcurrentHashMap<UUID, Long> battleStartTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Float>> battleDamageTracker = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RaidMetadata> raidMetadataCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> finishedBattleTimestamps = new ConcurrentHashMap<>();

    private DataRepository() {}

    public static DataRepository getInstance() {
        return INSTANCE;
    }

    // --- Getters for Buffers (Read-Only access usually, but Queue is mutable) ---
    public ConcurrentLinkedQueue<CaughtPokemon> getCaughtBuffer() { return caughtPokemonBuffer; }
    public ConcurrentLinkedQueue<ReleasedPokemon> getReleasedBuffer() { return releasedPokemonBuffer; }
    public ConcurrentLinkedQueue<PokemonHatched> getHatchedBuffer() { return hatchedPokemonBuffer; }
    public ConcurrentLinkedQueue<BattleResult> getBattleResultBuffer() { return battleResultsBuffer; }
    public ConcurrentLinkedQueue<PlayerSession> getSessionBuffer() { return sessionBuffer; }
    public ConcurrentLinkedQueue<RaidInteraction> getRaidBuffer() { return raidBuffer; }

    public ConcurrentLinkedQueue<GtsTransaction> getGtsBuffer() { return gtsBuffer; }

    // --- State Management Methods ---

    public void addCaught(CaughtPokemon data) { caughtPokemonBuffer.add(data); }
    public void addReleased(ReleasedPokemon data) { releasedPokemonBuffer.add(data); }
    public void addHatched(PokemonHatched data) { hatchedPokemonBuffer.add(data); }
    public void addBattleResult(BattleResult data) { battleResultsBuffer.add(data); }
    public void addSession(PlayerSession data) { sessionBuffer.add(data); }
    public void addRaidInteraction(RaidInteraction data) { raidBuffer.add(data); }

    public void addGtsTransaction(GtsTransaction data) { gtsBuffer.add(data); }

    public void saveBattleStartTime(UUID battleId, long timestamp) {
        battleStartTimestamps.put(battleId, timestamp);
    }

    public Long removeBattleStartTime(UUID battleId) {
        return battleStartTimestamps.remove(battleId);
    }

    public void saveRaidMetadata(UUID battleId, RaidMetadata metadata) {
        raidMetadataCache.put(battleId, metadata);
    }

    public RaidMetadata getRaidMetadata(UUID battleId) {
        return raidMetadataCache.get(battleId);
    }

    public ConcurrentHashMap<UUID, RaidMetadata> getRaidMetadataMap() {
        return raidMetadataCache;
    }

    public RaidMetadata removeRaidMetadata(UUID battleId) {
        return raidMetadataCache.remove(battleId);
    }

    public void logDamage(UUID battleId, UUID playerUuid, float damage) {
        battleDamageTracker.putIfAbsent(battleId, new ConcurrentHashMap<>());
        battleDamageTracker.get(battleId).merge(playerUuid, damage, Float::sum);
    }

    public ConcurrentHashMap<UUID, Float> getDamageMap(UUID battleId) {
        return battleDamageTracker.get(battleId);
    }

    public void removeDamageTracker(UUID battleId) {
        battleDamageTracker.remove(battleId);
    }

    public void markBattleFinished(UUID battleId) {
        finishedBattleTimestamps.put(battleId, System.currentTimeMillis());
    }

    /**
     * Cleanup battle data finished after 2 minutes.
     */
    public void cleanUpStaleData() {
        long now = System.currentTimeMillis();
        long expirationTime = 120000; // 2 minutes margin

        finishedBattleTimestamps.forEach((battleId, endTime) -> {
            if (now - endTime > expirationTime) {
                battleDamageTracker.remove(battleId);
                battleStartTimestamps.remove(battleId);
                raidMetadataCache.remove(battleId);
                finishedBattleTimestamps.remove(battleId);
                LOGGER.info("Limpieza de Memoria: Batalla " + battleId + " eliminada.");
            }
        });
    }
}