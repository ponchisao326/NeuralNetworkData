package com.victorgponce.service;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.victorgponce.model.*;
import com.victorgponce.repository.DataRepository;
import com.victorgponce.utils.BigDataUtils;
import com.victorgponce.utils.DataUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.Listing.ItemListing;
import org.pokesplash.gts.Listing.Listing;
import org.pokesplash.gts.Listing.PokemonListing;
import org.pokesplash.gts.api.event.events.PurchaseEvent;

import java.util.*;

import static com.victorgponce.NeuralNetworkData.LOGGER;

/**
 * Facade Pattern:
 * It acts as an unification interface to process the logic and access to repository
 * Controllers only call methods here
 */
public class TelemetryFacade {

    private static final TelemetryFacade INSTANCE = new TelemetryFacade();
    private final DataRepository repository;

    private TelemetryFacade() {
        this.repository = DataRepository.getInstance();
    }

    public static TelemetryFacade getInstance() {
        return INSTANCE;
    }

    // --- Lifecycle Logic ---

    public void processCapture(Pokemon pokemonCaptured, ServerPlayerEntity player) {
        long currentTimestamp = System.currentTimeMillis();

        int level = pokemonCaptured.getLevel();
        String nature = pokemonCaptured.getNature().getName().getPath();
        String ability = pokemonCaptured.getAbility().getName();
        boolean shiny = pokemonCaptured.getShiny();
        Ivs ivs = DataUtils.getTotalIvs(pokemonCaptured);
        String ballUsed = pokemonCaptured.getCaughtBall().getName().getPath();
        float dexPercentage = BigDataUtils.getNationalDexPercentage(player);

        CaughtPokemon data = new CaughtPokemon(
                pokemonCaptured.getUuid(),
                pokemonCaptured.getSpecies().getName(),
                level,
                nature,
                ability,
                shiny,
                ivs,
                ballUsed,
                dexPercentage,
                currentTimestamp
        );

        repository.addCaught(data);
    }

    public void processRelease(Pokemon pokemonReleased, ServerPlayerEntity player) {
        long currentTimestamp = System.currentTimeMillis();
        String species = pokemonReleased.getSpecies().getName();
        int level = pokemonReleased.getLevel();
        boolean shiny = pokemonReleased.getShiny();
        Ivs ivs = DataUtils.getTotalIvs(pokemonReleased);

        String playerUuid = "unknown";
        String playerName = "System";
        String biome = "Unknown";

        if (player != null) {
            playerUuid = player.getUuidAsString();
            playerName = player.getName().getString();
            biome = player.getWorld().getBiome(player.getBlockPos())
                    .getKey().map(k -> k.getValue().toString()).orElse("Unknown");
        }

        ReleasedPokemon data = new ReleasedPokemon(
                pokemonReleased.getUuid(),
                species,
                level,
                shiny,
                ivs,
                playerName,
                biome,
                playerUuid,
                currentTimestamp,
                0
        );

        repository.addReleased(data);
    }

    public void processHatch(Pokemon pokemon, ServerPlayerEntity player) {
        long currentTimestamp = System.currentTimeMillis();
        Ivs ivs = DataUtils.getTotalIvs(pokemon);
        String biome = "Unknown";

        if (player != null) {
            biome = player.getWorld().getBiome(player.getBlockPos())
                    .getKey().map(key -> key.getValue().toString()).orElse("unknown");
        }

        PokemonHatched data = new PokemonHatched(
                pokemon.getUuid(),
                pokemon.getSpecies().getName(),
                pokemon.getShiny(),
                ivs,
                pokemon.getAbility().getName(),
                pokemon.getNature().getName().getPath(),
                pokemon.getCaughtBall().getName().getPath(),
                player != null ? player.getUuidAsString() : "unknown",
                biome,
                currentTimestamp
        );

        repository.addHatched(data);
    }

    // --- Session Logic ---

    public void processSession(ServerPlayerEntity player, String type) {
        long now = System.currentTimeMillis();
        String ip = "unknown";
        try {
            ip = player.getIp();
        } catch (Exception e) {
            // Singleplayer ignore
        }

        PlayerSession session = new PlayerSession(
                player.getUuidAsString(),
                player.getName().getString(),
                type,
                ip,
                now
        );
        repository.addSession(session);
    }

    // --- Battle Logic ---

    public void processBattleStart(PokemonBattle battle) {
        repository.saveBattleStartTime(battle.getBattleId(), System.currentTimeMillis());

        // Detect Raid
        for (BattleActor actor : battle.getActors()) {
            if (actor instanceof PokemonBattleActor pokemonActor) {
                Entity entity = pokemonActor.getEntity();
                Pokemon pokemon = pokemonActor.getEntity().getPokemon();
                if (pokemon == null) continue;

                boolean isRaid = false;
                String raidTier = "UNKNOWN";

                if (pokemon.getAspects().contains("raid")) isRaid = true;

                if (!isRaid && entity != null) {
                    for (String tag : entity.getCommandTags()) {
                        if (tag.contains("raid") || tag.contains("boss")) {
                            isRaid = true;
                            if (tag.contains("tier")) raidTier = tag;
                        }
                    }
                }

                if (isRaid) {
                    String species = pokemon.getSpecies().getName();
                    UUID trackingUuid = (entity != null) ? entity.getUuid() : pokemon.getUuid();
                    RaidMetadata metadata = new RaidMetadata(species, raidTier, trackingUuid);
                    repository.saveRaidMetadata(battle.getBattleId(), metadata);
                    break;
                }
            }
        }
    }

