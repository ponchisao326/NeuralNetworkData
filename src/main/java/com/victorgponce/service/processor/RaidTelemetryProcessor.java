package com.victorgponce.service.processor;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.victorgponce.model.RaidInteraction;
import com.victorgponce.model.RaidMetadata;
import com.victorgponce.repository.DataRepository;
import com.victorgponce.utils.TelemetryUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

import static com.victorgponce.NeuralNetworkData.LOGGER;

public class RaidTelemetryProcessor {

    public RaidInteraction analyzeRaidEnd(RaidEndEvent event, Map<UUID, RaidMetadata> metadataMap, DataRepository repository) {
        LOGGER.info("[BigData] Procesando RaidEnd en Processor.");

        Pokemon bossPokemon = event.getPokemon();
        ServerPlayerEntity eventPlayer = event.getPlayer();

        if (bossPokemon == null || eventPlayer == null) return null;

        String eventSpecies = bossPokemon.getSpecies().getName();
        UUID eventPlayerUuid = eventPlayer.getUuid();
        UUID foundBattleId = null;

        // Lógica de búsqueda (Lectura)
        for (UUID battleId : metadataMap.keySet()) {
            var damageMap = repository.getDamageMap(battleId);
            if (damageMap != null && damageMap.containsKey(eventPlayerUuid)) {
                RaidMetadata meta = metadataMap.get(battleId);
                if (meta != null && meta.bossSpecies().equalsIgnoreCase(eventSpecies)) {
                    foundBattleId = battleId;
                    break;
                }
            }
        }

        if (foundBattleId == null) {
            for (Map.Entry<UUID, RaidMetadata> entry : metadataMap.entrySet()) {
                if (entry.getValue().bossSpecies().equalsIgnoreCase(eventSpecies)) {
                    foundBattleId = entry.getKey();
                    break;
                }
            }
        }

        if (foundBattleId == null) {
            LOGGER.info("[BigData] Fallo: No se encontró batalla activa para " + eventSpecies);
            return null;
        }

        RaidMetadata meta = metadataMap.get(foundBattleId);
        if (meta == null) return null;

        float damage = 0f;
        int participants = 1;
        var dmgMap = repository.getDamageMap(foundBattleId);
        if (dmgMap != null) {
            damage = dmgMap.getOrDefault(eventPlayerUuid, 0f);
            participants = Math.max(1, dmgMap.size());
        }

        String result = event.isWin() ? "WIN" : "LOSS";
        LOGGER.info("BigData RAID: " + meta.bossSpecies() + " | Result: " + result + " | Dmg: " + damage);

        return new RaidInteraction(
                foundBattleId.toString(),
                eventPlayerUuid.toString(),
                meta.bossSpecies(),
                meta.raidTier(),
                result,
                participants,
                damage,
                TelemetryUtils.getBiomeName(eventPlayer),
                TelemetryUtils.getWorldName(eventPlayer),
                System.currentTimeMillis()
        );
    }
}