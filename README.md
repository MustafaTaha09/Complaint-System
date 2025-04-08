# Complaint System API ⚙️

A robust RESTful API backend for managing customer/user complaints and support tickets, built with Java and the Spring Boot framework. This system provides features for ticket creation, assignment, status tracking, user management, role-based access control, and more.

## Overview ✨

This project implements a complete backend solution for a complaint management workflow. Users can submit tickets, administrators can manage users, roles, departments, and ticket assignments, and secure authentication is handled via JWT (JSON Web Tokens) with refresh token capabilities. The API is documented using Swagger/OpenAPI for easy understanding and interaction.

## Features Implemented 🚀

* **Ticket Management:**
    * CRUD operations (Create, Read, Update, Delete) for Tickets.
    * Partial updates (PATCH) for tickets.
    * Assignment of tickets to users/departments (via `TicketAssignment`).
    * Management of Ticket Statuses.
    * Ability to add Comments to tickets.
    * Paginated retrieval of tickets (Admin only).
    * Simplified Ticket History endpoint (showing creation/update times and comments).
* **User Management:**
    * User Registration (`/api/auth/register` - Public).
    * User Login (`/api/auth/login`) returning JWT access and refresh tokens.
    * Token Refresh (`/api/auth/refresh`).
    * User Logout (`/api/auth/logout`) - invalidates refresh token.
    * CRUD operations for Users (Admin or self-service for some operations).
    * Secure Password Change endpoint.
    * Admin-only Username Change endpoint.
    * Retrieval of user profiles and detailed views (including tickets/comments).
* **Role Management (Admin Only):**
    * CRUD operations for Roles (e.g., ROLE_USER, ROLE_ADMIN).
    * Prevents deletion of roles currently assigned to users.
* **Department Management (Admin Only):**
    * CRUD operations for Departments.
    * Prevents deletion of departments assigned to users or tickets.
* **Security:**
    * JWT-based Authentication (Access Tokens + Refresh Tokens).
    * Role-Based Authorization using Spring Security (`@PreAuthorize`, `hasRole`).
    * Secure Password Hashing (BCrypt).
* **API Documentation:**
    * Interactive API documentation via Swagger UI (`springdoc-openapi`).
* **Logging:**
    * Structured logging using SLF4J and Logback.
    * Configurable logging levels (e.g., for different environments).
* **Error Handling:**
    * Global exception handling (`@ControllerAdvice`) for consistent error responses (4xx/5xx).
* **Testing:**
    * Includes examples/setup for Unit Testing (JUnit 5, Mockito) and Integration Testing (`@SpringBootTest`, MockMvc, H2).

## Technologies Used 🛠️

* **Java:** Version 17+
* **Framework:** Spring Boot 3.x
* **Web:** Spring Web (MVC) for REST controllers
* **Data:** Spring Data JPA, Hibernate
* **Database:** SQL Server (for development/production - requires setup), H2 (for integration tests)
* **Security:** Spring Security (JWT, Refresh Tokens, BCrypt, Method Security)
* **API Documentation:** `springdoc-openapi` (Swagger UI)
* **Logging:** SLF4J, Logback (via `spring-boot-starter-logging`)
* **Utilities:** Lombok
* **Build Tool:** Maven (or Gradle)
* **Testing:** JUnit 5, Mockito, Spring Boot Test, `spring-security-test`

## Getting Started 🏁

### Prerequisites

* Java Development Kit (JDK) 17 or later
* Apache Maven (or Gradle)
* An instance of SQL Server (or configure `application.properties` for a different database like H2 or PostgreSQL for development)
* Git

### Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd <repository-directory>
    ```
2.  **Configure Database:**
    * Open `src/main/resources/application.properties`.
    * Update the `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` properties to match your database setup.
    ```properties
    # Example for SQL Server
    spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=complaint_system_db;encrypt=true;trustServerCertificate=true;
    spring.datasource.username=your_db_user
    spring.datasource.password=your_db_password
    spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

    spring.jpa.hibernate.ddl-auto=validate # Recommended after initial setup/migrations
    ```
3.  **Configure JWT Secret:**
    * **Important:** Change the `jwt.secret` property in `application.properties` to a strong, unique, Base64-encoded secret key (at least 256 bits). **Do not use the placeholder in production.**
    ```properties
    jwt.secret=your-very-strong-randomly-generated-base64-encoded-secret-key
    jwt.expirationMs=120000 # 2 minutes (for testing)
    jwt.refreshExpirationMs=604800000 # 7 days
    ```
4.  **Database Schema:**
    * The necessary SQL scripts to create tables (including `refresh_tokens`, `user_roles`, etc.) should be run manually against your database *or* managed using a migration tool like Flyway/Liquibase (recommended). Ensure `spring.jpa.hibernate.ddl-auto` is set appropriately (`validate` or `none` if using migrations). Refer to the generated SQL/DBML in previous discussions if needed.
5.  **Build the project:**
    ```bash
    mvn clean install
    # OR using Gradle: ./gradlew clean build
    ```
6.  **Run the application:**
    ```bash
    mvn spring-boot:run
    # OR using Gradle: ./gradlew bootRun
    # OR run the main application class from your IDE
    ```
    The application should start on `http://localhost:8080` (or the configured port).

## API Documentation (Swagger UI) 📖

Once the application is running, you can access the interactive API documentation via Swagger UI:

* **URL:** `http://localhost:8080/api-documentation` (or `/swagger-ui.html` if you used the default path).

You can explore endpoints, view schemas, and even try out API calls directly. Use the "Authorize" button to authenticate using a JWT token obtained from the login endpoint.

## Authentication Flow 🔑

1.  **Login:** Send a `POST` request to `/api/auth/login` with username and password in the request body.
    * **Success:** Returns a `200 OK` with an `accessToken` (short-lived) and a `refreshToken` (long-lived).
    * **Failure:** Returns `401 Unauthorized`.
2.  **Accessing Secured Endpoints:** Include the obtained `accessToken` in the `Authorization` header for subsequent requests:
    ```
    Authorization: Bearer <your_access_token>
    ```
3.  **Token Refresh:** If your `accessToken` expires (you'll likely get a `401 Unauthorized`), send a `POST` request to `/api/auth/refresh` with your `refreshToken` in the request body.
    * **Success:** Returns a `200 OK` with a *new* `accessToken` (and the same `refreshToken`). Use this new access token for future requests.
    * **Failure:** Returns `403 Forbidden` if the refresh token is invalid or expired. You need to log in again with credentials.
4.  **Logout:** Send a `POST` request to `/api/auth/logout` (with a valid access token in the header). This invalidates the refresh token on the server. The client should then discard both tokens.

## Running Tests 🧪

Execute the unit and integration tests using:

```bash
mvn test
# OR using Gradle: ./gradlew test