    public void processBattleEnd(PokemonBattle battle, PlayerBattleActor playerActor, String result) {
        long currentTimestamp = System.currentTimeMillis();
        ServerPlayerEntity playerEntity = playerActor.getEntity();
        if (playerEntity == null) return;

        UUID battleId = battle.getBattleId();
        Long startTimestamp = repository.removeBattleStartTime(battleId);

        long durationMs;
        if (startTimestamp != null) {
            durationMs = currentTimestamp - startTimestamp;
        } else {
            durationMs = battle.getTime() * 50L;
        }

        String opponentType = "UNKNOWN";
        for (BattleActor actor : battle.getActors()) {
            if (actor.getSide() != playerActor.getSide()) {
                opponentType = actor.getType().name();
                break;
            }
        }

        int faintedCount = 0;
        List<String> teamSnapshot = new ArrayList<>();
        for (BattlePokemon p : playerActor.getPokemonList()) {
            if (p.getHealth() <= 0) faintedCount++;
            double hpPercent = (double) p.getHealth() / p.getMaxHealth() * 100;
            teamSnapshot.add(p.getEffectedPokemon().getSpecies().getName() + String.format(":%.1f%%", hpPercent));
        }

        String biome = playerEntity.getWorld().getBiome(playerEntity.getBlockPos())
                .getKey().map(k -> k.getValue().toString()).orElse("unknown");

        BattleResult data = new BattleResult(
                battle.getBattleId().toString(),
                playerEntity.getUuidAsString(),
                result,
                opponentType,
                durationMs,
                faintedCount,
                teamSnapshot.toString(),
                biome,
                currentTimestamp
        );

        repository.addBattleResult(data);
        repository.markBattleFinished(battle.getBattleId());

        // Log console output as requested logic preservation
        LOGGER.info("BigData Battle: " + result + " vs " + opponentType + " (Duration: " + (durationMs/1000) + "s)");
    }

    // --- Raid Logic ---

    public void processRaidEnd(RaidEndEvent event) {
        LOGGER.info("[BigData] Evento RaidEnd capturado.");
        boolean won = event.isWin();
        String result = won ? "WIN" : "LOSS";
        Pokemon bossPokemon = event.getPokemon();
        ServerPlayerEntity eventPlayer = event.getPlayer();

        if (bossPokemon == null || eventPlayer == null) {
            LOGGER.info("[BigData] Error: Pokemon o Jugador nulos en el evento.");
            return;
        }

        String eventSpecies = bossPokemon.getSpecies().getName();
        UUID eventPlayerUuid = eventPlayer.getUuid();
        UUID foundBattleId = null;

        // We need to iterate over the keys in repository logic, but for Facade we can access repository methods
        // Refactoring logic to use repository state:

        // Logic recovery from original file
        for (UUID battleId : repository.getRaidMetadataMap().keySet()) {
            var damageMap = repository.getDamageMap(battleId);
            if (damageMap != null && damageMap.containsKey(eventPlayerUuid)) {
                RaidMetadata meta = repository.getRaidMetadata(battleId);
                if (meta != null && meta.bossSpecies().equalsIgnoreCase(eventSpecies)) {
                    foundBattleId = battleId;
                    break;
                }
            }
        }

        // Pass 2: Metadata only
        if (foundBattleId == null) {
            for (Map.Entry<UUID, RaidMetadata> entry : repository.getRaidMetadataMap().entrySet()) {
                if (entry.getValue().bossSpecies().equalsIgnoreCase(eventSpecies)) {
                    foundBattleId = entry.getKey();
                    break;
                }
            }
        }

        if (foundBattleId == null) {
            LOGGER.info("[BigData] Fallo: No se encontró ninguna batalla activa en caché para " + eventSpecies);
            return;
        }

        RaidMetadata meta = repository.removeRaidMetadata(foundBattleId);
        if (meta == null) return;

        float damage = 0f;
        int participants = 1;

        var dmgMap = repository.getDamageMap(foundBattleId);
        if (dmgMap != null) {
            damage = dmgMap.getOrDefault(eventPlayerUuid, 0f);
            participants = Math.max(1, dmgMap.size());
        }

        String biome = eventPlayer.getWorld().getBiome(eventPlayer.getBlockPos()).getKey()
                .map(k -> k.getValue().toString()).orElse("Unknown");

        RaidInteraction data = new RaidInteraction(
                foundBattleId.toString(),
                eventPlayerUuid.toString(),
                meta.bossSpecies(),
                meta.raidTier(),
                result,
                participants,
                damage,
                biome,
                System.currentTimeMillis()
        );

        repository.addRaidInteraction(data);
        LOGGER.info("BigData RAID: " + meta.bossSpecies() + " | Result: " + result + " | Dmg: " + damage);

        repository.removeDamageTracker(foundBattleId);
        repository.removeBattleStartTime(foundBattleId);
        repository.markBattleFinished(foundBattleId);
    }

