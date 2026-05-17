package com.sabtok.process.dto.request;

public record GlobalSearchRequestRecord(
        String searchType,
        String queryString,
        String threadType,
        String mode
) {
}
