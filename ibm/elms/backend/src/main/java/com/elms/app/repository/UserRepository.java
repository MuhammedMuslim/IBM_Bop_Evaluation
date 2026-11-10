package com.elms.app.repository;

import com.elms.app.entity.User;
import com.elms.app.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByManagerId(Long managerId);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.role IN ('EMPLOYEE','MANAGER') AND u.active = true ORDER BY u.lastName")
    List<User> findAllActiveEmployees();
}
