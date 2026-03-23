package com.varniga.requestmanagement.dto;

import com.varniga.requestmanagement.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDto {
    private String name;
    private String email;
    private String password;
    private Role role; // USER, ADMIN, MANAGER, IT, COMPLIANCE
}