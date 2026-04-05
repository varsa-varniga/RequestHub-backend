package com.varniga.requestmanagement.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}