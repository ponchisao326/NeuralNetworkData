package com.victorgponce.controller;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.victorgponce.service.TelemetryFacade;

public class PokemonLifecycleController {

    public static void register() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(event ->
                TelemetryFacade.getInstance().processCapture(event.getPokemon(), event.getPlayer())
        );

        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.subscribe(event ->
                TelemetryFacade.getInstance().processRelease(event.getPokemon(), event.getPlayer())
        );
    }
}