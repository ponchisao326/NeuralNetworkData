package com.victorgponce.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.victorgponce.data_objects.BattleResult;
import com.victorgponce.data_objects.CaughtPokemon;
import com.victorgponce.data_objects.PokemonHatched;
import com.victorgponce.data_objects.ReleasedPokemon;
import com.victorgponce.data_objects.RaidInteraction; // [NUEVO IMPORT]
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.victorgponce.cache.PokemonData.*;

public class GetBufferedData implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("cobbleneuraldata")
                .requires(css -> css.hasPermissionLevel(4))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;

                    StringBuilder string = new StringBuilder();

                    string.append("------------------ onCaptured Data ------------------\n");
                    for (CaughtPokemon caughtPokemon : caughtPokemonBuffer) {
                        string.append(caughtPokemon.toJson()).append("\n");
                    }
                    string.append("------------------ onReleased Data ------------------\n");
                    for (ReleasedPokemon releasedPokemon : releasedPokemonBuffer) {
                        string.append(releasedPokemon.toJson()).append("\n");
                    }
                    string.append("------------------ onHatched Data ------------------\n");
                    for (PokemonHatched pokemonHatched : hatchedPokemonBuffer) {
                        string.append(pokemonHatched.toJson()).append("\n");
                    }
                    string.append("------------------ onBattle Data ------------------\n");
                    for (BattleResult battleResult : battleResultsBuffer) {
                        string.append(battleResult.toJson()).append("\n");
                    }

                    // --- SECCIÓN NUEVA: RAIDS ---
                    string.append("------------------ onRaid Data ------------------\n");
                    for (RaidInteraction raidInteraction : raidBuffer) {
                        string.append(raidInteraction.toJson()).append("\n");
                    }

                    string.append("----------------------------------------------------\n");

                    player.sendMessage(Text.literal(string.toString()));

                    return 1;
                }));
    }
}