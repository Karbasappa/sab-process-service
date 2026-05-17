package com.sabtok.process.edu.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {
    UPI,
    CARD;
    public static final String UPI_VALUE = "UPI";
    public String getStringValue(PaymentType paymentType) {
        return paymentType.name();
    }
}
