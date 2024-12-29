package com.example.wallextra.models;

public class Wallet {
    private String id;
    private String name;
    private Long balance;
    private String ownerId;
    private String imageUrl;

    public Wallet(String id, String name, Long balance, String ownerId, String imageUrl) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getBalance() {
        return balance;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
