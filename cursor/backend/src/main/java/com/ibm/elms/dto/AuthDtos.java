package com.ibm.elms.dto;

import com.ibm.elms.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record AuthResponse(
            String token,
            Long userId,
            String email,
            String firstName,
            String lastName,
            Role role) {}
}
