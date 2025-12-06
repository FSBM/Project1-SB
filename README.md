# Ride Sharing Platform - Backend API Documentation

## Overview

This document provides a comprehensive guide to the Ride Sharing Platform's backend system. The platform facilitates seamless connections between passengers seeking transportation and drivers willing to provide rides. Built using Spring Boot and MongoDB, this system offers a robust, secure, and scalable solution for modern ride-sharing needs.

## Technology Stack

The backend leverages modern Java technologies to ensure reliability and performance:

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 21
- **Database**: MongoDB (Cloud-based via MongoDB Atlas)
- **Security**: Spring Security with JWT authentication
- **Build Tool**: Maven
- **Server**: Embedded Apache Tomcat (Port 8080)

### Key Dependencies

- **Spring Web**: Handles HTTP requests and RESTful API endpoints
- **Spring Security**: Manages authentication and authorization
- **Spring Data MongoDB**: Provides seamless database integration
- **JWT Library (jjwt)**: Implements token-based authentication
- **Lombok**: Reduces boilerplate code through annotations
- **Jakarta Validation**: Ensures data integrity through validation

## System Architecture

### Application Structure

The application follows a layered architecture pattern, promoting separation of concerns and maintainability:

```
src/main/java/com/shyamkrishnan/demo/
├── config/          # Security and application configurations
├── controller/      # REST API endpoints
├── dto/            # Data Transfer Objects for API requests/responses
├── exception/      # Global exception handling
├── filter/         # JWT authentication filter
├── model/          # Database entity models
├── repository/     # Data access layer
├── service/        # Business logic layer
└── util/           # Utility classes (JWT operations)
```

### Database Schema

#### Users Collection

Stores information about all platform users, both passengers and drivers:

| Field | Type | Description |
|-------|------|-------------|
| userId | String | Unique identifier (auto-generated) |
| userEmail | String | User's email address (unique index) |
| userPassword | String | Encrypted password |
| userType | String | Role designation (ROLE_PASSENGER or ROLE_DRIVER) |

#### Rides Collection

Manages all ride requests and their lifecycle:

| Field | Type | Description |
|-------|------|-------------|
| rideId | String | Unique identifier (auto-generated) |
| passengerId | String | Reference to requesting passenger |
| assignedDriver | String | Reference to accepting driver (nullable) |
| startPoint | String | Pickup location |
| endPoint | String | Destination location |
| rideStatus | String | Current status (REQUESTED, ACCEPTED, COMPLETED) |
| requestedTime | Date | Timestamp of ride request |

## API Endpoints Reference

### Authentication Endpoints

#### 1. User Registration

Creates a new user account in the system.

**Endpoint**: `POST /api/auth/register`

**Headers**: 
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "emailId": "user@example.com",
  "pass": "securePassword123",
  "userRole": "ROLE_PASSENGER"
}
```

**Field Descriptions**:
- `emailId`: Must be a valid email format
- `pass`: User's chosen password (will be encrypted)
- `userRole`: Either "ROLE_PASSENGER" or "ROLE_DRIVER"

**Success Response** (200 OK):
```json
{
  "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
  "emailId": "user@example.com",
  "userRole": "ROLE_PASSENGER"
}
```

**Error Response** (400 Bad Request):
```json
"email alredy exists"
```

**Implementation Notes**:
- Passwords are encrypted using BCrypt before storage
- Email uniqueness is enforced at the database level
- JWT token is automatically generated upon successful registration
- Token validity: 10 hours from creation

---

#### 2. User Login

Authenticates existing users and provides access tokens.

**Endpoint**: `POST /api/auth/login`

**Headers**: 
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "emailId": "user@example.com",
  "pass": "securePassword123"
}
```

**Success Response** (200 OK):
```json
{
  "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
  "emailId": "user@example.com",
  "userRole": "ROLE_PASSENGER"
}
```

**Error Response** (400 Bad Request):
```json
"Invalid username or password"
```

**Implementation Notes**:
- Uses Spring Security's AuthenticationManager for credential verification
- Failed login attempts return generic error message for security
- New JWT token generated for each successful login

---

### Ride Management Endpoints

All ride endpoints require authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

#### 3. Request New Ride

Allows passengers to create new ride requests.

**Endpoint**: `POST /api/rides/request`

**Headers**: 
```
Content-Type: application/json
Authorization: Bearer <passenger-jwt-token>
```

