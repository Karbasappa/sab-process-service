package com.sabtok.process.edu.strategy;

import org.springframework.stereotype.Service;

@Service
public interface PaymentStrategy {

    void pay(double amount);
}
