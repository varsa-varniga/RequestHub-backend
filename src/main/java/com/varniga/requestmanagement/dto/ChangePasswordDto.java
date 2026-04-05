package com.varniga.requestmanagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {
    private String oldPassword;
    private String newPassword;
}