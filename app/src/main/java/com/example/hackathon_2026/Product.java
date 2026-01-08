package com.example.hackathon_2026;

import com.google.gson.annotations.SerializedName;

public class Product {
    public String barcode;
    public String name;
    public Prices prices; // אובייקט המחירים הפנימי

    public static class Prices {
        @SerializedName("Shufersal Deal")
        public Double shufersalDeal;

        @SerializedName("Rami Levi")
        public Double ramiLevi;

        @SerializedName("Yohananof")
        public Double yohananof;
    }
}