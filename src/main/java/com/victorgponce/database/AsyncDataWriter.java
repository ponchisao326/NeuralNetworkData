package com.victorgponce.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.victorgponce.config.ConfigManager;
import com.victorgponce.config.NeuralNetworkConfig;
import com.victorgponce.model.*;
import com.victorgponce.repository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncDataWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger("NeuralNetworkData-API");
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // HTTP client
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Gson gson = new Gson();

    public static void start() {
        int interval = ConfigManager.get().batchIntervalSeconds;

        scheduler.scheduleAtFixedRate(AsyncDataWriter::flushBuffers, interval, interval, TimeUnit.SECONDS);

        // AutoSave task (Every 5 mins)
        scheduler.scheduleAtFixedRate(() -> {
            DataRepository.getInstance().runAutosave();
        }, 5, 5, TimeUnit.MINUTES);

        LOGGER.info("API Writer & Cache Autosave Started.");
    }
    public static void stop() {
        flushBuffers();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private static void flushBuffers() {
        DataRepository repo = DataRepository.getInstance();
        if (isEmpty(repo)) return;

        List<JsonObject> batch = new ArrayList<>();

        // --- Grab all buffer data ---

        // LifeCycle
        drainToBatch(repo.getCaughtBuffer(), batch, "LIFECYCLE", "POKEMON_CAPTURED");
        drainToBatch(repo.getReleasedBuffer(), batch, "LIFECYCLE", "POKEMON_RELEASED");
        drainToBatch(repo.getBredBuffer(), batch, "LIFECYCLE", "POKEMON_BRED");

        // Combat
        drainToBatch(repo.getBattleResultBuffer(), batch, "COMBAT", "BATTLE_END");
        drainToBatch(repo.getRaidBuffer(), batch, "COMBAT", "RAID_INTERACTION");

        // Economy
        drainToBatch(repo.getGtsBuffer(), batch, "ECONOMY", "GTS_TRANSACTION");

        // Sessions
        drainToBatch(repo.getSessionBuffer(), batch, "SESSION", "SESSION_EVENT");
        drainToBatch(repo.getSnapshotBuffer(), batch, "SESSION", "SESSION_SNAPSHOT");

        // Behavior
        drainToBatch(repo.getCommandBuffer(), batch, "BEHAVIOR", "COMMAND_USAGE");
        drainToBatch(repo.getDeathBuffer(), batch, "BEHAVIOR", "PLAYER_DEATH");

        // --- Send if there is any data ---
        if (!batch.isEmpty()) {
            sendToApi(batch);
        }
    }

    /**
     * Extract queue elements, it converts them to JSON standard and adds them to the batch.
     */
    private static <T> void drainToBatch(Queue<T> queue, List<JsonObject> batch, String category, String actionOverride) {
        while (!queue.isEmpty()) {
            T item = queue.poll();

            // Convert the record into a JSON tree
            JsonElement jsonContext = gson.toJsonTree(item);
            JsonObject contextObj = jsonContext.getAsJsonObject();

            // Extract key metadata from the context object
            String uuid = extractUuid(contextObj);
            String biome = contextObj.has("biome") ? contextObj.get("biome").getAsString() : "unknown";
            String world = contextObj.has("world") ? contextObj.get("world").getAsString() : "minecraft:overworld";

            String finalAction = actionOverride;
            if (actionOverride.equals("SESSION_EVENT") && contextObj.has("eventType")) {
                finalAction = "SESSION_" + contextObj.get("eventType").getAsString();
            }

            // Construct final object that will be sent to the API
            JsonObject payload = new JsonObject();
            payload.addProperty("server_id", ConfigManager.get().serverId);
            payload.addProperty("player_uuid", uuid);
            payload.addProperty("category", category);
            payload.addProperty("action_type", finalAction);
            payload.add("context_data", jsonContext);
            payload.addProperty("world", world);
            payload.addProperty("biome", biome);

            batch.add(payload);
        }
    }

    private static String extractUuid(JsonObject obj) {
        if (obj.has("playerUuid")) return obj.get("playerUuid").getAsString();
        if (obj.has("buyerUuid")) return obj.get("buyerUuid").getAsString(); // Caso GTS
        return "system";
    }

    private static void sendToApi(List<JsonObject> batch) {
        try {
            String jsonBody = gson.toJson(batch);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ConfigManager.get().apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + ConfigManager.get().apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            LOGGER.error("API Error: " + response.statusCode() + " - " + response.body());
                        } else if (ConfigManager.get().debugMode) {
                            LOGGER.info("Enviados " + batch.size() + " eventos a la API.");
                        }
                    })
                    .exceptionally(e -> {
                        LOGGER.error("Fallo de red al enviar a API: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            LOGGER.error("Error construyendo petición HTTP", e);
        }
    }

    private static boolean isEmpty(DataRepository repo) {
        return repo.getCaughtBuffer().isEmpty() && repo.getReleasedBuffer().isEmpty() &&
                repo.getBredBuffer().isEmpty() && repo.getBattleResultBuffer().isEmpty() &&
                repo.getRaidBuffer().isEmpty() && repo.getGtsBuffer().isEmpty() &&
                repo.getSessionBuffer().isEmpty() && repo.getSnapshotBuffer().isEmpty() &&
                repo.getCommandBuffer().isEmpty() && repo.getDeathBuffer().isEmpty();
    }
}