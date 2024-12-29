package com.example.wallextra.models;

import java.util.Date;

public class Transaction {
    private String id;
    private String name;
    private TransactionType type;
    private Long amount;
    private Wallet wallet;
    private Date date;

    public Transaction(String id, String name, TransactionType type, Long amount, Wallet wallet, Date date) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TransactionType getType() {
        return type;
    }

    public Long getAmount() {
        return amount;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Date getDate() {
        return date;
    }
}
