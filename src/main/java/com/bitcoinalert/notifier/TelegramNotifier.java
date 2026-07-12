package com.bitcoinalert.notifier;

import com.bitcoinalert.model.BitcoinPrice;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TelegramNotifier {

    private static final String API_URL = "https://api.telegram.org/bot%s/sendMessage";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final NumberFormat USD_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    private static final NumberFormat KRW_FORMAT = NumberFormat.getNumberInstance(Locale.KOREA);

    private final String token;
    private final String channelId;
    private final HttpClient client = HttpClient.newHttpClient();

    public TelegramNotifier() {
        this.token = System.getenv("TELEGRAM_BOT_TOKEN");
        this.channelId = System.getenv("TELEGRAM_CHANNEL_ID");
        if (token == null || token.isBlank()) throw new IllegalStateException("TELEGRAM_BOT_TOKEN is not set");
        if (channelId == null || channelId.isBlank()) throw new IllegalStateException("TELEGRAM_CHANNEL_ID is not set");
    }

    public void send(BitcoinPrice price, double hourlyChange) throws Exception {
        String message = buildMessage(price, hourlyChange);
        sendRaw(message);
        System.out.println("Bitcoin alert sent.");
    }

    public void sendError(String context, String errorMessage) {
        try {
            String text = context + (errorMessage != null && !errorMessage.isEmpty() ? "\n" + errorMessage : "");
            sendRaw(text);
        } catch (Exception e) {
            System.err.println("[Telegram] 에러 알림 전송 실패: " + e.getMessage());
        }
    }

    private void sendRaw(String message) throws Exception {
        String body = "chat_id=" + URLEncoder.encode(channelId, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
                + "&parse_mode=HTML";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(API_URL, token)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Telegram API returned " + response.statusCode() + ": " + response.body());
        }
    }

    private String buildMessage(BitcoinPrice price, double hourlyChange) {
        String kstTime = ZonedDateTime.now(KST)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return String.format(
                "<b>🚨 Bitcoin 급락 알림</b>\n\n" +
                "💰 <b>현재가</b>\n" +
                "• USD: <b>$%s</b>\n" +
                "• KRW: <b>₩%s</b>\n\n" +
                "📉 <b>1시간 전 대비: %.2f%%</b>\n" +
                "(24시간 변동: %.2f%%)\n\n" +
                "<i>%s KST | Bitcoin Alert</i>",
                USD_FORMAT.format(price.getUsd()),
                KRW_FORMAT.format((long) price.getKrw()),
                hourlyChange,
                price.getUsd24hChange(),
                kstTime
        );
    }
}
