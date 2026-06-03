# Notification Sending System

A Spring Boot 3.x application for a task about notification delivery and history tracking.

## Problem Statement

The system covers these scenarios:

- User registers
- User logs in
- Notification is sent for the user
- Notification history is stored in the database

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring Security + JWT
- Spring Data JPA
- H2 Database
- Spring Cache with Caffeine
- Docker
- JUnit 5, Mockito, MockMvc

## Features

- Register and login endpoints with JWT authentication
- Role-based access control with `ROLE_USER` and `ROLE_ADMIN`
- Notification channels:
  - `EMAIL`
  - `SMS`
  - `IN_APP`
- Notification history persistence in H2
- JSON error responses for `401` and `403`
- Simple cache for user lookup and notification history
- Seeded admin user for demo and review

## Default Credentials

### Admin

- Username: `admin`
- Password: `Admin123!`
- Email: `admin@example.com`

### Local user example

Register a normal user through `POST /auth/register`, then log in with the same username and password.

## Configuration

### Shared properties

`src/main/resources/application.properties`

- Server port: `8082`
- JWT secret and expiration
- Bootstrap admin settings
- Cache settings

### Profiles

- `application.properties`
- `application-test.properties`

Use `SPRING_PROFILES_ACTIVE=test` when running the test suite manually if needed.

## Run Locally

```bash
./mvnw spring-boot:run
```

The app runs on:

```text
http://localhost:8082
```

## Run Tests

```bash
./mvnw test
```

## Run With Docker

Run the Application

To run the project locally using Docker:

```bash
docker pull honeloper/notification-system
docker run -p 8082:8082 honeloper/notification-system
```

## Main API Endpoints

### Auth

- `POST /auth/register`
- `POST /auth/login`

### Notifications

- `POST /notifications`
- `GET /notifications/me`
- `GET /notifications/{id}`

### Admin only

- `GET /notifications/admin/all`
- `GET /notifications/admin/users/{username}`

## Example Requests

### Register

```http
POST /auth/register
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "password": "Password123!"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "Password123!"
}
```

### Send notification

```http
POST /notifications
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "channel": "EMAIL",
  "title": "Order created",
  "message": "Your order has been created successfully."
}
```

## Error Responses

The API returns JSON for common security errors:

- `401 Unauthorized`
- `403 Forbidden`

Example:

```json
{
  "timestamp": "2026-06-03T15:40:00.000",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have access to this resource.",
  "path": "/notifications/admin/all"
}
```

## Testing Notes

The test suite includes:

- Unit tests for JWT, auth service, cache behavior, and notification service
- Integration tests for register/login, JWT flow, security rules, admin access, and H2 persistence

## Coverage Map

- Register flow: `AuthControllerIT`, `AuthServiceTest`
- Login flow: `AuthControllerIT`, `AuthServiceTest`
- Notification creation: `NotificationControllerIT`, `NotificationServiceTest`
- Notification history persistence: `NotificationControllerIT`, `NotificationServiceTest`
- JWT generation/validation: `JwtServiceTest`
- Role-based access: `NotificationControllerIT`
- 401/403 JSON handling: `NotificationControllerIT`
- Cache behavior: `UserQueryServiceTest`, `NotificationServiceTest`
