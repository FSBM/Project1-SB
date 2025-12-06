package com.shyamkrishnan.demo.service;

import com.shyamkrishnan.demo.dto.RideRequestDto;
import com.shyamkrishnan.demo.model.Ride;
import com.shyamkrishnan.demo.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RideService {

    @Autowired
    private RideRepository rideRepo;

    // creating new ride request from passanger
    public Ride makeNewRide(RideRequestDto request, String passengerId) {
        Ride ride = new Ride();
        ride.setPassengerId(passengerId);
        ride.setStartPoint(request.getFromLocation());
        ride.setEndPoint(request.getToLocation());
        ride.setRideStatus("REQUESTED");
        ride.setRequestedTime(new Date());
        return rideRepo.save(ride);
    }

    // when driver accepts a ride
    public void driverAcceptRide(String rideId, String driverId) {
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("ride not found"));

        // checking if ride is still available
        if (!"REQUESTED".equals(ride.getRideStatus())) {
            throw new RuntimeException("ride alredy taken");
        }

        ride.setAssignedDriver(driverId);
        ride.setRideStatus("ACCEPTED");
        rideRepo.save(ride);
    }

    // marking ride as completed by driver
    public void markRideComplete(String rideId, String driverId) {
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("ride not found"));

        // validate driver is assigned to this ride
        if (!driverId.equals(ride.getAssignedDriver())) {
            throw new RuntimeException("you not assigned to this ride");
        }

        if (!"ACCEPTED".equals(ride.getRideStatus())) {
            throw new RuntimeException("ride not in accepted state");
        }

        ride.setRideStatus("COMPLETED");
        rideRepo.save(ride);
    }

    // get rides that are availble for drivers
    public List<Ride> getAllAvailableRides() {
        return rideRepo.findByRideStatus("REQUESTED");
    }

    // get all rides for a passanger
    public List<Ride> getPassengerRides(String passengerId) {
        return rideRepo.findByPassengerId(passengerId);
    }

    // fetching single ride by id
    public Ride getRideDetails(String rideId) {
        return rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("ride not found"));
    }
}
