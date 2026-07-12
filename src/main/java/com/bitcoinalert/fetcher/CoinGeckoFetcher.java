package com.bitcoinalert.fetcher;

import com.bitcoinalert.model.BitcoinPrice;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CoinGeckoFetcher {

    private static final String URL =
            "https://api.coingecko.com/api/v3/simple/price" +
            "?ids=bitcoin&vs_currencies=usd,krw&include_24hr_change=true";

    private final HttpClient client = HttpClient.newHttpClient();

    public BitcoinPrice fetch() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("CoinGecko API returned " + response.statusCode() + ": " + response.body());
        }

        JSONObject root = new JSONObject(response.body());
        JSONObject bitcoin = root.getJSONObject("bitcoin");

        double usd = bitcoin.getDouble("usd");
        double krw = bitcoin.getDouble("krw");
        double change = bitcoin.getDouble("usd_24h_change");

        return new BitcoinPrice(usd, krw, change);
    }
}
