package com.shyamkrishnan.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDto {

    @NotBlank(message = "email cant be empty")
    private String emailId;

    @NotBlank(message = "password cant be empty")
    private String pass;

    private String userRole;
}
