package com.sabtok.process.service;

import com.sabtok.process.dto.BookRecord;
import com.sabtok.process.dto.SearchItem;
import com.sabtok.process.dto.UserStoryRecord;
import com.sabtok.process.dto.request.GlobalSearchRequestRecord;
import com.sabtok.process.dto.response.SearchResponseRecord;
import com.sabtok.process.openfeign.ExceedClient;
import com.sabtok.process.openfeign.SabInfoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public SearchResponseRecord searchContentByForkJoinThreadPool(GlobalSearchRequestRecord searchRequest) {

        final ForkJoinPool customThreadPool = new ForkJoinPool(2);
        long t1 = System.currentTimeMillis();
        Set<SearchItem> searchItems = new HashSet<>();
        Set<String> threadSet = new HashSet<>();
        try {
            ForkJoinTask<Set<String>> setForkJoinTask = customThreadPool.submit(() -> {
                threadSet.add(Thread.currentThread().getName());
                if (searchRequest.requiredThreadDump()) {
                    Thread.sleep(3600);
                }
                return exceedClient.getAllUserStories().stream()
                        .map(UserStoryRecord::discription)
                        .filter(description -> description.contains(searchRequest.queryString()))
                        .collect(Collectors.toSet());
            });

            ForkJoinTask<Set<BookRecord>> setForkJoinTask1 = customThreadPool.submit(()-> {
                threadSet.add(Thread.currentThread().getName());
                if (searchRequest.requiredThreadDump()) {
                    Thread.sleep(3600);
                }
                return sabInfoClient.getAllBooks();
            });
            searchItems.add(new SearchItem("Exceed",setForkJoinTask.get()));
            searchItems.add(new SearchItem("SabInfo",setForkJoinTask1.get().stream().map(BookRecord::description)
                    .filter(desc -> desc.contains(searchRequest.queryString())).collect(Collectors.toSet())));


        } catch (Exception e) {
            System.out.println("Getting error for thread: "+Thread.currentThread().getName());
        } finally {
            System.out.println("Time taken is "+ (System.currentTimeMillis() - t1));
        }
        return SearchResponseRecord.builder()
                .responseTime((System.currentTimeMillis() - t1) + "ms")
                .threadName("ForkJoinPool")
                .searchItems(searchItems)
                .threads(threadSet)
                .build();
    }

    public SearchResponseRecord searchContentByCompletableFuture(GlobalSearchRequestRecord searchRequest) throws ExecutionException, InterruptedException {
        long t1 = System.currentTimeMillis();
        Set<String> threadSet = new HashSet<>();
        Set<SearchItem> searchItems = new HashSet<>();
        var sabInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    threadSet.add(Thread.currentThread().getName());
                    if (searchRequest.requiredThreadDump()) {
                        try {
                            Thread.sleep(3600);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    long startTime = System.currentTimeMillis();
                    Set<BookRecord> bookRecords = sabInfoClient.getAllBooks();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken to fetch book "+bookRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                    return bookRecords;
                },
                feignExecutor);


        var exceeCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    threadSet.add(Thread.currentThread().getName());
                    if (searchRequest.requiredThreadDump()) {
                        try {
                            Thread.sleep(3600);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    long startTime = System.currentTimeMillis();
                    Set<UserStoryRecord> userStoryRecords = exceedClient.getAllUserStories();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken to fetch user story "+userStoryRecords.size()+" records: "+ (endTime - startTime) + " milliseconds "+"Thread: "+Thread.currentThread().getName());
                    return userStoryRecords;
                },
                feignExecutor);
        // Block and wait for both parallel processes to finish its optional
        //CompletableFuture.allOf(sabInfoCompletableFuture, exceeCompletableFuture).join();


        CompletableFuture<Set<String>> filteredFuture = sabInfoCompletableFuture.thenApply(bookRecords -> bookRecords.stream()
                .map(BookRecord::description) // Changed from UserStoryRecord to BookRecord
                .filter(description -> description.contains(searchRequest.queryString()))
                .collect(Collectors.toSet()));
        searchItems.add(new SearchItem("SabInfo",filteredFuture.join()));

        CompletableFuture<Set<String>> filteredFuture1 = exceeCompletableFuture.thenApply(userStoryRecords -> userStoryRecords.stream()
                .map(UserStoryRecord::discription) // Changed from UserStoryRecord to BookRecord
                .filter(description -> description.contains(searchRequest.queryString()))
                .collect(Collectors.toSet()));

        searchItems.add(new SearchItem("Exceed",filteredFuture1.join()));

        return SearchResponseRecord.builder()
                .responseTime((System.currentTimeMillis() - t1) + "ms")
                .threadName("CompletableFuture")
                .searchItems(searchItems)
                .threads(threadSet)
                .build();
    }
}
