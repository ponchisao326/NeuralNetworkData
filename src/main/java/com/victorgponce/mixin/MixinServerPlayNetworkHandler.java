package com.victorgponce.mixin;

import com.victorgponce.service.TelemetryFacade;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {

    @Shadow public ServerPlayerEntity player;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "onCommandExecution", at = @At("HEAD"))
    private void onCommandExecution(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        // Extract command directly from the network packet
        String command = packet.command();
        LOGGER.info("Info command from NeuralNetworkData: {}", command);

        // Ignore view data command
        if (command.startsWith("cobbleneuraldata")) return;

        // Ignore Worldedit
        if (command.startsWith("/") ||
            command.startsWith("wand") ||
            command.startsWith("pos1") ||
            command.startsWith("pos2")) return;

        // Ignore massive construction commands (Admin Staff)
        if (command.startsWith("fill") || command.startsWith("setblock") || command.startsWith("clone")) return;

        // Ignore empty commands or too shorts (Typos)
        if (command.length() < 2) return;

        // Send to Facade
        TelemetryFacade.getInstance().processCommand(
                this.player,
                "/" + command, // Slash-bar for consistency
                true
        );
    }

}
