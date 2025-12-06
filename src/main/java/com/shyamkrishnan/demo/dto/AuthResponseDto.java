package com.shyamkrishnan.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String jwtToken;
    private String emailId;
    private String userRole; 
}
