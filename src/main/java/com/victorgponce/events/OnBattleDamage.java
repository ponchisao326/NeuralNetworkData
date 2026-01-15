package com.victorgponce.events;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.victorgponce.cache.PokemonData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OnBattleDamage {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(OnBattleDamage::onEntityDamage);
    }

    private static void onEntityDamage(LivingEntity entity, DamageSource source, float baseDamage, float damageTaken, boolean blocked) {

        // Verify if victim is a Pokemon (Boss)
        if (!(entity instanceof PokemonEntity pokemonEntity)) return;

        // Verify that the attacker is a player (or a player-owned Pokémon)
        UUID playerUuid = null;

        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            playerUuid = player.getUuid();
        } else if (source.getAttacker() instanceof PokemonEntity attackerPokemon) {
            // If the Pokemon attacks, we search for its owner
            if (attackerPokemon.getOwnerUuid() != null) {
                playerUuid = attackerPokemon.getOwnerUuid();
            }
        }

        if (playerUuid == null) return;

        // Obtain Battle ID
        UUID battleId = pokemonEntity.getBattleId();

        if (battleId != null) {
            // --- DAMAGE REGISTER LOGIC ---
            PokemonData.battleDamageTracker.putIfAbsent(battleId, new ConcurrentHashMap<>());

            PokemonData.battleDamageTracker.get(battleId).merge(playerUuid, damageTaken, Float::sum);
        }
    }
}