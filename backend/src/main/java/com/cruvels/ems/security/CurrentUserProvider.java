package com.cruvels.ems.security;

import com.cruvels.ems.model.Employee;
import com.cruvels.ems.model.User;
import com.cruvels.ems.repository.EmployeeRepository;
import com.cruvels.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged in user not found in database"));
    }

    public Employee getCurrentEmployee() {
        User user = getCurrentUser();
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No employee profile linked to this account"));
    }
}
