package com.example.wallextra.models;

import androidx.annotation.NonNull;

public enum TransactionType {
    INCOME("Income"),
    EXPENSE("Expense");

    private final String value;

    private TransactionType(String value) {
        this.value = value;
    }

    @NonNull
    public String toString() {
        return value;
    }

    public static TransactionType from(String value) {
        for (TransactionType type : TransactionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid transaction type: " + value);
    }
}
