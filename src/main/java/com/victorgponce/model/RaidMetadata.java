package com.victorgponce.model;

import java.util.UUID;

public record RaidMetadata(
        String bossSpecies,
        String raidTier,
        UUID bossUuid
) {}