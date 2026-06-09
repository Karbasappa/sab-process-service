package com.sabtok.process.controller;

import com.sabtok.process.service.BookWriterService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/book-write")
@RequiredArgsConstructor
public class BookWriterController {

    private final BookWriterService bookWriterService;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(5);

    @GetMapping("/create")
    public ResponseEntity<byte[]> writeBook(){
        byte[] pdfBytes =  bookWriterService.writeBook();
        // 2. Configure HTTP headers so the client browser understands the file structure
        HttpHeaders headers = new HttpHeaders();

        // Explicitly tells the client network layer that this is a PDF document
        headers.setContentType(MediaType.APPLICATION_PDF);

        // Forces the browser to trigger an automatic file download window named "page_[id].pdf"
        headers.setContentDispositionFormData("attachment", "wiki_" + ".pdf");

        // Stops proxy caches from holding onto stale copies of old dynamic pages
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        // 3. Return the response entity container along with an HTTP 200 OK status
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/create/{bookId}")
    public Object writeBook(@PathVariable("bookId") String bookId){
        return bookWriterService.writeBook();
        //return "SUCCESS";
    }

    @GetMapping("/stream/report")
    public ResponseEntity<ResponseBodyEmitter> streamBookProcessingReport() {

        // 1. Create the emitter instance (Timeout set to 5 minutes / 300,000ms)
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300_000L);

        // 2. Submit the heavy processing task asynchronously to our background thread
        threadPool.submit(() -> {
            try {
                // Initial update sent immediately to the user's browser
                emitter.send("Initializing book scan process...\n\n", MediaType.TEXT_PLAIN);
                Thread.sleep(1000); // Mimicking a quick DB setup delay

                // Step A: Book Processing
                emitter.send("Step 1: Processing 'Playwright Cheat Sheet'...\n", MediaType.TEXT_PLAIN);
                Thread.sleep(3600); // Mimicking heavy work/PDF creation
                emitter.send("-> Successfully compiled 2 sub-pages.\n\n", MediaType.TEXT_PLAIN);

                // Step B: Next Book Processing
                emitter.send("Step 2: Processing 'Java Concurrency in Practice'...\n", MediaType.TEXT_PLAIN);
                Thread.sleep(4200);
                emitter.send("-> Successfully compiled 15 sub-pages.\n\n", MediaType.TEXT_PLAIN);

                // Send a structured final completion summary report
                emitter.send("Final Status: All books successfully compiled to PDF format!\n", MediaType.TEXT_PLAIN);

                // 3. CRITICAL: Inform the container that the stream is finished
                emitter.complete();

            } catch (Exception e) {
                // Inform the client browser that an error disrupted the stream pipeline
                emitter.completeWithError(e);
            }
        });

        // 4. Return the emitter directly with a 200 OK wrapper.
        // Spring handles the chunked transfer encoding protocol under the hood.
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(emitter);
    }
}