**Request Body**:
```json
{
  "fromLocation": "123 Main Street, Downtown",
  "toLocation": "456 Oak Avenue, Uptown"
}
```

**Field Validation**:
- `fromLocation`: Required, cannot be blank
- `toLocation`: Required, cannot be blank

**Success Response** (200 OK):
```json
{
  "rideId": "6934659e0c7663da0d01f34a",
  "passengerId": "693465880c7663da0d01f348",
  "assignedDriver": null,
  "startPoint": "123 Main Street, Downtown",
  "endPoint": "456 Oak Avenue, Uptown",
  "rideStatus": "REQUESTED",
  "requestedTime": "2025-12-06T17:19:26.456Z"
}
```

**Error Scenarios**:
- Missing required fields returns validation error
- Unauthorized users receive 401/403 status

**Business Logic**:
- Automatically captures requesting user's ID from JWT token
- Sets initial status to "REQUESTED"
- Records timestamp of request
- Driver assignment remains null until acceptance

---

#### 4. View Available Rides

Displays all rides awaiting driver acceptance.

**Endpoint**: `GET /api/rides/available`

**Headers**: 
```
Authorization: Bearer <driver-jwt-token>
```

**Success Response** (200 OK):
```json
[
  {
    "rideId": "6934659e0c7663da0d01f34a",
    "passengerId": "693465880c7663da0d01f348",
    "assignedDriver": null,
    "startPoint": "123 Main Street, Downtown",
    "endPoint": "456 Oak Avenue, Uptown",
    "rideStatus": "REQUESTED",
    "requestedTime": "2025-12-06T17:19:26.456Z"
  }
]
```

**Response Notes**:
- Returns empty array if no rides are available
- Only shows rides with status "REQUESTED"
- Excludes already accepted or completed rides

**Use Case**:
Drivers use this endpoint to browse rides they can accept. The frontend typically refreshes this list periodically to show new requests in real-time.

---

#### 5. Accept Ride Request

Enables drivers to claim available ride requests.

**Endpoint**: `POST /api/rides/{rideId}/accept`

**Path Parameter**:
- `rideId`: The unique identifier of the ride to accept

**Headers**: 
```
Authorization: Bearer <driver-jwt-token>
```

**Success Response** (200 OK):
```json
"Ride accepted successfully"
```

**Error Responses**:

*Non-driver user* (400 Bad Request):
```json
"Only drivers can accept rides"
```

*Ride not found or already accepted* (400 Bad Request):
```json
"<Error message from service layer>"
```

**Business Rules**:
- Only users with ROLE_DRIVER can accept rides
- Ride must be in "REQUESTED" status
- Updates ride status to "ACCEPTED"
- Records driver's ID in assignedDriver field
- Prevents double-booking by rejecting subsequent accept attempts

---

#### 6. Mark Ride Complete

Allows drivers to finalize completed rides.

**Endpoint**: `POST /api/rides/{rideId}/complete`

**Path Parameter**:
- `rideId`: The unique identifier of the ride to complete

**Headers**: 
```
Authorization: Bearer <driver-jwt-token>
```

**Success Response** (200 OK):
```json
"Ride completed successfully"
```

**Error Responses**:

*Non-driver user* (400 Bad Request):
```json
"Only drivers can complete rides"
```

*Invalid ride or driver mismatch* (400 Bad Request):
```json
"<Error message from service layer>"
```

**Business Rules**:
- Only the assigned driver can complete their ride
- Ride must be in "ACCEPTED" status
- Updates ride status to "COMPLETED"
- Driver ID must match the assignedDriver field

---

#### 7. View My Rides

Retrieves all rides associated with the authenticated user.

**Endpoint**: `GET /api/rides/my-rides`

**Headers**: 
```
Authorization: Bearer <jwt-token>
```

**Success Response** (200 OK):
```json
[
  {
    "rideId": "6934659e0c7663da0d01f34a",
    "passengerId": "693465880c7663da0d01f348",
    "assignedDriver": "693465890c7663da0d01f349",
    "startPoint": "123 Main Street, Downtown",
    "endPoint": "456 Oak Avenue, Uptown",
    "rideStatus": "COMPLETED",
    "requestedTime": "2025-12-06T17:19:26.456Z"
  }
]
```

**Behavior by User Type**:
- **Passengers**: See all rides they have requested
- **Drivers**: See all rides they have accepted
- Returns empty array if user has no rides

