package com.varniga.requestmanagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRequestDto {

    private String title;
    private String description;

    // frontend sends this (example: "IT_SUPPORT", "HR", etc.)
    private String requestTypeCode;

    private String urgency;
}