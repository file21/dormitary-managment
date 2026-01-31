# Dormitory Management System

Simple JavaFX desktop app that models dormitory applications, approvals, building assignment, and resident tracking. It uses an in-memory data store so the focus stays on workflow and OOP structure rather than database setup.

## Run the App

```bash
mvn clean javafx:run
```

## Login Credentials

- Admin: `admin` / `admin123`
- Proctor: `proctor1` / `pass123`
- Owner: `owner` / `owner123`
- Student: `student1` / `pass123`

## Project Structure

- `model/` - Data classes (User, Student, DormApplication, etc.)
- `service/` - In-memory repository and business logic
- `ui/` - JavaFX views and dashboards
