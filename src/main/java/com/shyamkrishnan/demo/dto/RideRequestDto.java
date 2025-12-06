package com.shyamkrishnan.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RideRequestDto {

    @NotBlank(message = "need pickup location")
    private String fromLocation; 

    @NotBlank(message = "need drop location")
    private String toLocation; 
}
