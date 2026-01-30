# Dormitory Management System Design (Detailed)

## 1) Purpose and scope
This document expands the design plan with **detailed, code-linked responsibilities** so each team member can jump directly to the correct files and implement features without guessing. It also explains how the current code is wired, what is already available, and which parts are still placeholders. The goal is: **read → copy/paste/extend → run**.

## 2) Architecture overview (how the app is wired)
The system is a standard JavaFX + DAO + Service stack:

- **UI (FXML + Controllers)** → **Service layer** → **DAO layer** → **MySQL DB**.
- **Entry point**: `App.java` creates a `SceneRouter` and opens `login.fxml`.【F:src/main/java/edu/aau/dorm/App.java†L1-L21】
- **Scene navigation**: `SceneRouter` loads FXML files from `/edu/aau/dorm/ui/` with titles.【F:src/main/java/edu/aau/dorm/ui/SceneRouter.java†L1-L32】
- **Auth flow**: `LoginController` calls `AuthService.login()`; status text is currently hard-coded (routing still missing).【F:src/main/java/edu/aau/dorm/ui/controller/LoginController.java†L1-L31】
- **Service layer** already contains core business methods (approve/reject, check-in/withdraw).【F:src/main/java/edu/aau/dorm/service/ApplicationService.java†L1-L52】【F:src/main/java/edu/aau/dorm/service/ProctorService.java†L1-L61】
- **DAO layer** provides MySQL persistence. Even though some classes are named `*Pg`, they use the shared `Db.dataSource()` which is configured for MySQL/HikariCP.【F:src/main/java/edu/aau/dorm/dao/UserDaoPg.java†L1-L58】【F:src/main/java/edu/aau/dorm/util/Db.java†L1-L35】

**Key takeaway:** All pages are defined in FXML under `src/main/resources/edu/aau/dorm/ui/`. Controllers live under `src/main/java/edu/aau/dorm/ui/controller/`. Services/DAOs are ready to be called from those controllers.

## 3) Data model highlights (what is stored)
The MySQL schema is already detailed and should be your source of truth.

- **Users & roles**: `app_user` holds all logins and roles (OWNER/ADMIN/PROCTOR/STUDENT).【F:src/main/resources/sql/schema_mysql.sql†L17-L41】
- **Student profile**: `student_profile` stores student-specific info (full name, AAU ID, department, year, category, gender).【F:src/main/resources/sql/schema_mysql.sql†L43-L66】
- **Dorm applications**: `dorm_application` tracks status, sponsorship, campus preference, distance, score, etc.【F:src/main/resources/sql/schema_mysql.sql†L141-L175】
- **Allocations**: `allocation` links approved applications to beds/rooms and check-in timestamps.【F:src/main/resources/sql/schema_mysql.sql†L203-L230】
- **Notifications**: `notification` stores messages for users (read/unread).【F:src/main/resources/sql/schema_mysql.sql†L236-L253】

## 4) Functional areas (detailed and mapped to code)
### 4.1 Authentication & role routing
**What exists now**
- `LoginController` calls `AuthService.login()` and displays a message (no routing yet).【F:src/main/java/edu/aau/dorm/ui/controller/LoginController.java†L1-L31】
- `AuthService` validates active users and passwords via `UserDaoPg` + `PasswordHasher`.【F:src/main/java/edu/aau/dorm/service/AuthService.java†L1-L27】

**What needs to be added**
- Route by user role after login (Owner/Admin/Proctor/Student).
- Add a session or global state to track the logged-in user.
- Add a link to the signup screen from login (optional).

**Files to edit**
- `LoginController.java`, `SceneRouter.java`, `login.fxml`, and possibly a new `Session` helper.

### 4.2 Student application (submit/track)
**What exists now**
- `ApplicationService` handles approve/reject and writes notifications (for admin/proctor usage).【F:src/main/java/edu/aau/dorm/service/ApplicationService.java†L1-L52】
- `ApplicationDaoPg` implements `getById`, `markSubmitted`, and `setStatus`.【F:src/main/java/edu/aau/dorm/dao/ApplicationDaoPg.java†L1-L90】

