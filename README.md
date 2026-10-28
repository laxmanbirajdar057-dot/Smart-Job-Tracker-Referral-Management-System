# Smart Job Tracker & Referral Management System

A web app to keep track of your job applications and the people who refer you. Built with Java, Spring Boot, and MySQL.

**Live Demo:** https://smart-job-tracker-referral-management-system-production.up.railway.app/

## What It Does

- **Track applications:** Add, edit, and delete jobs with company, role, status, location, salary, and deadline.
- **Track status:** Move each job through Saved, Applied, Screening, Technical Round, HR Round, Offer, Negotiating, or Rejected.
- **Manage referrals:** Save referral contacts and link them to jobs.
- **Dashboard:** See your total applications, offers, rejections, and referrals at a glance.
- **Filters:** Filter jobs by status or by referral.
- **Login:** Sign up and log in securely with JWT. Each user sees only their own data.
- **Analytics:** See your application funnel and compare how referrals and normal applications perform.
- **Resume match score:** Upload a resume and see how many keywords match a job description.
- **AI resume feedback:** Get strengths, gaps, and suggestions from the Gemini API.
- **ATS check:** Check if your resume is easy for ATS software to read.
- **Follow-up reminders:** Find applications with no update for 7+ days or a deadline coming soon. A daily scheduled job checks this.

## Tech Used

- **Backend:** Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA, MySQL
- **Extras:** Apache Tika (reads resume text), Google Gemini API (AI feedback), Spring Cache, Spring Scheduling
- **Frontend:** HTML, CSS, JavaScript, Thymeleaf
- **Deployment:** Railway, GitHub Actions

## Run It on Your Computer

**You need:** Java 17+, Maven, and MySQL. A free [Gemini API key](https://aistudio.google.com) is optional and only needed for AI feedback.

1. Clone the project:
```bash
git clone https://github.com/laxmanbirajdar057-dot/Smart-Job-Tracker-Referral-Management-System.git
cd Smart-Job-Tracker-Referral-Management-System
```

2. Create the database:
```sql
CREATE DATABASE job_tracker;
```

3. Create the file `src/main/resources/application-local.yml` with your own values. This file is in `.gitignore`, so it never goes to GitHub.
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/job_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: YOUR_MYSQL_PASSWORD

jwt:
  secret: YOUR_RANDOM_SECRET_AT_LEAST_64_CHARACTERS

gemini:
  api-key: YOUR_GEMINI_API_KEY
```

4. Start the app with the `local` profile:
```bash
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

5. Open http://localhost:8080 in your browser.

## Environment Variables (for Railway)

| Variable | Needed? | Used for |
|---|---|---|
| `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` | Yes | Connecting to the database |
| `JWT_SECRET` | Yes | Signing login tokens. Must be at least 64 characters. |
| `GEMINI_API_KEY` | No | AI resume feedback only. Everything else works without it. |

Never put real passwords or keys in files that go to GitHub. If one is ever committed by mistake, create a new one. Deleting it later does not remove it from Git history.

## Main API Endpoints

| Method | Endpoint | What it does |
|--------|----------|--------------|
| POST | `/api/auth/signup` | Create an account |
| POST | `/api/auth/login` | Log in and get a token |
| GET, POST | `/api/jobs` | List jobs, add a job |
| PATCH, DELETE | `/api/jobs/{id}` | Update or delete a job |
| GET | `/api/jobs/analytics` | Funnel and referral vs cold-apply results |
| GET | `/api/jobs/follow-ups` | Jobs that need a follow-up |
| GET | `/api/jobs/{jobId}/match-score?resumeId=` | Resume match score |
| GET | `/api/jobs/{jobId}/ats-check?resumeId=` | ATS check |
| GET | `/api/jobs/{jobId}/ai-feedback?resumeId=` | AI resume feedback |
| GET, POST | `/api/referrals` | List referrals, add a referral |
| PATCH, DELETE | `/api/referrals/{id}` | Update or delete a referral |
| GET | `/api/referrals/stats` | Referral statistics |
| GET, POST | `/api/resumes` | List resumes, upload a resume |
| DELETE | `/api/resumes/{id}` | Delete a resume |

All endpoints except signup and login need a valid JWT token.

## Deployment

The app runs on Railway with a MySQL database. Pushing to `main` redeploys it automatically. Set the environment variables above in Railway's Variables tab.

## Author

Laxman Birajdar
[LinkedIn](https://www.linkedin.com/in/laxman-birajdar) | [GitHub](https://github.com/laxmanbirajdar057-dot)
