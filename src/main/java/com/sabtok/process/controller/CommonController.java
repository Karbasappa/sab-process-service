package com.sabtok.process.controller;

import com.sabtok.process.entity.AlertMessage;
import com.sabtok.process.repository.AlertMessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Value("${env.name:default}")
    private String environment;

    @GetMapping("/env")
    public String getEnvironment() {
        return this.environment;
    }

    @Autowired
    private AlertMessageRepo alertMessageRepo;

    @Cacheable("my-test-cache")
    @GetMapping("/data")
    public String getData(String id) {
        // This result will now be stored in "my-test-cache"
        return "Some Data";
    }

    @Cacheable("user-cache")
    @GetMapping("/messages")
    public List<AlertMessage> getAlertMessages() {
        return alertMessageRepo.findAll();
    }

}
