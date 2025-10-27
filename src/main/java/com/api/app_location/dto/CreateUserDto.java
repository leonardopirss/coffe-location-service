package com.api.app_location.dto;

import com.api.app_location.enums.RoleName;

public record CreateUserDto(
        String email,
        String password,
        RoleName role
) {
}
