package com.bitcoinalert.history;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

public class PriceHistoryStore {

    private static final Path FILE = Paths.get("data", "last-price.json");

    public Optional<Double> loadPreviousUsd() {
        try {
            if (!Files.exists(FILE)) {
                return Optional.empty();
            }
            JSONObject json = new JSONObject(Files.readString(FILE));
            return Optional.of(json.getDouble("usd"));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void save(double usd) throws IOException {
        Files.createDirectories(FILE.getParent());
        JSONObject json = new JSONObject();
        json.put("usd", usd);
        json.put("timestamp", Instant.now().toString());
        Files.writeString(FILE, json.toString(2));
    }
}
