package com.varniga.requestmanagement.dto;

import com.varniga.requestmanagement.enums.Role;
import lombok.Data;

@Data
public class UpdateUserDto {
    private String name;
    private String email;
    private Role role;
    private Boolean active;
}