# ADHD Focus Companion

> [!WARNING]
> **🚧 Work in Progress 🚧**
> This project is currently under active development. Expect frequent changes, breaking updates, and incomplete features. It is not yet ready for production use.

## About

A mental health toolkit designed with ADHD in mind. Instead of rigid time-blocking and planning, this app focuses on tools that work *with* ADHD brains: hyperfocus management through Pomodoro sessions and mood/pattern tracking to build self-awareness.

## Getting Started

To get this project up and running locally, you need a combination of Docker Compose for the database and direct pnpm and Java for the frontend and backend.

### Prerequisites

Ensure you have the following installed:

- **[Docker](https://docs.docker.com/get-started/get-docker/)**
- **[Docker Compose](https://docs.docker.com/compose/install/)**
- **[Git](https://git-scm.com/)**
- **[Pnpm](https://pnpm.io/)**
- **[Java 17 or later](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)**

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone [your-repo-url]
    cd mental-health-planner
    ```
2.  **Create `.env` files**:
    You'll need two `.env` files for the application to run correctly:

    * **Backend and PostgreSQL (`.env`)**:
      This file should be in the root `mental-health-planner` directory, alongside your `docker-compose.yml`. It will contain environment variables for both the Spring Boot backend and the PostgreSQL database.
        ```
        POSTGRES_USER=developer
        POSTGRES_PASSWORD=password
        POSTGRES_DB=mental_planner
        DB_USERNAME=developer
        DB_PASSWORD=password
        ```
    * **Frontend (`mental-planner-frontend/.env.local`)**:
        ```
        NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=your_clerk_publishable_key
        CLERK_SECRET_KEY=your_clerk_secret_key
        ```
3.  **Start the Database (PostgreSQL) with Docker Compose:**
    This command will build the container for the database and start it up on port 5432.
    ```bash
    docker-compose up --build
    ```
4.  **Run the Backend (Spring Boot):**
    Navigate into your backend project directory `cd mental-planner-backend`
    ```bash
    ./gradlew bootRun # On Linux/macOS
    gradlew bootRun # On Windows
    ```
5.  **Run the Frontend (Next.js):**
   Navigate to the frontend project directory `cd mental-planner-frontend`
   ```bash
   pnpm install # If you haven't installed dependencies yet
   pnpm run dev
   ```

6.  **Access the application:**
    * **Frontend:** `http://localhost:3000`
    * **Backend API:** `http://localhost:8080/api`

The application will now be running, and you can interact with the Pomodoro timer and save sessions.

---

## Deploying to Heroku

This repository is a monorepo, so deploy **two separate Heroku apps**:

- `mental-planner-backend` (Spring Boot API)
- `mental-planner-frontend` (Next.js web app)

### 1) Prerequisites

- Install the [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)
- Log in: `heroku login`

### 2) Create backend app

```bash
heroku create your-backend-app-name --buildpack heroku/java
heroku addons:create heroku-postgresql:mini -a your-backend-app-name
```

Set backend config vars:

```bash
heroku config:set CORS_ALLOWED_ORIGINS=https://your-frontend-app-name.herokuapp.com -a your-backend-app-name
heroku config:set CLERK_ISSUER_URL=your_clerk_issuer_url -a your-backend-app-name
heroku config:set CLERK_JWKS_URI=your_clerk_jwks_uri -a your-backend-app-name
```

> The backend is configured to use Heroku's `PORT` and Postgres `JDBC_DATABASE_*` variables automatically.

### 3) Create frontend app

```bash
heroku create your-frontend-app-name --buildpack heroku/nodejs
```

Set frontend config vars:

```bash
heroku config:set NEXT_PUBLIC_API_URL=https://your-backend-app-name.herokuapp.com -a your-frontend-app-name
heroku config:set NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=your_clerk_publishable_key -a your-frontend-app-name
heroku config:set CLERK_SECRET_KEY=your_clerk_secret_key -a your-frontend-app-name
```

### 4) Add Heroku remotes

```bash
heroku git:remote -a your-backend-app-name --remote heroku-backend
heroku git:remote -a your-frontend-app-name --remote heroku-frontend
```

### 5) Deploy from monorepo subfolders

Deploy backend:

```bash
git subtree push --prefix mental-planner-backend heroku-backend main
```

Deploy frontend:

```bash
git subtree push --prefix mental-planner-frontend heroku-frontend main
```

### 6) Open apps

```bash
heroku open -a your-frontend-app-name
heroku open -a your-backend-app-name
```

---

## Features

### Current Features
- **🍅 Pomodoro Timer & Session Tracking:** Manage hyperfocus sessions with customizable timers. Rate each session (1-5) to track productivity patterns.
- **📊 Mood Tracker:** Daily mood logging with customizable factors and notes. Identify patterns and triggers over time.
- **💼 Job Search Tracker:** Keep a simple table of applications with company name, role title, and current status.
- **🎨 Clean, Responsive UI:** Built with Next.js and shadcn/ui components for a distraction-free experience.
- **🔒 User Authentication:** Powered by Clerk for secure, personalized tracking.
- **🐳 Dockerized:** Easy environment setup with Docker Compose and PostgreSQL.

### Planned ADHD-Friendly Features
- **Flexible To-Do Lists:** Non-time-blocked task capture with priority tagging (no rigid schedules!)
- **Dopamine Tracking:** Log activities and their impact on your energy/motivation
- **Hyperfocus Activity Logger:** Track what activities trigger flow states
- **Body Doubling Support:** Virtual co-working session timer
- **Habit Streaks (Forgiving):** Track habits with ADHD-friendly grace periods
- **Pattern Recognition Dashboard:** Visualize connections between mood, activities, and productivity

---

## 🛠️ Technologies

* **Frontend:** Next.js 14, TypeScript, React, shadcn/ui, Tailwind CSS
* **Backend:** Spring Boot, Java 17+, Spring Data JPA, Lombok
* **Database:** PostgreSQL with Flyway migrations
* **Authentication:** Clerk
* **Containerization:** Docker, Docker Compose
* **Testing:** JUnit, Testcontainers

---

## Stopping the Application

To stop the services:

1. Frontend: Go to the terminal running pnpm run dev and press Ctrl+C.
2. Backend: Go to the terminal running gradlew bootRun and press Ctrl+C.
3. Database: Go to the terminal where you ran docker-compose up -d postgres and run:

```bash
docker-compose down # This will stop all services defined in docker-compose
```