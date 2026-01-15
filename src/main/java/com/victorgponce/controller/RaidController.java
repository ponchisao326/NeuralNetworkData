package com.victorgponce.controller;

import com.necro.raid.dens.common.events.RaidEvents;
import com.victorgponce.service.TelemetryFacade;

public class RaidController {
    public static void register() {
        RaidEvents.RAID_END.subscribe(event ->
                TelemetryFacade.getInstance().processRaidEnd(event)
        );
    }
}