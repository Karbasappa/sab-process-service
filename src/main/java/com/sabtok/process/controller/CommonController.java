package com.sabtok.process.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Cacheable("my-test-cache")
    @GetMapping("/data")
    public String getData(String id) {
        // This result will now be stored in "my-test-cache"
        return "Some Data";
    }
}
