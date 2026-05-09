package com.sabtok.process.dto;

import java.util.Set;

public record CombinedResponseWithTime(Set<BookRecord> bookRecords,
                                       Set<UserStoryRecord> userSoryRecords, long reponseTime) {

}
