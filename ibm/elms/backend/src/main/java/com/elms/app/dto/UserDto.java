package com.elms.app.dto;

import com.elms.app.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Role role;
    private boolean active;
    private Long managerId;
    private String managerName;
    private String department;
    private String jobTitle;
    private LocalDateTime createdAt;
}
