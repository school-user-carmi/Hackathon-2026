package com.example.hackathon_2026.Utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static JSONObject getJsonObjectOfProducts() {
        return null;
    }

    private static JSONArray getProductsArray(Context context) {
        try {
            InputStream is = context.getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            return new JSONArray(jsonString); // ⭐ שים לב – JSONArray

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getBarcodeByName(Context context, String name) {
        try {
            JSONArray products = getProductsArray(context);
            if (products == null) return null;

            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);

                String productName = product.getString("name");
                if (productName.equalsIgnoreCase(name)) {
                    return product.getString("barcode");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // לא נמצא
    }
    public static void getListOfProductsByName(String name) {

    }
}