**What needs to be added**
- **Form submission**: read fields, validate, compute a score, write to `dorm_application`, and call `markSubmitted`.
- **Student dashboard**: show current status, score, and latest notifications.
- **Validation rules**: require AAU ID format using `Validation.requireValidAauId()`.【F:src/main/java/edu/aau/dorm/util/Validation.java†L1-L35】

**Files to edit**
- `application_form.fxml`, `student_dashboard.fxml`
- new `StudentDashboardController` or expand existing controller(s).

### 4.3 Admin review (approve/reject)
**What exists now**
- `ApplicationService.approve()` and `ApplicationService.reject()` (with notifications).【F:src/main/java/edu/aau/dorm/service/ApplicationService.java†L1-L52】

**What needs to be added**
- Admin UI to view pending applications and trigger approve/reject.
- Add a reason field for rejection (stored via notification text or a future review table).

**Files to edit**
- `admin_dashboard.fxml`
- new `AdminDashboardController` wired to `ApplicationService`.

### 4.4 Proctor check-in (allocation/withdraw)
**What exists now**
- `ProctorService.checkIn()` writes an allocation and marks status CHECKED_IN with notification.【F:src/main/java/edu/aau/dorm/service/ProctorService.java†L1-L43】
- `ProctorService.withdraw()` frees beds and marks WITHDREW (with notification).【F:src/main/java/edu/aau/dorm/service/ProctorService.java†L1-L61】
- `AllocationDaoPg` does DB inserts/deletes for allocations.【F:src/main/java/edu/aau/dorm/dao/AllocationDaoPg.java†L1-L38】

**What needs to be added**
- Proctor UI: search accepted applications, assign bed, room number.
- Withdraw/checkout action.
- (Optional) query assigned block using `BlockDaoPg.getAssignedBlockIdForProctor()` to filter bed list.【F:src/main/java/edu/aau/dorm/dao/BlockDaoPg.java†L1-L33】

**Files to edit**
- `proctor_checkin.fxml` and a `ProctorCheckinController`.

### 4.5 Notifications
**What exists now**
- `SimpleNotificationService` prints to console (demo only).【F:src/main/java/edu/aau/dorm/service/SimpleNotificationService.java†L1-L16】
- `NotificationDaoPg` can insert notifications into the DB.【F:src/main/java/edu/aau/dorm/dao/NotificationDaoPg.java†L1-L28】

**What needs to be added**
- Replace `SimpleNotificationService` with a DB-backed implementation (use `NotificationDaoPg`).
- Build UI widgets on dashboards for unread notifications.

### 4.6 Owner / user management
**What exists now**
- Basic `UserDaoPg.findByUsername()` only (read).【F:src/main/java/edu/aau/dorm/dao/UserDaoPg.java†L1-L58】

**What needs to be added**
- CRUD for users (create, deactivate, reset passwords, set roles).
- Owner UI page to manage user accounts.

## 5) Team assignments (clear ownership)
Below is **exactly** who owns which parts. Each person should open the listed files, copy and edit code there, and complete the features.

### ✅ Natnael — Authentication + Routing + App navigation
**Responsibilities**
- Wire login to route to correct dashboard based on role.
- Create a simple `Session` or static user holder.
- Add a “logout” action to return to `login.fxml`.

**Files to read/edit**
- `src/main/java/edu/aau/dorm/App.java` (startup flow).【F:src/main/java/edu/aau/dorm/App.java†L1-L21】
- `src/main/java/edu/aau/dorm/ui/SceneRouter.java` (navigation).【F:src/main/java/edu/aau/dorm/ui/SceneRouter.java†L1-L32】
- `src/main/java/edu/aau/dorm/ui/controller/LoginController.java` (use AuthService + route).【F:src/main/java/edu/aau/dorm/ui/controller/LoginController.java†L1-L31】
- `src/main/resources/edu/aau/dorm/ui/login.fxml` (add signup link, basic layout).【F:src/main/resources/edu/aau/dorm/ui/login.fxml†L1-L15】

