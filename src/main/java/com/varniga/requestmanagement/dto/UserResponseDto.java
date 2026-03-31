package com.varniga.requestmanagement.dto;

import com.varniga.requestmanagement.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
}