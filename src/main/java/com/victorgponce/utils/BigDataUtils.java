package com.victorgponce.utils;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokedex.Dexes;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokedex.PokedexManager;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class BigDataUtils {

    public static final Identifier NAT_DEX_ID = Identifier.of("cobblemon", "national");

    public static float getNationalDexPercentage(ServerPlayerEntity player) {

        // Obtain Dexes map
        var dexEntryMap = Dexes.INSTANCE.getDexEntryMap();

        if (!dexEntryMap.containsKey(NAT_DEX_ID)) {
            return 0.0f;
        }

        var nationalDex = dexEntryMap.get(NAT_DEX_ID);
        var entries = nationalDex.getEntries();

        // Calculate total implemented
        long totalImplemented = entries.stream()
                .filter(entry -> {
                    var species = PokemonSpecies.INSTANCE.getByIdentifier(entry.getSpeciesId());
                    return species != null && species.getImplemented();
                })
                .count();

        if (totalImplemented == 0) return 0.0f;

        // Calculate player progress
        PokedexManager playerData = Cobblemon.INSTANCE.getPlayerDataManager().getPokedexData(player);

        long playerCaughtCount = entries.stream()
                .filter(entry -> {
                    // We obtain the current progress
                    PokedexEntryProgress status = playerData.getKnowledgeForSpecies(entry.getSpeciesId());

                    // We compare, if actual state is >= CAUGHT, count it
                    return status.ordinal() >= PokedexEntryProgress.CAUGHT.ordinal();
                })
                .count();

        // Return percentage
        return ((float) playerCaughtCount / totalImplemented) * 100.0f;
    }
}