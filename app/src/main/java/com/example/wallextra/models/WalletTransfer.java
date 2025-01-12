package com.example.wallextra.models;

import java.util.Date;

public class WalletTransfer extends BaseTransaction{
    private Wallet sourceWallet;
    private Wallet destWallet;
    private String adminTransactionId;

    public WalletTransfer(String id, Wallet sourceWallet, Wallet destWallet, Long amount, String adminTransactionId, Date date) {
        super(id, amount, date);
        this.sourceWallet = sourceWallet;
        this.destWallet = destWallet;
        this.adminTransactionId = adminTransactionId;
    }

    public Wallet getSourceWallet() {
        return sourceWallet;
    }

    public Wallet getDestWallet() {
        return destWallet;
    }

    public String getAdminTransactionId() {
        return adminTransactionId;
    }
}
