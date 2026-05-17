package com.sabtok.process.controller;

import com.sabtok.process.dto.request.GlobalSearchRequestRecord;
import com.sabtok.process.service.ContentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

import static com.sabtok.process.dto.ThreadType.FORK;

@RestController
@RequiredArgsConstructor
public class GlobalSearchController {

    private final ContentSearchService skillContentSearchService;

    @GetMapping(value = "/search")
    public Object getSearchData(@RequestBody GlobalSearchRequestRecord searchRequest) throws ExecutionException, InterruptedException {

        if (FORK.name().equalsIgnoreCase(searchRequest.threadType())) {
            return skillContentSearchService.searchContentByForkJoinThreadPool(searchRequest);
        } else {
            return skillContentSearchService.searchContentByCompletableFuture(searchRequest);
        }

    }


}
