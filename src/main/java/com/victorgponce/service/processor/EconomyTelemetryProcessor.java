package com.victorgponce.service.processor;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.item.ItemStack;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.Listing.ItemListing;
import org.pokesplash.gts.Listing.Listing;
import org.pokesplash.gts.Listing.PokemonListing;
import org.pokesplash.gts.api.event.events.PurchaseEvent;
import com.victorgponce.model.GtsTransaction;

import static com.victorgponce.NeuralNetworkData.LOGGER;

public class EconomyTelemetryProcessor {

    public GtsTransaction analyzeTransaction(PurchaseEvent event) {
        Listing<?> listing = event.getProduct();
        long now = System.currentTimeMillis();

        String sellerUuid = listing.getSellerUuid().toString();
        String buyerUuid = event.getBuyer().toString();
        double price = listing.getPrice();

        long durationMs = -1;
        double configDurationHours = Gts.config.getListingDuration();

        if (listing.getEndTime() != -1 && configDurationHours > 0) {
            long maxDurationMs = (long) (configDurationHours * 3600000L);
            long remainingTime = listing.getEndTime() - now;
            durationMs = maxDurationMs - remainingTime;
            if (durationMs < 0) durationMs = 0;
        }

        String type;
        String description;

        if (listing instanceof PokemonListing pokemonListing) {
            type = "POKEMON";
            Pokemon pokemon = pokemonListing.getListing();
            description = pokemon.getSpecies().getName() + " Lvl" + pokemon.getLevel() + (pokemon.getShiny() ? " (Shiny)" : "");
        } else if (listing instanceof ItemListing itemListing) {
            type = "ITEM";
            ItemStack itemStack = itemListing.getListing();
            description = itemStack.getName().getString() + " (x" + itemStack.getCount() + ")";
        } else {
            type = "UNKNOWN";
            description = listing.getListingName();
        }

        LOGGER.info("BigData ECONOMY: Sold {} for {}", description, price);

        return new GtsTransaction(
                sellerUuid,
                buyerUuid,
                type,
                description,
                price,
                durationMs,
                now
        );
    }
}