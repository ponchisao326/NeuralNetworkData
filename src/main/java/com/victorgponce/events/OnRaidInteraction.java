package com.victorgponce.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.victorgponce.cache.PokemonData;
import com.victorgponce.data_objects.RaidInteraction;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class OnRaidInteraction {

    // Cambia 'Battle' por 'PokemonBattle' si tu versión lo requiere
    public static void checkAndStore(PokemonBattle battle, PlayerBattleActor playerActor, String result, long durationMs, String biome) {

        boolean isRaid = false;
        String raidTier = "UNKNOWN";
        String bossSpecies = "Unknown";

        // 1. Detectar si es Raid buscando Tags en el enemigo
        for (BattleActor actor : battle.getActors()) {
            if (actor.getSide() != playerActor.getSide()) {
                if (actor instanceof PokemonBattleActor pokemonActor) {
                    Entity entity = pokemonActor.getEntity();

                    if (entity != null) {
                        // Buscamos pistas en los tags de Minecraft
                        for (String tag : entity.getCommandTags()) {
                            if (tag.contains("raid") || tag.contains("boss")) {
                                isRaid = true;
                                if (tag.contains("tier")) {
                                    raidTier = tag;
                                }
                            }
                        }
                    }
                }

                // Si encontramos un nombre de clase específico del mod (gracias a que importaste la API)
                if (actor.getClass().getName().toLowerCase().contains("raid")) {
                    isRaid = true;
                }

                if (isRaid && !actor.getPokemonList().isEmpty()) {
                    bossSpecies = actor.getPokemonList().get(0).getOriginalPokemon().getSpecies().getName();
                    break; // Ya encontramos al boss
                }
            }
        }

        if (!isRaid) return; // Si no es raid, no hacemos nada

        // 2. Recuperar el daño acumulado del Tracker
        float damageDealt = 0f;
        UUID battleId = battle.getBattleId();

        if (PokemonData.battleDamageTracker.containsKey(battleId)) {
            damageDealt = PokemonData.battleDamageTracker.get(battleId)
                    .getOrDefault(playerActor.getUuid(), 0f);
        }

        // 3. Crear y guardar
        RaidInteraction raidData = new RaidInteraction(
                battleId.toString(),
                playerActor.getUuid().toString(),
                bossSpecies,
                raidTier,
                result,
                battle.getPlayers().size(),
                damageDealt,
                biome,
                System.currentTimeMillis()
        );

        PokemonData.raidBuffer.add(raidData);
        System.out.println("BigData RAID: " + bossSpecies + " (Tier: " + raidTier + ") - Dmg: " + damageDealt);
    }
}