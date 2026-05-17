package com.sabtok.process.edu.strategy;

import org.springframework.stereotype.Component;

@Component("CARD")
public class CardPaymentStrategy implements PaymentStrategy{
    @Override
    public void pay(double amount) {
        System.out.println("Card payment process");
    }

}
