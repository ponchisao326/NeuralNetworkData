package com.victorgponce.model;

import com.google.gson.Gson;

public record GtsTransaction(
        String sellerUuid,
        String buyerUuid,
        String itemType,      // "POKEMON" or "ITEM"
        String description,   // Ej: "Charizard (Lvl 50, Shiny)" or "Diamond (x64)"
        double price,
        long listingDurationMs, // Time to be sold (Liquidity)
        long timestamp
) {
    private static final Gson gson = new Gson();

    public String toJson() {
        return gson.toJson(this);
    }
}