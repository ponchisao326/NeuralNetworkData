package com.victorgponce.service.processor;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.model.CaughtPokemon;
import com.victorgponce.model.PokemonHatched;
import com.victorgponce.model.ReleasedPokemon;
import com.victorgponce.utils.BigDataUtils;
import com.victorgponce.utils.DataUtils;
import com.victorgponce.utils.TelemetryUtils;
import net.minecraft.server.network.ServerPlayerEntity;

public class PokemonTelemetryProcessor {

    public CaughtPokemon createCaught(Pokemon pokemon, ServerPlayerEntity player) {
        return new CaughtPokemon(
                player != null ? player.getUuidAsString() : "unknown",
                pokemon.getUuid(),
                pokemon.getSpecies().getName(),
                pokemon.getLevel(),
                pokemon.getNature().getName().getPath(),
                pokemon.getAbility().getName(),
                pokemon.getShiny(),
                DataUtils.getTotalIvs(pokemon),
                pokemon.getCaughtBall().getName().getPath(),
                TelemetryUtils.getBiomeName(player),
                TelemetryUtils.getWorldName(player),
                BigDataUtils.getNationalDexPercentage(player),
                System.currentTimeMillis()
        );
    }

    public ReleasedPokemon createReleased(Pokemon pokemon, ServerPlayerEntity player) {
        String playerUuid = (player != null) ? player.getUuidAsString() : "unknown";
        String playerName = (player != null) ? player.getName().getString() : "System";

        return new ReleasedPokemon(
                pokemon.getUuid(),
                pokemon.getSpecies().getName(),
                pokemon.getLevel(),
                pokemon.getShiny(),
                DataUtils.getTotalIvs(pokemon),
                playerName,
                TelemetryUtils.getBiomeName(player),
                TelemetryUtils.getWorldName(player),
                playerUuid,
                System.currentTimeMillis(),
                0
        );
    }

    public PokemonHatched createHatched(Pokemon pokemon, ServerPlayerEntity player) {
        return new PokemonHatched(
                pokemon.getUuid(),
                pokemon.getSpecies().getName(),
                pokemon.getShiny(),
                DataUtils.getTotalIvs(pokemon),
                pokemon.getAbility().getName(),
                pokemon.getNature().getName().getPath(),
                pokemon.getCaughtBall().getName().getPath(),
                player != null ? player.getUuidAsString() : "unknown",
                TelemetryUtils.getWorldName(player),
                TelemetryUtils.getBiomeName(player),
                System.currentTimeMillis()
        );
    }
}