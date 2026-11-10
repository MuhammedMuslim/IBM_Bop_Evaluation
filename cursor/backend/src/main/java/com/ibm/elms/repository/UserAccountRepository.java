package com.ibm.elms.repository;

import com.ibm.elms.domain.entity.UserAccount;
import com.ibm.elms.domain.enums.EmployeeStatus;
import com.ibm.elms.domain.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UserAccount> findByManagerIdAndStatus(Long managerId, EmployeeStatus status);

    List<UserAccount> findByRole(Role role);

    List<UserAccount> findByStatus(EmployeeStatus status);

    @Query("select u from UserAccount u left join fetch u.manager where u.id = :id")
    Optional<UserAccount> findByIdWithManager(@Param("id") Long id);
}
