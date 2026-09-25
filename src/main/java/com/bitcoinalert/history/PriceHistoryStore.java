package com.bitcoinalert.history;

import org.json.JSONException;
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
        if (!Files.exists(FILE)) {
            return Optional.empty();
        }
        try {
            JSONObject json = new JSONObject(Files.readString(FILE));
            return Optional.of(json.getDouble("usd"));
        } catch (IOException e) {
            System.err.println("이전 가격 파일 읽기 실패: " + e.getMessage());
            return Optional.empty();
        } catch (JSONException e) {
            System.err.println("이전 가격 파일 형식 손상: " + e.getMessage());
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
