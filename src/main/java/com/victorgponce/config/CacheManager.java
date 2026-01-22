package com.victorgponce.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.victorgponce.NeuralNetworkData;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CacheManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CACHE_FILE = Paths.get("config", "neuralnetworkdata_cache.json");

    // Flag to know if there are pending changes
    private static boolean isDirty = false;

    /**
     * Async loading at init
     */
    public static Set<String> loadProcessedEggs() {
        if (!Files.exists(CACHE_FILE)) {
            return new HashSet<>();
        }

        try (Reader reader = Files.newBufferedReader(CACHE_FILE)) {
            Type type = new TypeToken<HashSet<String>>() {}.getType();
            Set<String> loaded = GSON.fromJson(reader, type);
            NeuralNetworkData.LOGGER.info("Caché cargado: " + (loaded != null ? loaded.size() : 0) + " huevos procesados.");
            return loaded != null ? loaded : new HashSet<>();
        } catch (IOException e) {
            NeuralNetworkData.LOGGER.error("Error al cargar el caché de huevos: ", e);
            return new HashSet<>();
        }
    }

    /**
     * Async saving
     * Ideal to AutoSave while playing
     */
    public static void saveAsync(Set<String> data) {
        if (!isDirty) return; // If there is no data, don't save

        CompletableFuture.runAsync(() -> {
            saveSync(data);
        }).exceptionally(e -> {
            NeuralNetworkData.LOGGER.error("Error en guardado asíncrono: ", e);
            return null;
        });
    }

    /**
     * Async Saving
     * Obligatory to use this one when the server is shutting down (STOPPING).
     */
    public static void saveSync(Set<String> data) {
        try {
            // Create folder if it doesn't exists
            if (!Files.exists(CACHE_FILE.getParent())) {
                Files.createDirectories(CACHE_FILE.getParent());
            }

            // Use a .tmp file
            Path tempFile = Paths.get(CACHE_FILE.toString() + ".tmp");

            try (Writer writer = Files.newBufferedWriter(tempFile)) {
                GSON.toJson(data, writer);
            }

            // Atomic Replace
            Files.move(tempFile, CACHE_FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            isDirty = false;

        } catch (IOException e) {
            NeuralNetworkData.LOGGER.error("Error grave guardando caché en disco: ", e);
        }
    }

    public static void markDirty() {
        isDirty = true;
    }
}