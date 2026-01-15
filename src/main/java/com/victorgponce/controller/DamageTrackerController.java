package com.victorgponce.controller;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.victorgponce.model.RaidMetadata;
import com.victorgponce.repository.DataRepository;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public class DamageTrackerController {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(DamageTrackerController::onEntityDamage);
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
        DataRepository repository = DataRepository.getInstance();

        // B Plan: Recover by UUID if battleID is null (Raids common bug)
        if (battleId == null) {
            UUID entityUuid = entity.getUuid();
            for (Map.Entry<UUID, RaidMetadata> entry : repository.getRaidMetadataMap().entrySet()) {
                if (entry.getValue().bossUuid().equals(entityUuid)) {
                    battleId = entry.getKey();
                    break;
                }
            }
        }

        // Log via Repository directly (optimized for high frequency events)
        if (battleId != null) {
            repository.logDamage(battleId, playerUuid, damageTaken);
        }
    }
}