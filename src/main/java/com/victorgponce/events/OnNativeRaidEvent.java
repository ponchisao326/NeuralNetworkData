package com.victorgponce.events;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.victorgponce.cache.PokemonData;
import com.victorgponce.data_objects.RaidInteraction;
import com.victorgponce.data_objects.RaidMetadata;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public class OnNativeRaidEvent {

    public static void register() {
        RaidEvents.RAID_END.subscribe(OnNativeRaidEvent::onRaidEnd);
    }

    private static void onRaidEnd(RaidEndEvent event) {
        System.out.println("[BigData] Evento RaidEnd capturado.");

        // Obtain event data
        boolean won = event.isWin();
        String result = won ? "WIN" : "LOSS";
        Pokemon bossPokemon = event.getPokemon();
        ServerPlayerEntity eventPlayer = event.getPlayer();

        if (bossPokemon == null || eventPlayer == null) {
            System.out.println("[BigData] Error: Pokemon o Jugador nulos en el evento.");
            return;
        }

        String eventSpecies = bossPokemon.getSpecies().getName();
        UUID eventPlayerUuid = eventPlayer.getUuid();

        // Search for the battle on the cache
        UUID foundBattleId = null;

       // PASS 1: Search for matching players in the damage tracker
        // (This is the most accurate if damage was dealt)
        for (UUID battleId : PokemonData.battleDamageTracker.keySet()) {
            if (PokemonData.battleDamageTracker.get(battleId).containsKey(eventPlayerUuid)) {
                // Verify if metadata matches the event species
                RaidMetadata meta = PokemonData.raidMetadataCache.get(battleId);
                if (meta != null && meta.bossSpecies().equalsIgnoreCase(eventSpecies)) {
                    foundBattleId = battleId;
                    break;
                }
            }
        }

        // PASS 2 (Fallback): Search only by Specia on metadata
        if (foundBattleId == null) {
            for (Map.Entry<UUID, RaidMetadata> entry : PokemonData.raidMetadataCache.entrySet()) {
                if (entry.getValue().bossSpecies().equalsIgnoreCase(eventSpecies)) {
                    foundBattleId = entry.getKey();
                    break;
                }
            }
        }

        if (foundBattleId == null) {
            System.out.println("[BigData] Fallo: No se encontró ninguna batalla activa en caché para " + eventSpecies);
            return;
        }

        // Process and Save
        RaidMetadata meta = PokemonData.raidMetadataCache.remove(foundBattleId);
        if (meta == null) return; // Already processed

        float damage = 0f;
        int participants = 1;

        // Try to grab real data, if else, use default values
        if (PokemonData.battleDamageTracker.containsKey(foundBattleId)) {
            damage = PokemonData.battleDamageTracker.get(foundBattleId).getOrDefault(eventPlayerUuid, 0f);
            participants = Math.max(1, PokemonData.battleDamageTracker.get(foundBattleId).size());
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

        PokemonData.raidBuffer.add(data);
        System.out.println("BigData RAID: " + meta.bossSpecies() + " | Result: " + result + " | Dmg: " + damage);

        // Cleaning
        PokemonData.battleDamageTracker.remove(foundBattleId);
        PokemonData.battleStartTimestamps.remove(foundBattleId);
        PokemonData.finishedBattleTimestamps.put(foundBattleId, System.currentTimeMillis());
    }

    private static void debugCache() {
        System.out.println("--- Contenido del Caché (Metadata) ---");
        if (PokemonData.raidMetadataCache.isEmpty()) {
            System.out.println("  (Vacío)");
        } else {
            PokemonData.raidMetadataCache.forEach((id, meta) ->
                    System.out.println("  ID: " + id + " | Boss: " + meta.bossSpecies())
            );
        }
        System.out.println("--------------------------------------");
    }
}