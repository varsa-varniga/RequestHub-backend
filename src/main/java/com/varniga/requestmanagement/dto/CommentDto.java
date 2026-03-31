package com.varniga.requestmanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    private String author;
    private String message;
    private LocalDateTime createdAt;
}