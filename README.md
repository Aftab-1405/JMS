# Journal App

A secure REST API for personal journal management built with Spring Boot and MongoDB.

## Overview

Journal App lets users create, read, update, and delete personal journal entries. Each user's data is isolated—you can only access your own journals. The app uses role-based access control with `USER` and `ADMIN` roles.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.4 |
| Language | Java 17 |
| Database | MongoDB Atlas |
| Security | Spring Security (Basic Auth + BCrypt) |
| Build | Maven |

## Features

- **User Registration** — Public endpoint for self-signup
- **Journal CRUD** — Create, read, update, delete entries
- **User Management** — Update credentials, delete account
- **Admin Panel** — View all users, create admin accounts
- **Password Encryption** — BCrypt hashing
- **Ownership Validation** — Users can only access their own journals

## API Endpoints

### Public (No Auth)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/public/create-user` | Register new user |

### User (Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/journal` | Get all your journals |
| POST | `/journal` | Create new journal |
| GET | `/journal/id/{id}` | Get specific journal |
| PUT | `/journal/id/{id}` | Update journal |
| DELETE | `/journal/id/{id}` | Delete journal |
| PUT | `/user` | Update your credentials |
| DELETE | `/user` | Delete your account |

### Admin (ADMIN Role)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/all-users` | List all users |
| POST | `/admin/create-admin` | Create admin account |

## Project Structure

```
src/main/java/com/abnalliance/journalapp/
├── configuration/
│   └── SpringSecurity.java      # Security config
├── controller/
│   ├── AdminController.java     # Admin endpoints
│   ├── JournalEntryController.java  # Journal CRUD
│   ├── PublicController.java    # Registration
│   └── UserController.java      # User management
├── entity/
│   ├── JournalEntry.java        # Journal model
│   └── Users.java               # User model
├── repository/
│   ├── JournalEntryRepository.java
│   └── UserRepository.java
├── service/
│   ├── JournalEntryService.java
│   ├── UserDetailServiceImp.java  # Spring Security integration
│   └── UserService.java
└── JournalappApplication.java
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB instance (local or Atlas)

### Configuration

Update `src/main/resources/application-dev.yml` or `application-prod.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: your-mongodb-connection-string
      database: journaldb
      auto-index-creation: true
```

### Run Locally

```bash
# Development (port 8080)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production (port 9090)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Build

```bash
./mvnw clean package
java -jar target/journalapp-0.0.1-SNAPSHOT.jar
```

## Example Requests

### Register User

```bash
curl -X POST http://localhost:8080/journal/public/create-user \
  -H "Content-Type: application/json" \
  -d '{"userName": "john", "password": "secret123"}'
```

### Create Journal Entry

```bash
curl -X POST http://localhost:8080/journal/journal \
  -u john:secret123 \
  -H "Content-Type: application/json" \
  -d '{"title": "My First Entry", "content": "Started journaling today!"}'
```

### Get All Journals

```bash
curl http://localhost:8080/journal/journal -u john:secret123
```

## Security

- All passwords stored as BCrypt hashes
- HTTP Basic Authentication
- Role-based endpoint protection:
  - `/public/**` — Open
  - `/journal/**`, `/user/**` — Authenticated users
  - `/admin/**` — ADMIN role only
- Users can only access their own journal entries