**Common Use Cases**:
- Passengers tracking ride history
- Drivers viewing their assignment history
- Accessing ride details for customer service

---

#### 8. Get Ride Details

Fetches complete information about a specific ride.

**Endpoint**: `GET /api/rides/{rideId}`

**Path Parameter**:
- `rideId`: The unique identifier of the ride

**Headers**: 
```
Authorization: Bearer <jwt-token>
```

**Success Response** (200 OK):
```json
{
  "rideId": "6934659e0c7663da0d01f34a",
  "passengerId": "693465880c7663da0d01f348",
  "assignedDriver": "693465890c7663da0d01f349",
  "startPoint": "123 Main Street, Downtown",
  "endPoint": "456 Oak Avenue, Uptown",
  "rideStatus": "ACCEPTED",
  "requestedTime": "2025-12-06T17:19:26.456Z"
}
```

**Error Response** (400 Bad Request):
```json
"<Error message if ride not found>"
```

**Use Cases**:
- Viewing detailed ride information
- Checking current status of a ride
- Customer support inquiries

---

## Security Implementation

### JWT Authentication Flow

The application implements a stateless authentication mechanism using JSON Web Tokens:

1. **User Registration/Login**: Upon successful authentication, the server generates a JWT token containing the user's email address.

2. **Token Structure**: Each token includes:
   - Subject (sub): User's email
   - Issued At (iat): Token creation timestamp
   - Expiration (exp): Token validity period (10 hours)
   - Signature: HMAC-SHA256 signature using secret key

3. **Request Authentication**: For protected endpoints, clients must include the token:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

4. **Token Validation**: The JwtRequestFilter intercepts requests and:
   - Extracts the token from Authorization header
   - Validates token signature and expiration
   - Retrieves user details from token claims
   - Sets authentication in SecurityContext

### Security Configuration

The SecurityConfig class defines access rules:

- **Public Endpoints**: `/api/auth/**` (registration and login)
- **Protected Endpoints**: All other `/api/**` endpoints require authentication
- **Password Encryption**: BCrypt with strength 12
- **CORS**: Configured for cross-origin requests
- **CSRF**: Disabled for stateless API

### Role-Based Access Control

While all authenticated users can access most endpoints, business logic enforces role-specific operations:

- **Driver-only operations**: Accept rides, complete rides
- **Passenger-only operations**: Request rides
- **Shared operations**: View rides, get ride details

---

## Error Handling

### Global Exception Handler

The GlobalExceptionHandler class provides consistent error responses across the API:

**Validation Errors**:
```json
{
  "message": "Validation failed",
  "errors": {
    "fromLocation": "need pickup location",
    "toLocation": "need drop location"
  },
  "status": "error"
}
```

**Authentication Errors**:
- HTTP 401: Unauthorized (missing/invalid token)
- HTTP 403: Forbidden (insufficient permissions)

**Business Logic Errors**:
- HTTP 400: Bad Request with descriptive message
- Examples: "email alredy exists", "Only drivers can accept rides"

---

## Testing Results

### Comprehensive Test Suite

All API endpoints have been thoroughly tested with the following scenarios:

#### Authentication Tests
✅ User registration (passenger)  
✅ User registration (driver)  
✅ Successful login with valid credentials  
✅ Failed login with invalid credentials  
✅ Duplicate email registration prevention  

#### Ride Management Tests
✅ Passenger creates new ride request  
✅ Driver views available rides  
✅ Driver accepts ride request  
✅ Driver completes ride  
✅ Passenger views ride history  
✅ User retrieves specific ride details  

#### Security Tests
✅ Unauthorized access blocked (no token)  
✅ JWT token validation  
✅ Role-based operation enforcement  

### Test Execution

A comprehensive test script (`test-apis.sh`) is included in the project root. To run all tests:

```bash
chmod +x test-apis.sh
./test-apis.sh
```

The script tests all endpoints in sequence, creating test users, simulating a complete ride lifecycle, and validating edge cases.

---

## Setup and Deployment

### Prerequisites

- Java Development Kit (JDK) 21 or higher
- Maven 3.6 or higher
- MongoDB Atlas account (or local MongoDB instance)

### Configuration

Update `src/main/resources/application.properties`:

```properties
# Application Identity
spring.application.name=ride-sharing-api

# Database Connection
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<database>
spring.data.mongodb.database=ridesharing

# Server Configuration
server.port=8080
```

### Running the Application

**Development Mode**:
```bash
./mvnw spring-boot:run
```

