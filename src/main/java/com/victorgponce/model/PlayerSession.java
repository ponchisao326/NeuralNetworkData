package com.victorgponce.model;

import com.google.gson.Gson;

public record PlayerSession(
        String playerUuid,
        String playerName,
        String eventType, // "LOGIN" o "LOGOUT"
        String ipAddress,
        long timestamp
) {
    private static final Gson gson = new Gson();

    public String toJson() {
        return gson.toJson(this);
    }
}