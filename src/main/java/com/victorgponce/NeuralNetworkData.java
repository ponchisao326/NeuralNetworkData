package com.victorgponce;

import com.victorgponce.controller.*;
import com.victorgponce.view.GetBufferedData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeuralNetworkData implements ModInitializer {
    public static final String MOD_ID = "neuralnetworkdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Starting Neural Network Data");

        // Register Controllers
        PokemonLifecycleController.register();
        BattleController.register();
        RaidController.register();
        DamageTrackerController.register();
        EconomyController.register();
        SessionController.register();
        SessionHeartbeatController.register();
        BehaviorController.register();

        // Register Commands
        CommandRegistrationCallback.EVENT.register(new GetBufferedData());
    }
}