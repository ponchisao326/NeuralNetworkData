package com.victorgponce;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.victorgponce.commands.GetBufferedData;
import com.victorgponce.events.OnPokemonCaptured;
import com.victorgponce.events.OnPokemonRelease;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeuralNetworkData implements ModInitializer {
    public static final String MOD_ID = "neuralnetworkdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void onInitialize() {
        LOGGER.info("Starting Neural Network Data");

        // Events
        CobblemonEvents.POKEMON_CAPTURED.subscribe(OnPokemonCaptured::onCapturedEvent);
        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.subscribe(OnPokemonRelease::onReleasedEvent);

        // Commands
        CommandRegistrationCallback.EVENT.register(new GetBufferedData());
    }

}