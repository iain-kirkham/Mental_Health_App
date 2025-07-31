# Mental Health Planner

> [!WARNING]
> **🚧 Work in Progress 🚧**
> This project is currently under active development. Expect frequent changes, breaking updates, and incomplete features. It is not yet ready for production use.

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

## Features (Current & Planned)

- **Pomodoro Timer & Session Tracking:** Save session duration and a 1-5 rating.
- **Mood Tracker** Rate days from very bad to very good, with factors affecting mood and notes.
- **Task Manager** Plan your days with a task manager including subtasks
- **Next.js Frontend:** Responsive UI for interactive planning.
- **Spring Boot Backend:** RESTful API for data persistence in PostgreSQL.
- **Dockerized:** Easy environment setup and deployment (and Test Containers!).
- **Planned:** User authentication, personalised dashboards, more mental health planning tools.

---

## ️ Technologies

* **Frontend:** Next.js, JavaScript/TypeScript, (Your UI library if any, e.g., React)
* **Backend:** Spring Boot, Java 17+, Spring Data JPA, Lombok, PostgreSQL
* **Containerization:** Docker, Docker Compose

---

## Stopping the Application

To stop the services:

1. Frontend: Go to the terminal running pnpm run dev and press Ctrl+C.
2. Backend: Go to the terminal running gradlew bootRun and press Ctrl+C.
3. Database: Go to the terminal where you ran docker-compose up -d postgres and run:

```bash
docker-compose down # This will stop all services defined in docker-compose
```