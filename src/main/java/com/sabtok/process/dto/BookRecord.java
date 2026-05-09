package com.sabtok.process.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
public record BookRecord(
        String bookId,
        Integer bookNo,
        String bookName,
        String description,
       // @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
       // LocalDateTime createdDate,
        String createdBy
) {}
