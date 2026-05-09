package com.sabtok.process.dto;

public record UserStoryRecord(String storyNumber,
                              String discription,
                              String projectName,
                              // @JsonFormat(pattern = "dd-MMM-yyyy") LocalDate startDate,
                              // @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime createDate,
                              // @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime closedDate,
                              Integer storyPoint,
                              String assigned,
                              String status,
                              String gitUrl
                            ) {}
