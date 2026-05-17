package com.sabtok.process.controller;

import com.sabtok.process.dto.BookRecord;
import com.sabtok.process.dto.CombinedResponse;
import com.sabtok.process.dto.UserStoryRecord;
import com.sabtok.process.service.ContentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.sabtok.process.dto.ThreadType.FORK;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final ContentSearchService skillContentSearchService;

    @GetMapping("/text/{searchString}/{threadType}")
    public Object getSearchData(@PathVariable("searchString") String searchString,
                                @PathVariable("threadType") String threadType) throws ExecutionException, InterruptedException {

        if (FORK.name().equalsIgnoreCase(threadType)) {
            return skillContentSearchService.searchContentByForkJoinThreadPool(searchString);
        } else {
            return skillContentSearchService.searchContentByCompletableFuture(searchString);
        }

    }


}
