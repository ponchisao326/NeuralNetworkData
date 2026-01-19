package com.victorgponce.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("NeuralNetworkData-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("neuralnetworkdata.json").toFile();

    private static NeuralNetworkConfig instance;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, NeuralNetworkConfig.class);
                LOGGER.info("Config loaded correctly");
            } catch (IOException e) {
                LOGGER.error("Error while loading configuration, using default values.", e);
                instance = new NeuralNetworkConfig();
            }
        } else {
            instance = new NeuralNetworkConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
            LOGGER.info("Configuración creada/guardada en: " + CONFIG_FILE.getAbsolutePath());
            LOGGER.info("Configuration created/saved on: " + CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Configuration could not be saved.", e);
        }
    }

    public static NeuralNetworkConfig get() {
        if (instance == null) load();
        return instance;
    }
}