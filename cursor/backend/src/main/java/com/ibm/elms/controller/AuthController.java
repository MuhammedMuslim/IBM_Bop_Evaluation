package com.ibm.elms.controller;

import com.ibm.elms.dto.AuthDtos.AuthResponse;
import com.ibm.elms.dto.AuthDtos.LoginRequest;
import com.ibm.elms.dto.UserDtos.UserResponse;
import com.ibm.elms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.me();
    }
}
