package com.victorgponce.events;

import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.data_objects.Ivs;
import com.victorgponce.data_objects.ReleasedPokemon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.victorgponce.cache.PokemonData.releasedPokemonBuffer;

public class OnPokemonRelease {

    public static void onReleasedEvent(ReleasePokemonEvent event) {
        Pokemon pokemonReleased = event.getPokemon();
        ServerPlayerEntity player = event.getPlayer();
        long currentTimestamp = System.currentTimeMillis();

        // Basic data
        String species = pokemonReleased.getSpecies().getName();
        int level = pokemonReleased.getLevel();
        boolean shiny = pokemonReleased.getShiny();

        // Ivs (Vital to know if release was to delete garbage or not)
        Ivs ivs = new Ivs(
                pokemonReleased.getIvs().get(Stats.HP),
                pokemonReleased.getIvs().get(Stats.ATTACK),
                pokemonReleased.getIvs().get(Stats.DEFENCE),
                pokemonReleased.getIvs().get(Stats.SPECIAL_ATTACK),
                pokemonReleased.getIvs().get(Stats.SPECIAL_DEFENCE),
                pokemonReleased.getIvs().get(Stats.SPEED)
        );

        // Obtain player (null if deleted by command)
        String playerUuid = "unknown";
        if (event.getPlayer() != null) {
            playerUuid = event.getPlayer().getUuidAsString();
        }

        // Origin
        // It could be null if its deleted by command so we instantiate first
        String playerName = "System";
        String biome = "Unknown";
        if (player != null) {
            // Obtain player name
            playerName = player.getName().getString();

            // Obtain the block where the player is at
            BlockPos blockPos = player.getBlockPos();
            // Get the world
            World world = player.getWorld();
            // Get the biome at the block position
            biome = world.getBiome(blockPos)
                    .getKey()
                    .map(key -> key.getValue().toString())
                    .orElse("Unknown");
        }

        ReleasedPokemon releasedPokemon = new ReleasedPokemon(
                pokemonReleased.getUuid(),
                species,
                level,
                shiny,
                ivs,
                playerName,
                biome,
                playerUuid,
                currentTimestamp,
                0);

        releasedPokemonBuffer.add(releasedPokemon);
    }

}
