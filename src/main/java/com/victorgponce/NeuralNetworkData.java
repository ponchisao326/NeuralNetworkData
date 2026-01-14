package com.victorgponce;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeuralNetworkData implements ModInitializer {
    public static final String MOD_ID = "neuralnetworkdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void onInitialize() {
        LOGGER.info("Starting Neural Network Data");

        // CobblemonEvents.POKEMON_CAPTURED.subscribe();
    }

}