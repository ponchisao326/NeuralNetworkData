package com.victorgponce.data_objects;

import java.util.UUID;

public record RaidMetadata(
        String bossSpecies,
        String raidTier,
        UUID bossUuid
) {}