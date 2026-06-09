package com.sabtok.process.dto;

public record PageResponseRecord (String pageId,
                                 Long pageNo,
                                 String bookId,
                                 String bookName,
                                 String title,
                                 String createdDate,
                                 String createdBy,
                                  String content) {
}
