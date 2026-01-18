package com.victorgponce.model;

import com.google.gson.Gson;

public record CommandUsage(
        String playerUuid,
        String command, // Ej: "home", "gts sell", "rtp"
        boolean isSuccess, // if command worked or not
        long timestamp
) {
    private static final Gson gson = new Gson();
    public String toJson() { return gson.toJson(this); }
}