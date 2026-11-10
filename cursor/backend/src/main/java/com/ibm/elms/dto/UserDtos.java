package com.ibm.elms.dto;

import com.ibm.elms.domain.enums.EmployeeStatus;
import com.ibm.elms.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class UserDtos {

    private UserDtos() {}

    public record UserResponse(
            Long id,
            String email,
            String firstName,
            String lastName,
            String employeeCode,
            String department,
            String jobTitle,
            Role role,
            EmployeeStatus status,
            Long managerId,
            String managerName) {}

    public record CreateUserRequest(
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String employeeCode,
            String department,
            String jobTitle,
            @NotNull Role role,
            Long managerId) {}

    public record UpdateUserRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String employeeCode,
            String department,
            String jobTitle,
            @NotNull Role role,
            @NotNull EmployeeStatus status,
            Long managerId) {}
}
