package com.sabtok.process.controller;

import com.sabtok.process.dto.BookRecord;
import com.sabtok.process.dto.CombinedResponse;
import com.sabtok.process.dto.CombinedResponseWithTime;
import com.sabtok.process.dto.UserStoryRecord;
import com.sabtok.process.openfeign.ExceedClient;
import com.sabtok.process.openfeign.SabInfoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportGenerator {

    @Autowired
    private  SabInfoClient sabInfoClient;

    @Autowired
    private  ExceedClient exceedClient;

    @Autowired
    private Executor feignExecutor;
    //http://laptop-paulleg2:8080/sab-info-services-5/book/all
    //http://laptop-paulleg2:8080/sab-info-services-5/page/all

   // http://laptop-paulleg2:5001/user/list/ALL

    @GetMapping("/merged-data_old")
    public CombinedResponse getMergedData() {

        CompletableFuture<Set<BookRecord>> sabInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                  long startTime = System.currentTimeMillis();
                   Set<BookRecord> bookRecords = sabInfoClient.getAllBooks();
                   long endTime = System.currentTimeMillis();
                   System.out.println("Time taken to fetch book "+bookRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                   return bookRecords;
                });

        CompletableFuture<Set<UserStoryRecord>> exceeCompletableFuture = CompletableFuture.supplyAsync(() ->
                {
                    long startTime = System.currentTimeMillis();
                    Set<UserStoryRecord> userStoryRecords = exceedClient.getAllUserStories();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken to fetch user story "+userStoryRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                   return userStoryRecords;
                }

        );
        CombinedResponse combinedResponse = new CombinedResponse(sabInfoCompletableFuture.join(), exceeCompletableFuture.join());
     return combinedResponse;
    }

    @GetMapping("/merged-data1")
    public CombinedResponse getMergedData1() {
        var sabInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    Set<BookRecord> bookRecords = sabInfoClient.getAllBooks();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken to fetch book "+bookRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                    return bookRecords;
                },
                feignExecutor);

        var exceeCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    Set<UserStoryRecord> userStoryRecords = exceedClient.getAllUserStories();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken to fetch user story "+userStoryRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                    return userStoryRecords;
                },
                 feignExecutor);

        CombinedResponse combinedResponse = new CombinedResponse(sabInfoCompletableFuture.join(), exceeCompletableFuture.join());
        return combinedResponse;
    }

    @GetMapping("/merged-data")
    public CombinedResponseWithTime getMergedData2(String bookId, String storyId) {
        long start = System.currentTimeMillis();

        var bookFuture = CompletableFuture.supplyAsync(() -> sabInfoClient.getAllBooks(), feignExecutor)
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    System.err.println("Failed to fetch book records: " + ex.getMessage());
                    return Set.of(); // Return empty set on failure
                });

        var storyFuture = CompletableFuture.supplyAsync(() -> exceedClient.getAllUserStories(), feignExecutor)
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    System.err.println("Failed to fetch book records: " + ex.getMessage());
                    return Set.of(); // Return empty set on failure
                });;

        return CompletableFuture.allOf(bookFuture, storyFuture)
                .thenApply(v -> new CombinedResponseWithTime(
                        bookFuture.join(),
                        storyFuture.join(),
                        System.currentTimeMillis() - start
                )).join();
    }

}
