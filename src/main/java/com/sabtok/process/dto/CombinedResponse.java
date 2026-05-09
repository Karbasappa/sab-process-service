package com.sabtok.process.dto;

import java.util.Set;


public record CombinedResponse(Set<BookRecord> bookRecords,
        Set<UserStoryRecord> userSoryRecords) {

}

