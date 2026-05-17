package com.sabtok.process.dto.request;

public record GlobalSearchRequestRecord(
        String searchType,
        String queryString,
        String threadType,
        Boolean requiredThreadDump
) {

    public GlobalSearchRequestRecord {
        if (requiredThreadDump == null) {
            requiredThreadDump = false;
        }
    }
}
