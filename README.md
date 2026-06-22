# Smart Job Tracker & Referral Management System

A full-stack job application tracking system built with Spring Boot and vanilla JavaScript. Track your job applications, manage referral contacts, and monitor your job search progress — all in one place.

## Features

- **Application Tracking** — Add, edit, and delete job applications with details like company, role, status, job type, location, salary, and deadline
- **Referral Management** — Track referral contacts separately and link them to job applications
- **Status Tracking** — Track application progress across stages: Saved, Applied, Screening, Technical Round, HR Round, Offer, Negotiating, Rejected
- **Dashboard Stats** — See total applications, offers, rejections, and referrals at a glance
- **Filters** — Filter applications by status or referral status
- **JWT Authentication** — Secure login and signup with token-based auth
- **Referral Network Tab** — Manage your referral contacts independently from job applications

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- MySQL

**Frontend**
- HTML, CSS, Vanilla JavaScript (no frameworks)
- Thymeleaf (for page routing)

## Project Structure

```
src/
├── main/
│   ├── java/com/job/tracker/
│   │   ├── controller/        # REST controllers (Auth, Job, Referral)
│   │   ├── service/           # Business logic
│   │   ├── entity/            # JPA entities (User, Job, Referral)
│   │   ├── repository/        # Spring Data repositories
│   │   ├── dto/               # Request and response DTOs
│   │   ├── security/          # JWT filter and token provider
│   │   └── config/            # Security configuration
│   └── resources/
│       ├── static/            # CSS and JavaScript
│       ├── templates/         # HTML pages
│       └── application.yml    # App configuration
```

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL

### Setup

1. Clone the repository
```bash
git clone https://github.com/laxmanbirajdar057-dot/Job_Tracker.git
cd Job_Tracker
```

2. Create a MySQL database
```sql
CREATE DATABASE job_tracker;
```

3. Update `src/main/resources/application.yml` with your DB credentials
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/job_tracker
    username: your_username
    password: your_password
```

4. Run the application
```bash
./mvnw spring-boot:run
```

5. Open your browser and go to `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |
| GET | `/jobs` | Get all jobs for logged-in user |
| POST | `/jobs` | Create a new job |
| PATCH | `/jobs/{id}` | Update a job |
| DELETE | `/jobs/{id}` | Delete a job |
| GET | `/referrals` | Get all referral contacts |
| POST | `/referrals` | Add a referral contact |
| PATCH | `/referrals/{id}` | Update a referral contact |
| DELETE | `/referrals/{id}` | Delete a referral contact |
| GET | `/referrals/stats` | Get referral statistics |

## Deployment

Deployment coming soon — planning to host on Railway (backend + MySQL) and serve the frontend via Spring Boot's static resources.

## Author

Laxman Birajdar  
[LinkedIn](https://www.linkedin.com/in/laxmanbirajdar) · [GitHub](https://github.com/laxmanbirajdar057-dot)