    public void processGtsTransaction(PurchaseEvent event) {
        Listing<?> listing = event.getProduct();
        long now = System.currentTimeMillis();

        // 1. Extraer Datos Básicos
        String sellerUuid = listing.getSellerUuid().toString();
        String buyerUuid = event.getBuyer().toString(); // PurchaseEvent tiene el método getBuyer()
        double price = listing.getPrice();

        // 2. Calcular Liquidez (Tiempo en el mercado)
        // Fórmula: DuraciónTotal - (FechaFin - Ahora)
        long durationMs = -1;
        double configDurationHours = Gts.config.getListingDuration();

        if (listing.getEndTime() != -1 && configDurationHours > 0) {
            long maxDurationMs = (long) (configDurationHours * 3600000L);
            long remainingTime = listing.getEndTime() - now;
            durationMs = maxDurationMs - remainingTime;

            // Corrección por seguridad (si el lag causa negativos)
            if (durationMs < 0) durationMs = 0;
        }

        // 3. Determinar Tipo y Descripción
        String type;
        String description;

        if (listing instanceof PokemonListing pokemonListing) {
            type = "POKEMON";
            Pokemon pokemon = pokemonListing.getListing(); // Devuelve el objeto Pokemon de Cobblemon

            String species = pokemon.getSpecies().getName();
            int level = pokemon.getLevel();
            String shinyStr = pokemon.getShiny() ? " (Shiny)" : "";

            description = species + " Lvl" + level + shinyStr;

        } else if (listing instanceof ItemListing itemListing) {
            type = "ITEM";
            ItemStack itemStack = itemListing.getListing(); // Devuelve ItemStack

            String itemName = itemStack.getName().getString();
            int count = itemStack.getCount();

            description = itemName + " (x" + count + ")";
        } else {
            type = "UNKNOWN";
            description = listing.getListingName();
        }

        // 4. Crear DTO y guardar
        GtsTransaction data = new GtsTransaction(
                sellerUuid,
                buyerUuid,
                type,
                description,
                price,
                durationMs,
                now
        );

        repository.addGtsTransaction(data);
        LOGGER.info("BigData ECONOMY: Sold {} for {}", description, price);
    }

    // --- Session Snapshot Logic ---

    public void trackPlayerState(ServerPlayerEntity player) {
        // Obtain current biome
        String currentBiome = player.getWorld().getBiome(player.getBlockPos())
                .getKey().map(k -> k.getValue().toString()).orElse("unknown");

        // Save
        repository.trackBiome(player.getUuid(), currentBiome);
    }

    public void processSnapshot(ServerPlayerEntity player) {
        long now = System.currentTimeMillis();

        // Obtain distances
        // Note: Walk_one_cm includes walking. Sprint and Fly are separate.
        long walked = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        long sprinted = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM));
        long flown = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.FLY_ONE_CM)); // Si vuelan en creativo/pokemon
        long total = walked + sprinted + flown;

        // Retrieve and clean visited biomes in the last 5 minutes
        Set<String> biomes = repository.popVisitedBiomes(player.getUuid());

        // Create snapshot
        SessionSnapshot snapshot = new SessionSnapshot(
                player.getUuidAsString(),
                walked,
                sprinted,
                flown,
                total,
                new ArrayList<>(biomes), // Conver Set to List
                now
        );

        repository.addSessionSnapshot(snapshot);
    }

    // --- Command Logic ---
    public void processCommand(ServerPlayerEntity player, String command, boolean success) {
        String rootCommand = command.split(" ")[0];

        CommandUsage data = new CommandUsage(
                player.getUuidAsString(),
                rootCommand,
                success,
                System.currentTimeMillis()
        );
        repository.addCommandUsage(data);
    }

    // --- Death Logic ---
    public void processDeath(ServerPlayerEntity player, DamageSource source) {
        String cause = source.getName(); // Ej: "lava", "mob"
        String biome = player.getWorld().getBiome(player.getBlockPos())
                .getKey().map(k -> k.getValue().toString()).orElse("unknown");

        PlayerDeath data = new PlayerDeath(
                player.getUuidAsString(),
                cause,
                player.experienceLevel,
                biome,
                System.currentTimeMillis()
        );
        repository.addPlayerDeath(data);
    }

    public void cleanupPlayer(ServerPlayerEntity player) {
        repository.clearTracker(player.getUuid());
    }
}