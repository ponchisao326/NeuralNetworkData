package com.victorgponce.config;

public class NeuralNetworkConfig {
    // --- API Configuration ---
    // URL where Node.js server will be listening (http://yourdomain.com:3000/api/ingest)
    public String apiUrl = "http://localhost:3000/api/ingest";

    // Security Key to prevent anyone from sending false data to your API
    public String apiKey = "CAMBIA_ESTO_POR_UNA_CLAVE_SEGURA_123";

    // --- Performance ---
    public int batchIntervalSeconds = 10; // How often to send data
    public boolean debugMode = true; // Show console logs
    public NeuralNetworkConfig() {
        System.out.println("--- CARGANDO MODCONFIG CORRECTAMENTE ---");
    }
}