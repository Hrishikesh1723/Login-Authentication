# Spring Boot JWT Authentication System

## Overview
This project is a Spring Boot-based authentication system that uses JWT (JSON Web Token) for user authentication. It provides login, token validation, session tracking, and logout functionalities.

## Features
- User authentication with username and password
- JWT token generation and validation
- Session tracking with browser-specific sessions
- Stateless authentication using Spring Security
- CORS configuration for cross-origin requests
- Secure API endpoints with authentication checks

## Technologies Used
- Spring Boot
- Spring Security
- JWT (JSON Web Token)
- SLF4J for logging
- ConcurrentHashMap for session management

## Project Structure
```
├── config
│   ├── JwtUtil.java (Handles JWT operations)
│   ├── SecurityConfig.java (Configures security settings)
│   ├── WebConfig.java (Configures CORS settings)
│
├── controller
│   ├── AuthController.java (Handles authentication APIs)
│   ├── HomeController.java (Handles home redirection)
│
├── service
│   ├── AuthService.java (Handles user authentication logic)
│
├── model (Contains request and response models for API communication)
│
├──Resources
│   ├── application.properties (Spring Boot configuration file)
│   ├──Static
│       ├──index.html
```

## API Endpoints
### 1. **User Login**
- **Endpoint:** `POST /v1/auth/login`
- **Request Body:**
  ```json
  {
    "username": "user",
    "password": "password",
    "browserId": "browser-unique-id"
  }
  ```
- **Response:**
  ```json
  {
    "token": "<JWT-TOKEN>",
    "username": "user",
    "browserId": "browser-unique-id",
    "counter": 0,
    "error": false
  }
  ```

### 2. **Token Validation**
- **Endpoint:** `GET /v1/auth/validate`
- **Parameters:** `token`, `browserId`
- **Response:**
  ```json
  {
    "token": "<JWT-TOKEN>",
    "username": "user",
    "browserId": "browser-unique-id",
    "counter": 0,
    "error": false
  }
  ```

### 3. **Increment Counter**
- **Endpoint:** `POST /v1/auth/increment`
- **Request Body:**
  ```json
  { "token": "<JWT-TOKEN>" }
  ```
- **Response:**
  ```json
  { "count": 1, "success": true }
  ```

### 4. **User Logout**
- **Endpoint:** `POST /v1/auth/logout`
- **Request Body:**
  ```json
  {
    "username": "user",
    "browserId": "browser-unique-id",
    "logoutAll": false
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "message": "Logged out from this browser only"
  }
  ```

## Setup Instructions
### 1. Clone the Repository
```
git clone <repository-url>
cd <project-directory>
```
### 2. Run the Application
```
mvn spring-boot:run
```
### 3. How to Run

- Frontend: Open index.html in a browser.
- Login: Enter username & password, and log in.
- Increment Counter: Click the button to increase the counter.
- Logout: Choose to log out from the current or all browsers.



## Security Considerations
- The JWT token is stored securely in memory or local storage.
- CORS settings allow requests from any origin; modify it as needed for security.
- Users should implement proper password hashing and database authentication instead of hardcoded users.

## Future Improvements
- Implement a database-backed authentication system
- Use OAuth2 for better authentication and authorization
- Enhance token security with refresh tokens


