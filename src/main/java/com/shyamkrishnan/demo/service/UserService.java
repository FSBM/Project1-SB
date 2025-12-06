package com.shyamkrishnan.demo.service;

import com.shyamkrishnan.demo.model.User;
import com.shyamkrishnan.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // loading user details for authentication
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("cant find user: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getUserEmail(),
                user.getUserPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getUserType()))
        );
    }

    // creating new user account
    public User createNewUser(String email, String rawPass, String role) {
        if (userRepo.findByUserEmail(email).isPresent()) {
            throw new RuntimeException("email alredy exists");
        }

        // default role if not provided
        if (role == null || role.isEmpty()) {
            role = "ROLE_USER";
        }

        User newUser = new User();
        newUser.setUserEmail(email);
        newUser.setUserPassword(passwordEncoder.encode(rawPass));
        newUser.setUserType(role);

        return userRepo.save(newUser);
    }

    // fetch user by email id
    public User getUserByEmail(String email) {
        return userRepo.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("user not found"));
    }
}
