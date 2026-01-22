package com.victorgponce.service.processor;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.model.CaughtPokemon;
import com.victorgponce.model.Ivs;
import com.victorgponce.model.PokemonBred;
import com.victorgponce.model.ReleasedPokemon;
import com.victorgponce.repository.DataRepository;
import com.victorgponce.utils.BigDataUtils;
import com.victorgponce.utils.DataUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.victorgponce.utils.TelemetryUtils.getBiomeName;
import static com.victorgponce.utils.TelemetryUtils.getWorldName;

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
                getBiomeName(player),
                getWorldName(player),
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
                getBiomeName(player),
                getWorldName(player),
                playerUuid,
                System.currentTimeMillis(),
                0
        );
    }

    public List<PokemonBred> createScanInventoryForEggs(ServerPlayerEntity player) {
        // Creamos una lista para devolver (puede haber 0, 1 o 5 huevos nuevos)
        List<PokemonBred> foundEggs = new ArrayList<>();

        // Recorremos el inventario principal
        for (ItemStack stack : player.getInventory().main) {

            // 1. Filtro rápido
            if (stack.isEmpty() || stack.getItem() != Items.TURTLE_EGG) continue;

            // 2. Extraemos el NBT
            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (component == null) continue;

            NbtCompound nbt = component.copyNbt();

            // Si no tiene especie, no es un huevo Pokémon
            if (!nbt.contains("species", NbtElement.STRING_TYPE)) continue;

            // 3. CORRECCIÓN DE UUID vs EGG_ID
            String pokemonUuid = null;

            if (nbt.contains("egg_id")) {
                pokemonUuid = nbt.getString("egg_id");
            } else if (nbt.contains("uuid")) {
                pokemonUuid = nbt.getUuid("uuid").toString();
            }

            if (pokemonUuid == null) continue;

            // 4. EL CANDADO: ¿Ya hemos contado este huevo antes?
            // El Processor SOLO LEE del repo para saber si es nuevo, NO escribe.
            if (DataRepository.getInstance().isEggProcessed(pokemonUuid)) {
                continue;
            }

            // 5. Si es nuevo, extraemos los datos
            String species = nbt.getString("species");
            boolean isShiny = nbt.contains("shiny") && nbt.getBoolean("shiny");
            String ability = nbt.contains("ability") ? nbt.getString("ability") : "unknown";
            String nature = nbt.contains("nature") ? nbt.getString("nature") : "unknown";
            String ball = nbt.contains("pokeball") ? nbt.getString("pokeball") : "poke_ball";

            Ivs ivsData;
            if (nbt.contains("ivs", NbtElement.INT_ARRAY_TYPE)) {
                int[] ivsArray = nbt.getIntArray("ivs");
                if (ivsArray.length >= 6) {
                    ivsData = new Ivs(ivsArray[0], ivsArray[1], ivsArray[2], ivsArray[3], ivsArray[4], ivsArray[5]);
                } else {
                    ivsData = new Ivs(0,0,0,0,0,0);
                }
            } else {
                ivsData = new Ivs(0,0,0,0,0,0);
            }

            long currentTimestamp = System.currentTimeMillis();
            String biome = getBiomeName(player);
            String world = getWorldName(player);

            // Añadimos a la lista temporal
            foundEggs.add(new PokemonBred(
                    pokemonUuid,
                    species,
                    isShiny,
                    ivsData,
                    ability,
                    nature,
                    ball,
                    player.getUuidAsString(),
                    world,
                    biome,
                    currentTimestamp
            ));
        }

        // Devolvemos la lista al Facade
        return foundEggs;
    }
}