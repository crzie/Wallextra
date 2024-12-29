package com.example.wallextra.models;

import java.util.Date;

public class WalletTransfer {
    private String id;
    private Wallet sourceWallet;
    private Wallet destWallet;
    private Long amount;
    private String adminTransactionId;
    private Date date;

    public WalletTransfer(String id, Wallet sourceWallet, Wallet destWallet, Long amount, String adminTransactionId, Date date) {
        this.id = id;
        this.sourceWallet = sourceWallet;
        this.destWallet = destWallet;
        this.amount = amount;
        this.adminTransactionId = adminTransactionId;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public Wallet getSourceWallet() {
        return sourceWallet;
    }

    public Wallet getDestWallet() {
        return destWallet;
    }

    public Long getAmount() {
        return amount;
    }

    public String getAdminTransactionId() {
        return adminTransactionId;
    }

    public Date getDate() {
        return date;
    }
}
