package com.shyamkrishnan.demo.controller;

import com.shyamkrishnan.demo.dto.AuthRequestDto;
import com.shyamkrishnan.demo.dto.AuthResponseDto;
import com.shyamkrishnan.demo.model.User;
import com.shyamkrishnan.demo.service.UserService;
import com.shyamkrishnan.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequestDto request) {
        try {
            User newUser = userService.createNewUser(
                request.getEmailId(), 
                request.getPass(), 
                request.getUserRole()
            );
            
            String jwtToken = jwtUtil.makeToken(newUser.getUserEmail());
            
            return ResponseEntity.ok(new AuthResponseDto(jwtToken, newUser.getUserEmail(), newUser.getUserType()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // returns token if credentails r valid
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDto request) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmailId(), request.getPass())
            );

            User existingUser = userService.getUserByEmail(request.getEmailId());
            String jwtToken = jwtUtil.makeToken(existingUser.getUserEmail());

            return ResponseEntity.ok(new AuthResponseDto(jwtToken, existingUser.getUserEmail(), existingUser.getUserType()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }
}
