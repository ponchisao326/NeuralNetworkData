package com.victorgponce.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.victorgponce.data_objects.CaughtPokemon;
import com.victorgponce.data_objects.ReleasedPokemon;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.victorgponce.cache.PokemonData.caughtPokemonBuffer;
import static com.victorgponce.cache.PokemonData.releasedPokemonBuffer;

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

                    player.sendMessage(Text.literal(string.toString()));

                    return 1;
                }));
    }
}
