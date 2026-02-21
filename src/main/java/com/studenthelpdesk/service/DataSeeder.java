package com.studenthelpdesk.service;

import com.studenthelpdesk.model.AppUser;
import com.studenthelpdesk.model.UserRole;
import com.studenthelpdesk.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedIfMissing("admin", "admin123", "Helpdesk Manager", "admin@college.edu", "Administration", UserRole.MANAGEMENT);
        seedIfMissing("dean", "dean123", "Academic Dean", "dean@college.edu", "Administration", UserRole.MANAGEMENT);
        seedIfMissing("staff1", "staff123", "Facilities Staff", "staff1@college.edu", "Facilities", UserRole.STAFF);
        seedIfMissing("staff2", "staff123", "IT Support Staff", "staff2@college.edu", "IT Services", UserRole.STAFF);
        seedIfMissing("student1", "student123", "Demo Student", "student1@college.edu", "Computer Science", UserRole.STUDENT);
    }

    private void seedIfMissing(String username, String rawPassword, String fullName,
                               String email, String department, UserRole role) {
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setDepartment(department);
        user.setRole(role);
        appUserRepository.save(user);
    }
}
