package com.victorgponce.view;

import com.mojang.brigadier.CommandDispatcher;
import com.victorgponce.model.*;
import com.victorgponce.repository.DataRepository;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GetBufferedData implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("cobbleneuraldata")
                .requires(css -> css.hasPermissionLevel(4))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;

                    DataRepository repo = DataRepository.getInstance();
                    StringBuilder string = new StringBuilder();

                    string.append("------------------ onCaptured Data ------------------\n");
                    for (CaughtPokemon d : repo.getCaughtBuffer()) string.append(d.toJson()).append("\n");

                    string.append("------------------ onReleased Data ------------------\n");
                    for (ReleasedPokemon d : repo.getReleasedBuffer()) string.append(d.toJson()).append("\n");

                    string.append("------------------ onHatched Data ------------------\n");
                    for (PokemonHatched d : repo.getHatchedBuffer()) string.append(d.toJson()).append("\n");

                    string.append("------------------ onBattle Data ------------------\n");
                    for (BattleResult d : repo.getBattleResultBuffer()) string.append(d.toJson()).append("\n");

                    string.append("------------------ onRaid Data ------------------\n");
                    for (RaidInteraction d : repo.getRaidBuffer()) string.append(d.toJson()).append("\n");

                    string.append("------------------ onGts Data -----------------\n");
                    for (GtsTransaction d : repo.getGtsBuffer()) string.append(d.toJson()).append("\n");

                    string.append("------------------ onSession Data ------------------\n");
                    for (PlayerSession d : repo.getSessionBuffer()) string.append(d.toJson()).append("\n");

                    string.append("------------------ Session Snapshots (5min) ------------------\n");
                    for (SessionSnapshot d : repo.getSnapshotBuffer()) string.append(d.toJson()).append("\n");

                    player.sendMessage(Text.literal(string.toString()));
                    return 1;
                }));
    }
}