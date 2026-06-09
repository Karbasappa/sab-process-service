package com.sabtok.process.controller;

import com.sabtok.process.service.PageWriterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/page-write")
@RequiredArgsConstructor
public class PageWriterController {

    private final PageWriterService pageWriterService;

    @GetMapping("/create/{pageId}")
    public ResponseEntity<byte[]> writePage1(@PathVariable("pageId") String pageId) throws IOException {
        // 1. Fetch the binary payload from your service layer
        byte[] pdfBytes = (byte[]) pageWriterService.writePage(pageId);

        // 2. Configure HTTP headers so the client browser understands the file structure
        HttpHeaders headers = new HttpHeaders();

        // Explicitly tells the client network layer that this is a PDF document
        headers.setContentType(MediaType.APPLICATION_PDF);

        // Forces the browser to trigger an automatic file download window named "page_[id].pdf"
        headers.setContentDispositionFormData("attachment", "page_" + pageId + ".pdf");

        // Stops proxy caches from holding onto stale copies of old dynamic pages
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        // 3. Return the response entity container along with an HTTP 200 OK status
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
