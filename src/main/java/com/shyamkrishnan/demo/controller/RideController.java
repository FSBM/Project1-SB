package com.shyamkrishnan.demo.controller;

import com.shyamkrishnan.demo.dto.RideRequestDto;
import com.shyamkrishnan.demo.model.Ride;
import com.shyamkrishnan.demo.model.User;
import com.shyamkrishnan.demo.service.RideService;
import com.shyamkrishnan.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.getUserByEmail(email);
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestRide(@Valid @RequestBody RideRequestDto rideRequest) {
        try {
            User currentUser = getCurrentUser();
            Ride newRide = rideService.makeNewRide(rideRequest, currentUser.getUserId());
            return ResponseEntity.ok(newRide);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<Ride>> getAvailableRides() {
        List<Ride> availableRides = rideService.getAllAvailableRides();
        return ResponseEntity.ok(availableRides);
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<?> acceptRide(@PathVariable String rideId) {
        try {
            User currentDriver = getCurrentUser();
            
            if (!"ROLE_DRIVER".equals(currentDriver.getUserType())) {
                return ResponseEntity.badRequest().body("Only drivers can accept rides");
            }
            
            rideService.driverAcceptRide(rideId, currentDriver.getUserId());
            return ResponseEntity.ok("Ride accepted successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{rideId}/complete")
    public ResponseEntity<?> completeRide(@PathVariable String rideId) {
        try {
            User currentDriver = getCurrentUser();
            
            if (!"ROLE_DRIVER".equals(currentDriver.getUserType())) {
                return ResponseEntity.badRequest().body("Only drivers can complete rides");
            }
            
            rideService.markRideComplete(rideId, currentDriver.getUserId());
            return ResponseEntity.ok("Ride completed successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/my-rides")
    public ResponseEntity<List<Ride>> getMyRides() {
        User currentUser = getCurrentUser();
        List<Ride> userRides = rideService.getPassengerRides(currentUser.getUserId());
        return ResponseEntity.ok(userRides);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<?> getRideDetails(@PathVariable String rideId) {
        try {
            Ride rideDetails = rideService.getRideDetails(rideId);
            return ResponseEntity.ok(rideDetails);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