**Production Build**:
```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The application starts on `http://localhost:8080`

---

## API Usage Examples

### Complete Ride Request Flow

**Step 1: Passenger Registration**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "emailId": "passenger@example.com",
    "pass": "mypassword",
    "userRole": "ROLE_PASSENGER"
  }'
```

**Step 2: Request a Ride**
```bash
curl -X POST http://localhost:8080/api/rides/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <passenger-token>" \
  -d '{
    "fromLocation": "Airport Terminal 1",
    "toLocation": "Downtown Hotel Plaza"
  }'
```

**Step 3: Driver Registration**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "emailId": "driver@example.com",
    "pass": "driverpass",
    "userRole": "ROLE_DRIVER"
  }'
```

**Step 4: Driver Views Available Rides**
```bash
curl -X GET http://localhost:8080/api/rides/available \
  -H "Authorization: Bearer <driver-token>"
```

**Step 5: Driver Accepts Ride**
```bash
curl -X POST http://localhost:8080/api/rides/<ride-id>/accept \
  -H "Authorization: Bearer <driver-token>"
```

**Step 6: Driver Completes Ride**
```bash
curl -X POST http://localhost:8080/api/rides/<ride-id>/complete \
  -H "Authorization: Bearer <driver-token>"
```

---

## Best Practices and Recommendations

### For Frontend Developers

1. **Token Management**: Store JWT tokens securely (httpOnly cookies or secure storage)
2. **Token Refresh**: Implement token refresh logic before expiration
3. **Error Handling**: Display user-friendly messages for API errors
4. **Loading States**: Show loading indicators during API calls
5. **Polling**: Refresh available rides list periodically for drivers

### For Backend Maintenance

1. **Database Indexing**: Email field already indexed for performance
2. **Logging**: Implement comprehensive logging for debugging
3. **Monitoring**: Set up health checks and performance monitoring
4. **Scalability**: Consider implementing pagination for large ride lists
5. **Validation**: Add more specific validation rules as needed

### Security Considerations

1. **Token Storage**: Never expose JWT secret key
2. **HTTPS**: Use HTTPS in production environments
3. **Rate Limiting**: Implement rate limiting to prevent abuse
4. **Input Sanitization**: Validate all user inputs
5. **Password Policy**: Enforce strong password requirements

---

## Future Enhancements

### Potential Features

- **Real-time Updates**: WebSocket integration for live ride status
- **Location Tracking**: GPS coordinates for pickup/dropoff
- **Rating System**: Passenger and driver rating mechanism
- **Payment Integration**: Fare calculation and payment processing
- **Ride History**: Advanced filtering and search capabilities
- **Notifications**: Email/SMS alerts for ride status changes
- **Admin Dashboard**: Administrative management interface
- **Analytics**: Ride statistics and performance metrics

### Technical Improvements

- **Caching**: Redis integration for frequently accessed data
- **Message Queue**: Asynchronous processing for notifications
- **API Versioning**: Support multiple API versions
- **Documentation**: Interactive API documentation (Swagger/OpenAPI)
- **Testing**: Expand unit and integration test coverage
- **CI/CD**: Automated deployment pipelines

---

## Troubleshooting

### Common Issues

**Port Already in Use**:
```bash
# Find and kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

**MongoDB Connection Failed**:
- Verify MongoDB URI in application.properties
- Check network connectivity to MongoDB Atlas
- Ensure IP whitelist includes your address

**JWT Token Expired**:
- Re-authenticate to obtain new token
- Token validity is 10 hours from issuance

**Validation Errors**:
- Ensure all required fields are provided
- Check field names match exactly (case-sensitive)

---

## Project Information

**Author**: Shyam Krishnan  
**Course**: 2nd Year, Trimester-2, Spring Boot Project  
**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: December 6, 2025

---

## Conclusion

This Ride Sharing Platform backend provides a solid foundation for building a complete ride-sharing application. The system demonstrates modern backend development practices including RESTful API design, JWT authentication, MongoDB integration, and comprehensive error handling.

The modular architecture allows for easy extension and maintenance, while the comprehensive test suite ensures reliability. Whether you're building a mobile app, web application, or both, this backend provides all necessary endpoints to create a fully functional ride-sharing experience.

For questions, issues, or contributions, please refer to the project repository or contact the development team.

---

*This documentation is a living document and will be updated as the system evolves. Please check for the latest version when developing against this API.*
