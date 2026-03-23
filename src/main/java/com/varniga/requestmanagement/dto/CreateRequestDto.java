package com.varniga.requestmanagement.dto;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class CreateRequestDto {
    private String title;
    private String description;
    private String type;
    private String urgency;
}