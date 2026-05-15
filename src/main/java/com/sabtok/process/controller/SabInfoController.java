package com.sabtok.process.controller;

import com.sabtok.process.domain.PageActivity;
import com.sabtok.process.sbinforepo.SabInfoRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/sabinfo")
@RequiredArgsConstructor
public class SabInfoController {

    private final SabInfoRepo sabInfoRepo;

    @GetMapping("/activities")
    public List<PageActivity> getActities() {
        return sabInfoRepo.findAll();
    }
}
