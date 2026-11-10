package com.elms.app.service;

import com.elms.app.dto.*;
import com.elms.app.entity.User;
import com.elms.app.enums.Role;
import com.elms.app.exception.*;
import com.elms.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<UserDto> getManagers() {
        return userRepository.findByRole(Role.MANAGER).stream().map(this::toDto).toList();
    }

    public UserDto getUserById(Long id) {
        return toDto(findById(id));
    }

    public List<UserDto> getTeamByManager(Long managerId) {
        return userRepository.findByManagerId(managerId).stream().map(this::toDto).toList();
    }

    @Transactional
    public UserDto createUser(CreateUserRequest req, User actor) {
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new BusinessException("Email already in use: " + req.getEmail());
        }
        User manager = null;
        if (req.getManagerId() != null) {
            manager = findById(req.getManagerId());
        }
        User user = User.builder()
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .role(req.getRole())
                .active(true)
                .manager(manager)
                .department(req.getDepartment())
                .jobTitle(req.getJobTitle())
                .build();
        user = userRepository.save(user);
        auditService.log(actor, "CREATE_USER", "User", user.getId(), "Created user " + user.getEmail());
        return toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, CreateUserRequest req, User actor) {
        User user = findById(id);
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()  != null) user.setLastName(req.getLastName());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getJobTitle()  != null) user.setJobTitle(req.getJobTitle());
        if (req.getRole()      != null) user.setRole(req.getRole());
        if (req.getManagerId() != null) {
            user.setManager(findById(req.getManagerId()));
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        user = userRepository.save(user);
        auditService.log(actor, "UPDATE_USER", "User", user.getId(), "Updated user " + user.getEmail());
        return toDto(user);
    }

    @Transactional
    public void deactivateUser(Long id, User actor) {
        User user = findById(id);
        user.setActive(false);
        userRepository.save(user);
        auditService.log(actor, "DEACTIVATE_USER", "User", id, "Deactivated user " + user.getEmail());
    }

    @Transactional
    public void activateUser(Long id, User actor) {
        User user = findById(id);
        user.setActive(true);
        userRepository.save(user);
        auditService.log(actor, "ACTIVATE_USER", "User", id, "Activated user " + user.getEmail());
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public UserDto toDto(User u) {
        return UserDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .fullName(u.getFullName())
                .role(u.getRole())
                .active(u.isActive())
                .managerId(u.getManager() != null ? u.getManager().getId() : null)
                .managerName(u.getManager() != null ? u.getManager().getFullName() : null)
                .department(u.getDepartment())
                .jobTitle(u.getJobTitle())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
