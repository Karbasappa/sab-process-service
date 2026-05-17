package com.sabtok.process.edu.strategy;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {
   private final Map<String, PaymentStrategy> stringPaymentStrategyMap;

    public PaymentService(Map<String, PaymentStrategy> stringPaymentStrategyMap) {
        this.stringPaymentStrategyMap = stringPaymentStrategyMap;
    }

    public void processPayment(String type, double amount) {
        PaymentStrategy paymentStrategy = stringPaymentStrategyMap.get(type);
        if (paymentStrategy == null){
            throw new RuntimeException("No payment strategy available");
        }
        paymentStrategy.pay(amount);
    }

}
