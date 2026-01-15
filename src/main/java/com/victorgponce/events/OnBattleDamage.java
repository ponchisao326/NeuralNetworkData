package com.victorgponce.events;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.victorgponce.cache.PokemonData;
import com.victorgponce.data_objects.RaidMetadata;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OnBattleDamage {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(OnBattleDamage::onEntityDamage);
    }

    private static void onEntityDamage(LivingEntity entity, DamageSource source, float baseDamage, float damageTaken, boolean blocked) {
        if (!(entity instanceof PokemonEntity pokemonEntity)) return;

        // Identify Player
        UUID playerUuid = null;
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            playerUuid = player.getUuid();
        } else if (source.getAttacker() instanceof PokemonEntity attackerPokemon) {
            if (attackerPokemon.getOwnerUuid() != null) {
                playerUuid = attackerPokemon.getOwnerUuid();
            }
        }

        if (playerUuid == null) return;

        // Obtain or recover Battle ID
        UUID battleId = pokemonEntity.getBattleId();

        // B Plan: Recover by UUID id battleID is null (Raids common bug)
        if (battleId == null) {
            UUID entityUuid = entity.getUuid();
            for (Map.Entry<UUID, RaidMetadata> entry : PokemonData.raidMetadataCache.entrySet()) {
                if (entry.getValue().bossUuid().equals(entityUuid)) {
                    battleId = entry.getKey();
                    break;
                }
            }
        }

        // Just add damage
        if (battleId != null) {
            PokemonData.battleDamageTracker.putIfAbsent(battleId, new ConcurrentHashMap<>());
            PokemonData.battleDamageTracker.get(battleId).merge(playerUuid, damageTaken, Float::sum);
        }
    }
}