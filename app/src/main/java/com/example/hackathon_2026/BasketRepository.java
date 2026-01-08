package com.example.hackathon_2026;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BasketRepository {

    private static final String PREF_NAME = "SmartCartPrefs";
    private static final String KEY_BASKETS = "saved_baskets";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public BasketRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveBasket(Basket basket) {
        List<Basket> baskets = getAllBaskets();
        // Remove existing basket with the same ID if it exists (update scenario)
        baskets.removeIf(b -> b.id.equals(basket.id));
        baskets.add(basket);
        saveBasketsList(baskets);
    }

    public Basket getBasket(String basketId) {
        List<Basket> baskets = getAllBaskets();
        for (Basket b : baskets) {
            if (b.id.equals(basketId)) {
                return b;
            }
        }
        return null;
    }

    public List<Basket> getAllBaskets() {
        String json = sharedPreferences.getString(KEY_BASKETS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Basket>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void deleteBasket(String basketId) {
        List<Basket> baskets = getAllBaskets();
        baskets.removeIf(b -> b.id.equals(basketId));
        saveBasketsList(baskets);
    }

    private void saveBasketsList(List<Basket> baskets) {
        String json = gson.toJson(baskets);
        sharedPreferences.edit().putString(KEY_BASKETS, json).apply();
    }
}
