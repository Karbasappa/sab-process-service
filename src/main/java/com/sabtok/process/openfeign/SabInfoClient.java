package com.sabtok.process.openfeign;

import com.sabtok.process.dto.BookRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;


@FeignClient(name = "sab-info-service", url = "${sab.info.service.url}")
public interface SabInfoClient {

    @GetMapping("/book/all")
    Set<BookRecord> getAllBooks();
}
