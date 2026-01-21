package com.victorgponce.service;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.victorgponce.model.*;
import com.victorgponce.repository.DataRepository;
import com.victorgponce.service.processor.*;
import com.victorgponce.utils.TelemetryUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.pokesplash.gts.api.event.events.PurchaseEvent;

import java.util.UUID;

public class TelemetryFacade {

    private static final TelemetryFacade INSTANCE = new TelemetryFacade();
    private final DataRepository repository;

    // Processors
    private final PokemonTelemetryProcessor pokemonProcessor;
    private final BattleTelemetryProcessor battleProcessor;
    private final RaidTelemetryProcessor raidProcessor;
    private final EconomyTelemetryProcessor economyProcessor;
    private final SessionTelemetryProcessor sessionProcessor;
    private final BehaviorTelemetryProcessor behaviorProcessor;

    private TelemetryFacade() {
        this.repository = DataRepository.getInstance();
        this.pokemonProcessor = new PokemonTelemetryProcessor();
        this.battleProcessor = new BattleTelemetryProcessor();
        this.raidProcessor = new RaidTelemetryProcessor();
        this.economyProcessor = new EconomyTelemetryProcessor();
        this.sessionProcessor = new SessionTelemetryProcessor();
        this.behaviorProcessor = new BehaviorTelemetryProcessor();
    }

    public static TelemetryFacade getInstance() {
        return INSTANCE;
    }

    // --- Lifecycle Logic ---

    public void processCapture(Pokemon pokemon, ServerPlayerEntity player) {
        CaughtPokemon data = pokemonProcessor.createCaught(pokemon, player);
        repository.addCaught(data);
    }

    public void processRelease(Pokemon pokemon, ServerPlayerEntity player) {
        ReleasedPokemon data = pokemonProcessor.createReleased(pokemon, player);
        repository.addReleased(data);
    }

    public void processHatch(Pokemon pokemon, ServerPlayerEntity player) {
        PokemonHatched data = pokemonProcessor.createHatched(pokemon, player);
        repository.addHatched(data);
    }

    // --- Session Logic ---

    public void processSession(ServerPlayerEntity player, String type) {
        PlayerSession data = sessionProcessor.createSession(player, type);
        repository.addSession(data);
    }

    public void trackPlayerState(ServerPlayerEntity player) {
        // Only tracking logic, it doesn't generate persistent DTO
        String biome = TelemetryUtils.getBiomeName(player);
        repository.trackBiome(player.getUuid(), biome);
    }

    public void processSnapshot(ServerPlayerEntity player) {
        // Processor need the visited biomes from repo to create the snapshot
        var visitedBiomes = repository.popVisitedBiomes(player.getUuid());
        SessionSnapshot data = sessionProcessor.createSnapshot(player, visitedBiomes);
        repository.addSessionSnapshot(data);
    }

    public void cleanupPlayer(ServerPlayerEntity player) {
        repository.clearTracker(player.getUuid());
    }

    // --- Battle Logic ---

    public void processBattleStart(PokemonBattle battle) {
        // Obtain a context with the data to save
        BattleTelemetryProcessor.BattleStartContext ctx = battleProcessor.analyzeBattleStart(battle);

        // Facade save the data
        repository.saveBattleStartTime(ctx.battleId(), ctx.timestamp());
        if (ctx.raidMetadata() != null) {
            repository.saveRaidMetadata(ctx.battleId(), ctx.raidMetadata());
        }
    }

    public void processBattleEnd(PokemonBattle battle, PlayerBattleActor playerActor, String result) {
        // We need to read the init time to calculate the duration
        Long startTime = repository.removeBattleStartTime(battle.getBattleId());

        BattleResult data = battleProcessor.analyzeBattleEnd(battle, playerActor, result, startTime);

        if (data != null) {
            repository.addBattleResult(data);
            repository.markBattleFinished(battle.getBattleId());
        }
    }

    // --- Raid Logic ---

    public void processRaidEnd(RaidEndEvent event) {
        // Processor needs to read the actual state (Maps) to know what happened
        RaidInteraction data = raidProcessor.analyzeRaidEnd(event, repository.getRaidMetadataMap(), repository);

        if (data != null) {
            repository.addRaidInteraction(data);

            // Post-Saving cleanup
            UUID battleId = UUID.fromString(data.battleId());
            repository.removeDamageTracker(battleId);
            repository.removeBattleStartTime(battleId);
            repository.markBattleFinished(battleId);
            repository.removeRaidMetadata(battleId);
        }
    }

    // --- Economy Logic ---

    public void processGtsTransaction(PurchaseEvent event) {
        GtsTransaction data = economyProcessor.analyzeTransaction(event);
        repository.addGtsTransaction(data);
    }

    // --- Behavior Logic ---

    public void processCommand(ServerPlayerEntity player, String command, boolean success) {
        CommandUsage data = behaviorProcessor.createCommandUsage(player, command, success);
        repository.addCommandUsage(data);
    }

    public void processDeath(ServerPlayerEntity player, DamageSource source) {
        PlayerDeath data = behaviorProcessor.createDeath(player, source);
        repository.addPlayerDeath(data);
    }
}