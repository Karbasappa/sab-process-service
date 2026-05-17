package com.sabtok.process.service;

import com.sabtok.process.dto.BookRecord;
import com.sabtok.process.dto.CombinedResponse;
import com.sabtok.process.dto.UserStoryRecord;
import com.sabtok.process.dto.response.SearchResponseRecord;
import com.sabtok.process.openfeign.ExceedClient;
import com.sabtok.process.openfeign.SabInfoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentSearchService {
    private final ExceedClient exceedClient;
    private final SabInfoClient sabInfoClient;
    private final Executor feignExecutor;

    public SearchResponseRecord searchContentByForkJoinThreadPool(String searchString) {

        final ForkJoinPool customThreadPool = new ForkJoinPool(2);
        long t1 = System.currentTimeMillis();
        Set<String> dataSet = new HashSet<>();
        try {
            ForkJoinTask<Set<String>> setForkJoinTask = customThreadPool.submit(() -> {
               return exceedClient.getAllUserStories().stream()
                        .map(UserStoryRecord::discription)
                        .filter(description -> description.contains(searchString))
                        .collect(Collectors.toSet());
            });

            ForkJoinTask<Set<BookRecord>> setForkJoinTask1 = customThreadPool.submit(sabInfoClient::getAllBooks);
            dataSet.addAll(setForkJoinTask.get());
            dataSet.addAll(setForkJoinTask1.get().stream().map(BookRecord::description)
                    .filter(desc -> desc.contains(searchString)).collect(Collectors.toSet()));

        } catch (Exception e) {
            System.out.println("Getting error for thread: "+Thread.currentThread().getName());
        } finally {
            System.out.println("Time taken is "+ (System.currentTimeMillis() - t1));
        }
        return SearchResponseRecord.builder()
                .responseTime((System.currentTimeMillis() - t1) + "ms")
                .threadName("ForkJoinPool")
                .searchItems(dataSet)
                .build();
    }

    public SearchResponseRecord searchContentByCompletableFuture(String searchString) throws ExecutionException, InterruptedException {
        long t1 = System.currentTimeMillis();
        Set<String> dataSet = new HashSet<>();
        var sabInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    Set<BookRecord> bookRecords = sabInfoClient.getAllBooks();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken to fetch book "+bookRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                    return bookRecords;
                },
                feignExecutor);

        CompletableFuture<Set<String>> filteredFuture = sabInfoCompletableFuture.thenApply(bookRecords -> bookRecords.stream()
                .map(BookRecord::description) // Changed from UserStoryRecord to BookRecord
                .filter(description -> description.contains(searchString))
                .collect(Collectors.toSet()));

        dataSet.addAll(filteredFuture.join());

        var exceeCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    Set<UserStoryRecord> userStoryRecords = exceedClient.getAllUserStories();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken to fetch user story "+userStoryRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                    return userStoryRecords;
                },
                feignExecutor);

        CompletableFuture<Set<String>> filteredFuture1 = exceeCompletableFuture.thenApply(userStoryRecords -> userStoryRecords.stream()
                .map(UserStoryRecord::discription) // Changed from UserStoryRecord to BookRecord
                .filter(description -> description.contains(searchString))
                .collect(Collectors.toSet()));

        dataSet.addAll(filteredFuture1.join());
        CombinedResponse combinedResponse = new CombinedResponse(sabInfoCompletableFuture.join(), exceeCompletableFuture.join());
        return SearchResponseRecord.builder()
                .responseTime((System.currentTimeMillis() - t1) + "ms")
                .threadName("CompletableFuture")
                .searchItems(dataSet)
                .build();
    }
}
