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
- **[Java 21 or later](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)**

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone [your-repo-url]
    cd "Mental Health App"
    ```
2.  **Create `.env` files**:
    You'll need two `.env` files for the application to run correctly:

    * **Backend and PostgreSQL (`.env`)**:
      Copy `.env.example` to `.env` in the repo root, alongside `docker-compose.yml`. It contains the PostgreSQL credentials shared by the database and the Spring Boot backend:
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
3.  **Start PostgreSQL with Docker Compose:**
    This starts just the database on port 5432, for use with the backend and frontend running directly on your machine.
    ```bash
    docker compose -f docker-compose-postgres.yml up -d
    ```
4.  **Run the Backend (Spring Boot):**
    Navigate into the backend project directory `cd mental-planner-backend`
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
    * **Backend API:** `http://localhost:8080` (endpoints under `/api/**`, e.g. `/api/pomodoro`, `/api/mood`)

The application will now be running, and you can interact with the Pomodoro timer and mood tracker.

> [!NOTE]
> `docker-compose.yml` in the repo root builds and runs the **full stack** (database, backend, and frontend) in containers instead — useful for a production-like smoke test: `docker compose up --build`.

---

## Testing

**Frontend** (from `mental-planner-frontend`):
```bash
pnpm test:unit  # Vitest unit tests
pnpm test:e2e   # Playwright end-to-end tests
```

**Backend** (from `mental-planner-backend`):
```bash
./gradlew test # On Linux/macOS
gradlew test # On Windows
```
Backend integration tests use Testcontainers, so Docker must be running.

---

## Deploying

- **Backend** (`mental-planner-backend`, Spring Boot API) — Heroku, deployed automatically via GitHub Actions (see below)
- **Frontend** (`mental-planner-frontend`, Next.js web app) — [Vercel](https://vercel.com), deployed automatically on push (configured in the Vercel dashboard, not in this repo)

### Backend: automated via GitHub Actions

Pushes to `master` that touch `mental-planner-backend/**` trigger [`.github/workflows/deploy-backend-heroku.yml`](.github/workflows/deploy-backend-heroku.yml), which builds `mental-planner-backend/Dockerfile`, pushes the image to Heroku's container registry, and releases it via the Platform API. It can also be run manually from the Actions tab (`workflow_dispatch`).

**One-time setup** (already done for `planner-backend`, keep for reference/new environments):

```bash
heroku create your-backend-app-name
heroku stack:set container -a your-backend-app-name
heroku addons:create heroku-postgresql:mini -a your-backend-app-name
```

Set backend config vars:

```bash
heroku config:set CORS_ALLOWED_ORIGINS=https://your-frontend-app.vercel.app -a your-backend-app-name
heroku config:set CLERK_ISSUER_URL=your_clerk_issuer_url -a your-backend-app-name
heroku config:set CLERK_JWKS_URI=your_clerk_jwks_uri -a your-backend-app-name
```

> The app's stack **must** be `container` — the workflow deploys by pushing a Docker image, not a buildpack build. The backend itself is configured to use Heroku's `PORT` and Postgres `JDBC_DATABASE_*`/`DATABASE_URL` variables automatically (`docker-entrypoint.sh` parses `DATABASE_URL` into the JDBC vars Spring expects).

Add these as **GitHub repo secrets** (Settings → Secrets and variables → Actions) so the workflow can authenticate:

```bash
heroku authorizations:create -d "github-actions-planner-backend-deploy"
# copy the Token value from the output, then:
gh secret set HEROKU_API_KEY --repo <owner>/<repo>   # paste the token when prompted
gh secret set HEROKU_APP_NAME --repo <owner>/<repo> --body "your-backend-app-name"
```

After that, every push to `master` under `mental-planner-backend/**` deploys automatically — no manual `git subtree push` needed for the backend.

### Frontend: Vercel

The frontend deploys via Vercel's own GitHub integration (root directory set to `mental-planner-frontend`) — pushes to `master` deploy automatically, no workflow file needed. Set these as Vercel project environment variables (Project Settings → Environment Variables), not in this repo:

- `NEXT_PUBLIC_API_URL` — the backend's Heroku URL
- `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY`
- `CLERK_SECRET_KEY`

---

## Features

### Current Features
- **🗂️ Weekly Task Planner:** Drag-and-drop kanban board across the week, with subtasks, priority tagging, and a detail view for each task — non-rigid, ADHD-friendly task capture.
- **🍅 Pomodoro Timer & Session Tracking:** Manage hyperfocus sessions with customizable timers. Rate each session (1-5) to track productivity patterns. Timer state persists globally with a live countdown in the navbar.
- **📊 Mood Tracker:** Daily mood logging with customizable factors and notes. Identify patterns and triggers over time.
- **🌓 Dark Mode:** Catppuccin-themed light/dark toggle.
- **📶 Offline Indicator:** Badge in the page header flags when the app loses connectivity.
- **🎨 Clean, Responsive UI:** Built with Next.js and shadcn/ui components for a distraction-free experience.
- **🔒 User Authentication:** Powered by Clerk for secure, personalized tracking. Signing out clears locally cached task data so it isn't left behind on shared devices.
- **🐳 Dockerized:** Easy environment setup with Docker Compose and PostgreSQL.

### Planned ADHD-Friendly Features
- **Dopamine Tracking:** Log activities and their impact on your energy/motivation
- **Hyperfocus Activity Logger:** Track what activities trigger flow states
- **Body Doubling Support:** Virtual co-working session timer
- **Habit Streaks (Forgiving):** Track habits with ADHD-friendly grace periods
- **Pattern Recognition Dashboard:** Visualize connections between mood, activities, and productivity

---

## 🛠️ Technologies

* **Frontend:** Next.js 16, TypeScript, React 19, shadcn/ui (Radix primitives), Tailwind CSS 4, TanStack Query, dnd-kit
* **Backend:** Spring Boot 4.1, Java 21, Spring Data JPA, Spring Security (OAuth2 resource server), Lombok
* **Database:** PostgreSQL with Flyway migrations
* **Authentication:** Clerk (JWT validated by the backend as an OAuth2 resource server)
* **Containerization:** Docker, Docker Compose
* **Testing:** Vitest and Playwright (frontend); JUnit and Testcontainers (backend)

---

## Stopping the Application

To stop the services:

1. Frontend: Go to the terminal running `pnpm run dev` and press Ctrl+C.
2. Backend: Go to the terminal running `gradlew bootRun` and press Ctrl+C.
3. Database:
    ```bash
    docker compose -f docker-compose-postgres.yml down
    ```