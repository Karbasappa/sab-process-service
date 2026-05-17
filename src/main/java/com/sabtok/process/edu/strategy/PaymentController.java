package com.sabtok.process.edu.strategy;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/pay/{type}/{amount}")
    public void processPayment(@PathVariable("type") String type, @PathVariable("amount") Double amount){
        paymentService.processPayment(type,amount);
    }
}
