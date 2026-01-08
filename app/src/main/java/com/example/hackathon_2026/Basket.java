package com.example.hackathon_2026;

import java.util.List;

public class Basket {
    public String id;
    public String name;
    public List<Product> products;
    public long timestamp;

    public Basket(String id, String name, List<Product> products) {
        this.id = id;
        this.name = name;
        this.products = products;
        this.timestamp = System.currentTimeMillis();
    }
}
