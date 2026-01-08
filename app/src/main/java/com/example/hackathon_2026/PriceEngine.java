package com.example.hackathon_2026;

import java.util.ArrayList;
import java.util.List;

public class PriceEngine {

    public static class BestDeal {
        public String storeName;
        public double price;

        public BestDeal(String storeName, double price) {
            this.storeName = storeName;
            this.price = price;
        }
    }

    public static class BasketResult {
        public String storeName;
        public double totalPrice;
        public List<String> missingItems;

        public BasketResult(String storeName, double totalPrice, List<String> missingItems) {
            this.storeName = storeName;
            this.totalPrice = totalPrice;
            this.missingItems = missingItems;
        }
    }

    public static BestDeal calculateCheapest(Product product) {
        if (product == null || product.prices == null) return null;

        double cheapest = Double.MAX_VALUE;
        String cheapestStore = "לא נמצא";

        // בדיקה לשופרסל
        if (product.prices.shufersalDeal != null && product.prices.shufersalDeal < cheapest) {
            cheapest = product.prices.shufersalDeal;
            cheapestStore = "שופרסל דיל";
        }

        // בדיקה לרמי לוי
        if (product.prices.ramiLevi != null && product.prices.ramiLevi < cheapest) {
            cheapest = product.prices.ramiLevi;
            cheapestStore = "רמי לוי";
        }

        // בדיקה ליוחננוף
        if (product.prices.yohananof != null && product.prices.yohananof < cheapest) {
            cheapest = product.prices.yohananof;
            cheapestStore = "יוחננוף";
        }

        if (cheapest == Double.MAX_VALUE) return null;

        return new BestDeal(cheapestStore, cheapest);
    }

    public static BasketResult calculateBasketBestDeal(List<Product> products) {
        if (products == null || products.isEmpty()) return null;

        double shufersalTotal = 0;
        double ramiLeviTotal = 0;
        double yohananofTotal = 0;

        List<String> shufersalMissing = new ArrayList<>();
        List<String> ramiLeviMissing = new ArrayList<>();
        List<String> yohananofMissing = new ArrayList<>();

        for (Product product : products) {
            if (product.prices == null) {
                shufersalMissing.add(product.name);
                ramiLeviMissing.add(product.name);
                yohananofMissing.add(product.name);
                continue;
            }

            if (product.prices.shufersalDeal != null) {
                shufersalTotal += product.prices.shufersalDeal;
            } else {
                shufersalMissing.add(product.name);
            }

            if (product.prices.ramiLevi != null) {
                ramiLeviTotal += product.prices.ramiLevi;
            } else {
                ramiLeviMissing.add(product.name);
            }

            if (product.prices.yohananof != null) {
                yohananofTotal += product.prices.yohananof;
            } else {
                yohananofMissing.add(product.name);
            }
        }

        // Determine the winner logic:
        // Priority 1: Fewest missing items.
        // Priority 2: Lowest price.

        String bestStore = "Shufersal Deal";
        double bestPrice = shufersalTotal;
        List<String> bestMissing = shufersalMissing;

        // Check Rami Levi
        if (ramiLeviMissing.size() < bestMissing.size()) {
            bestStore = "Rami Levi";
            bestPrice = ramiLeviTotal;
            bestMissing = ramiLeviMissing;
        } else if (ramiLeviMissing.size() == bestMissing.size()) {
            if (ramiLeviTotal < bestPrice) {
                bestStore = "Rami Levi";
                bestPrice = ramiLeviTotal;
                bestMissing = ramiLeviMissing;
            }
        }

        // Check Yohananof
        if (yohananofMissing.size() < bestMissing.size()) {
            bestStore = "Yohananof";
            bestPrice = yohananofTotal;
            bestMissing = yohananofMissing;
        } else if (yohananofMissing.size() == bestMissing.size()) {
            if (yohananofTotal < bestPrice) {
                bestStore = "Yohananof";
                bestPrice = yohananofTotal;
                bestMissing = yohananofMissing;
            }
        }

        // Hebrew names for consistency
        if (bestStore.equals("Shufersal Deal")) bestStore = "שופרסל דיל";
        if (bestStore.equals("Rami Levi")) bestStore = "רמי לוי";
        if (bestStore.equals("Yohananof")) bestStore = "יוחננוף";

        return new BasketResult(bestStore, bestPrice, bestMissing);
    }
}
