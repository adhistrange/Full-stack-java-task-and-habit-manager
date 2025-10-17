# Task Habit Manager

This repository contains a full-stack Java application that manages tasks and habits.

Project layout:
- backend/ — Spring Boot 3.3.4 backend with Spring Data JPA and SQLite
- desktop/ — JavaFX 17 desktop client (UI) that calls the backend REST APIs and can persist to a local SQLite DB

Requirements
- Java 17+
- Maven
- (For JavaFX desktop): ensure JavaFX runtime is available (the javafx-maven-plugin and dependencies are included)

Backend
- Uses Spring Boot 3.3.4
- Entities: `Task` (id, title, description, dueDate, completed) and `Habit` (id, name, dayOfWeek)
- SQLite is used as the persistent store (file: `taskhabit.db`, created in backend working directory)
- REST endpoints:
  - Tasks: GET /api/tasks, GET /api/tasks/{id}, POST /api/tasks, PUT /api/tasks/{id}, DELETE /api/tasks/{id}, PUT /api/tasks/{id}/complete?completed=true|false
  - Habits: GET /api/habits, GET /api/habits/{id}, POST /api/habits, PUT /api/habits/{id}, DELETE /api/habits/{id}

Desktop (JavaFX)
- UI located in `desktop/src/main/resources/main.fxml`
- Main app in `desktop/src/main/java/com/example/taskhabitmanager/Main.java`
- The desktop app:
  - Displays tasks and habits in tables
  - Can add/delete tasks and habits, mark tasks complete, and refresh lists
  - Calls backend at `http://localhost:8080` by default
  - Also initializes a local SQLite DB (`desktop_taskhabit.db`) for local persistence (see DatabaseHelper.java)

How to run

1. Start the backend
   - cd backend
   - mvn spring-boot:run
   - This will start the backend at `http://localhost:8080` and create `taskhabit.db` in the backend directory.

2. Start the desktop app
   - cd desktop
   - mvn javafx:run
   - The desktop application UI will open. Use the buttons to refresh, add, delete, and toggle completion.

Notes and tips
- The backend and desktop each create separate SQLite files:
  - backend: `taskhabit.db`
  - desktop: `desktop_taskhabit.db`
- The desktop app uses HTTP to call the backend. Make sure the backend is running when you attempt to refresh/load content.
- The provided code is a scaffold — you can extend validation, error handling, UI polish, or add authentication as needed.

License: MIT (sample project)
