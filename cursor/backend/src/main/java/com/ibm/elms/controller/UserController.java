package com.ibm.elms.controller;

import com.ibm.elms.dto.UserDtos.CreateUserRequest;
import com.ibm.elms.dto.UserDtos.UpdateUserRequest;
import com.ibm.elms.dto.UserDtos.UserResponse;
import com.ibm.elms.security.SecurityUtils;
import com.ibm.elms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public List<UserResponse> list() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN','MANAGER') or #id == principal.id")
    public UserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public List<UserResponse> team() {
        return userService.teamMembers(SecurityUtils.currentUser().getId());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }
}