**Functionality details**
- On login success, switch: OWNER → `owner_users.fxml`, ADMIN → `admin_dashboard.fxml`, PROCTOR → `proctor_checkin.fxml`, STUDENT → `student_dashboard.fxml`.
- Handle `IllegalArgumentException` and show in `statusLabel`.

---

### ✅ Nahom — Student Application + Student Dashboard
**Responsibilities**
- Build the application form (fields, validation, submit).
- Show student status and notifications on dashboard.

**Files to read/edit**
- `src/main/resources/edu/aau/dorm/ui/application_form.fxml` (form UI).【F:src/main/resources/edu/aau/dorm/ui/application_form.fxml†L1-L9】
- `src/main/resources/edu/aau/dorm/ui/student_dashboard.fxml` (dashboard).【F:src/main/resources/edu/aau/dorm/ui/student_dashboard.fxml†L1-L9】
- `src/main/java/edu/aau/dorm/dao/ApplicationDaoPg.java` (submit & status).【F:src/main/java/edu/aau/dorm/dao/ApplicationDaoPg.java†L1-L90】
- `src/main/java/edu/aau/dorm/util/Validation.java` (AAU ID validation).【F:src/main/java/edu/aau/dorm/util/Validation.java†L1-L35】

**Functionality details**
- Inputs: window code, sponsorship, disability, department, campus preference, distance, notes.
- Validate AAU ID format and required fields; compute score (e.g., disability + distance + category).
- Save to `dorm_application`, then call `ApplicationDaoPg.markSubmitted()`.
- Show status + score on dashboard + latest notifications.

---

### ✅ Kidus — Proctor Check-in + Withdraw
**Responsibilities**
- Build proctor UI to check students in and withdraw them.
- Use `ProctorService` + `AllocationDaoPg` to update DB.

**Files to read/edit**
- `src/main/resources/edu/aau/dorm/ui/proctor_checkin.fxml` (check-in UI).【F:src/main/resources/edu/aau/dorm/ui/proctor_checkin.fxml†L1-L9】
- `src/main/java/edu/aau/dorm/service/ProctorService.java` (business logic).【F:src/main/java/edu/aau/dorm/service/ProctorService.java†L1-L61】
- `src/main/java/edu/aau/dorm/dao/AllocationDaoPg.java` (bed allocation).【F:src/main/java/edu/aau/dorm/dao/AllocationDaoPg.java†L1-L38】
- (Optional) `src/main/java/edu/aau/dorm/dao/BlockDaoPg.java` (assigned block).【F:src/main/java/edu/aau/dorm/dao/BlockDaoPg.java†L1-L33】

**Functionality details**
- Search accepted applications by application ID or student name (extend DAO as needed).
- Provide bed selection list for the assigned block.
- Check-in should insert allocation and mark application CHECKED_IN.
- Withdraw should remove allocation and mark WITHDREW with a note.

---

### ✅ Mranatha — UI/UX (all screens styling + layout)
**Responsibilities**
- Build clean layouts, forms, and tables for each role.
- Ensure consistent spacing, fonts, and button styles.

