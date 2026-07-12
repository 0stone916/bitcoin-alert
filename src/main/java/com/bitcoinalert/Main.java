package com.bitcoinalert;

import com.bitcoinalert.fetcher.CoinGeckoFetcher;
import com.bitcoinalert.history.PriceHistoryStore;
import com.bitcoinalert.model.BitcoinPrice;
import com.bitcoinalert.notifier.TelegramNotifier;

import java.util.Optional;

public class Main {

    private static final double ALERT_THRESHOLD_PERCENT = -5.0;

    public static void main(String[] args) {
        TelegramNotifier notifier;
        try {
            notifier = new TelegramNotifier();
        } catch (Exception e) {
            System.err.println("TelegramNotifier 초기화 실패: " + e.getMessage());
            System.exit(1);
            return;
        }

        PriceHistoryStore history = new PriceHistoryStore();

        try {
            BitcoinPrice price = new CoinGeckoFetcher().fetch();
            System.out.printf("BTC: $%.2f | 24h: %.2f%%%n", price.getUsd(), price.getUsd24hChange());

            Optional<Double> previousUsd = history.loadPreviousUsd();
            if (previousUsd.isPresent()) {
                double hourlyChange = (price.getUsd() - previousUsd.get()) / previousUsd.get() * 100;
                System.out.printf("직전 실행 대비: %.2f%%%n", hourlyChange);
                if (hourlyChange <= ALERT_THRESHOLD_PERCENT) {
                    notifier.send(price, hourlyChange);
                }
            } else {
                System.out.println("이전 가격 기록 없음 - 이번 실행은 기록만 저장");
            }

            history.save(price.getUsd());
        } catch (Exception e) {
            System.err.println("Bitcoin 가격 조회 실패: " + e.getMessage());
            notifier.sendError("❌ Bitcoin Alert 오류", e.getMessage());
            System.exit(1);
        }
    }
}
