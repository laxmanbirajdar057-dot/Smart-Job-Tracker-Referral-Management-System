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
- **Analytics** — Application funnel breakdown and referral vs. cold-apply offer-rate comparison, computed server-side from real data
- **Resume ↔ JD Match Score** — Keyword-overlap scoring between a resume and a specific job's description, with matched/missing skills
- **AI Resume Feedback** — LLM-generated (Gemini) qualitative assessment of resume fit for a specific role/JD, with strengths, gaps, and concrete suggestions
- **ATS Check** — Role/JD-aware resume parseability check: file format, contact info, section headers, word count, and job-specific keyword coverage
- **Follow-up Reminders** — Automatic detection of stale applications (7+ days untouched) or approaching deadlines, backed by a daily scheduled job

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- MySQL
- Apache Tika (resume text extraction)
- Google Gemini API (AI resume feedback)
- Spring Cache (in-memory, for AI feedback results)
- Spring Scheduling (follow-up automation)

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
- (Optional, for AI Feedback) A free Gemini API key from [aistudio.google.com](https://aistudio.google.com)

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

3. Configure your local secrets

`application.yml` is committed to this repo and contains **no real secrets** — every sensitive value is an environment variable placeholder. To run locally, create `src/main/resources/application-local.yml` (this file is gitignored and never committed) with your real values:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/job_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: YOUR_ACTUAL_MYSQL_PASSWORD

jwt:
  secret: YOUR_OWN_RANDOM_64_CHAR_SECRET

gemini:
  api-key: YOUR_ACTUAL_GEMINI_API_KEY
  model: gemini-3.5-flash
  base-url: https://generativelanguage.googleapis.com/v1beta/models
```

A template with the same shape (safe to look at, no real values) is provided at `application-local.yml.example` — copy it and fill in your own values:
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

4. Run the application with the `local` profile active
```bash
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

5. Open your browser and go to `http://localhost:8080/dashboard`

### Environment Variables Reference

| Variable | Required | Used for |
|---|---|---|
| `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` | Yes | Database connection (production/Railway; local dev uses `application-local.yml` instead) |
| `JWT_SECRET` | Yes | Signs authentication tokens — generate your own random 64-char value, never reuse the one from this history |
| `GEMINI_API_KEY` | No — only for AI Feedback | Calls Google's Gemini API for LLM-generated resume feedback. Get a free key at [aistudio.google.com](https://aistudio.google.com). Everything else (Match Score, ATS Check, Analytics, Reminders) works without this |

**Never commit real values for any of the above.** If a real secret is ever accidentally committed, rotate it (generate a new one) rather than assuming deleting it from a later commit is enough — old commits keep it in Git history regardless.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |
| GET | `/api/jobs` | Get all jobs for logged-in user |
| POST | `/api/jobs` | Create a new job |
| PATCH | `/api/jobs/{id}` | Update a job |
| DELETE | `/api/jobs/{id}` | Delete a job |
| GET | `/api/jobs/analytics` | Application funnel + referral vs. cold-apply offer-rate comparison |
| GET | `/api/jobs/follow-ups` | Jobs needing a follow-up (stale 7+ days, or deadline within 3 days) |
| GET | `/api/jobs/{jobId}/match-score?resumeId=` | Resume ↔ JD keyword match score |
| GET | `/api/jobs/{jobId}/ats-check?resumeId=` | Role/JD-aware ATS parseability check |
| GET | `/api/jobs/{jobId}/ai-feedback?resumeId=` | LLM-generated resume feedback for this job |
| GET | `/api/referrals` | Get all referral contacts |
| POST | `/api/referrals` | Add a referral contact |
| PATCH | `/api/referrals/{id}` | Update a referral contact |
| DELETE | `/api/referrals/{id}` | Delete a referral contact |
| GET | `/api/referrals/stats` | Get referral statistics |
| POST | `/api/resumes` | Upload a resume |
| GET | `/api/resumes` | List all resumes |
| DELETE | `/api/resumes/{id}` | Delete a resume |

## Deployment

Deployed on Railway (backend + MySQL) with GitHub auto-deploy — pushing to `main` triggers a redeploy. Set all environment variables listed above in Railway's Variables tab for the deployed service; Railway does not use `application-local.yml` (that file is local-dev only).

## Author

Laxman Birajdar  
[LinkedIn](https://www.linkedin.com/in/laxmanbirajdar) · [GitHub](https://github.com/laxmanbirajdar057-dot)