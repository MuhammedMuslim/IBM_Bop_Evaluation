package com.ibm.elms.service;

import com.ibm.elms.domain.entity.UserAccount;
import com.ibm.elms.dto.AuthDtos.AuthResponse;
import com.ibm.elms.dto.AuthDtos.LoginRequest;
import com.ibm.elms.dto.UserDtos.UserResponse;
import com.ibm.elms.exception.ApiException;
import com.ibm.elms.repository.UserAccountRepository;
import com.ibm.elms.security.JwtService;
import com.ibm.elms.security.SecurityUtils;
import com.ibm.elms.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserAccountRepository userAccountRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserAccount user = userAccountRepository
                .findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole());
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        UserPrincipal principal = SecurityUtils.currentUser();
        UserAccount user = userAccountRepository
                .findByIdWithManager(principal.getId())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return Mapper.toUser(user);
    }
}
