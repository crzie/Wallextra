package com.example.wallextra.models;

import java.util.Date;

public abstract class BaseTransaction implements Comparable<BaseTransaction>{
    protected String id;
    protected Long amount;
    protected Date date;

    public BaseTransaction(String id, Long amount, Date date) {
        this.id = id;
        this.amount = amount;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public Long getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }


    @Override
    public int compareTo(BaseTransaction baseTransaction) {
        return baseTransaction.date.compareTo(this.date);
    }
}
