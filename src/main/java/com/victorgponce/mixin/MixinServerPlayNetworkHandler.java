package com.victorgponce.mixin;

import com.victorgponce.service.TelemetryFacade;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onCommandExecution", at = @At("HEAD"))
    private void onCommandExecution(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        // Extract command directly from the network packet
        String command = packet.command();

        // Send to Facade
        TelemetryFacade.getInstance().processCommand(
                this.player,
                "/" + command, // Slash-bar for consistency
                true
        );
    }

}
