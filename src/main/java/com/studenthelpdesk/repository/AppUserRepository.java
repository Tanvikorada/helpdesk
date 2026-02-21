package com.studenthelpdesk.repository;

import com.studenthelpdesk.model.AppUser;
import com.studenthelpdesk.model.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findAllByRoleOrderByFullNameAsc(UserRole role);

    List<AppUser> findAllByRoleIn(List<UserRole> roles);
}