**Files to read/edit**
- `src/main/resources/edu/aau/dorm/ui/login.fxml` (login layout).【F:src/main/resources/edu/aau/dorm/ui/login.fxml†L1-L15】
- `src/main/resources/edu/aau/dorm/ui/signup.fxml` (signup layout).【F:src/main/resources/edu/aau/dorm/ui/signup.fxml†L1-L10】
- `src/main/resources/edu/aau/dorm/ui/admin_dashboard.fxml` (admin UI).【F:src/main/resources/edu/aau/dorm/ui/admin_dashboard.fxml†L1-L9】
- `src/main/resources/edu/aau/dorm/ui/student_dashboard.fxml` (student UI).【F:src/main/resources/edu/aau/dorm/ui/student_dashboard.fxml†L1-L9】
- `src/main/resources/edu/aau/dorm/ui/proctor_checkin.fxml` (proctor UI).【F:src/main/resources/edu/aau/dorm/ui/proctor_checkin.fxml†L1-L9】
- `src/main/resources/edu/aau/dorm/ui/owner_users.fxml` (owner UI).【F:src/main/resources/edu/aau/dorm/ui/owner_users.fxml†L1-L9】
- `src/main/resources/edu/aau/dorm/ui/application_form.fxml` (form UI).【F:src/main/resources/edu/aau/dorm/ui/application_form.fxml†L1-L9】

**Functionality details**
- Provide reusable panes: navigation sidebar, summary cards, tables.
- Use JavaFX `TableView` for lists (applications/users/allocations).
- Ensure required input fields have validation labels.

---

### ✅ Kiya — Owner/Admin User Management
**Responsibilities**
- Build UI for creating users, toggling active status, resetting passwords.
- Expand DAO layer for user CRUD (create/update/deactivate).

**Files to read/edit**
- `src/main/resources/edu/aau/dorm/ui/owner_users.fxml` (user management UI).【F:src/main/resources/edu/aau/dorm/ui/owner_users.fxml†L1-L9】
- `src/main/java/edu/aau/dorm/dao/UserDaoPg.java` (add insert/update methods).【F:src/main/java/edu/aau/dorm/dao/UserDaoPg.java†L1-L58】
- `src/main/resources/sql/schema_mysql.sql` (user role fields).【F:src/main/resources/sql/schema_mysql.sql†L17-L41】

**Functionality details**
- Create new users with role + password (hash before insert).
- Deactivate/reactivate accounts (update `active`).
- Reset passwords with bcrypt (reuse `PasswordHasher`).

---

### ✅ Lameik — Notifications + Persistence
**Responsibilities**
- Replace console notifications with DB notifications.
- Add UI lists for unread and recent notifications in dashboards.

**Files to read/edit**
- `src/main/java/edu/aau/dorm/service/NotificationService.java` (interface).【F:src/main/java/edu/aau/dorm/service/NotificationService.java†L1-L5】
- `src/main/java/edu/aau/dorm/service/SimpleNotificationService.java` (replace with DB-backed).【F:src/main/java/edu/aau/dorm/service/SimpleNotificationService.java†L1-L16】
- `src/main/java/edu/aau/dorm/dao/NotificationDaoPg.java` (insert notifications).【F:src/main/java/edu/aau/dorm/dao/NotificationDaoPg.java†L1-L28】
- `src/main/resources/sql/schema_mysql.sql` (notification table).【F:src/main/resources/sql/schema_mysql.sql†L236-L253】

**Functionality details**
- On `notifyUser()`, insert into DB instead of printing.
- Add DAO methods to list unread notifications and mark as read.

## 6) Implementation order (suggested)
1. **Natnael**: login routing and session state.
2. **Mranatha**: UI structure to make wiring easy.
3. **Nahom**: student application + dashboard.
4. **Kidus**: proctor check-in/withdraw flows.
5. **Kiya**: user management.
6. **Lameik**: notifications persistence + UI.

## 7) Notes & gotchas
- Class names say `*Pg` but DB configuration in `Db` is for MySQL. Keep queries compatible with MySQL syntax (the schema file is MySQL).【F:src/main/java/edu/aau/dorm/util/Db.java†L1-L35】【F:src/main/resources/sql/schema_mysql.sql†L1-L11】
- Always validate AAU IDs using `Validation` helpers when creating students.【F:src/main/java/edu/aau/dorm/util/Validation.java†L1-L35】
- FXML files are placeholders—safe to replace with real layouts.
