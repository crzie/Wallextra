package com.example.wallextra.models;

import java.util.Date;

public class Transaction extends BaseTransaction {
    private String name;
    private TransactionType type;
    private Wallet wallet;

    public Transaction(String id, String name, TransactionType type, Long amount, Wallet wallet, Date date) {
        super(id, amount, date);
        this.name = name;
        this.type = type;
        this.wallet = wallet;
    }

    public String getName() {
        return name;
    }

    public TransactionType getType() {
        return type;
    }

    public Wallet getWallet() {
        return wallet;
    }
}