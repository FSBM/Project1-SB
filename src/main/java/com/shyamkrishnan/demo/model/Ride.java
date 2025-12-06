package com.shyamkrishnan.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "rides")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ride {
    @Id
    private String rideId;

    private String passengerId;

    private String assignedDriver;

    private String startPoint;

    private String endPoint;

    private String rideStatus;

    private Date requestedTime;
}
