package com.sabtok.process.edu.strategy;

import org.springframework.stereotype.Component;

@Component("UPI")
public class UpiPaymentStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("UPI payment strategy");
    }

}
