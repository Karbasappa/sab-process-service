package com.sabtok.process.openfeign;

import com.sabtok.process.dto.BookRecord;
import com.sabtok.process.dto.PageResponseRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;


@FeignClient(name = "sab-info-service", url = "${sab.info.service.url}")
public interface SabInfoClient {

    @GetMapping("/book/all")
    Set<BookRecord> getAllBooks();

    @GetMapping("/page/pageList/{bookId}")
    Set<PageResponseRecord> getAllPagesForBook(@PathVariable("bookId") String bookId);

    @GetMapping("/page/details/{pageId}")
    PageResponseRecord getPageDetails(@PathVariable("pageId") String pageId);

}
