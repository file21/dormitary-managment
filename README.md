# Dorm Application Management (JavaFX + PostgreSQL)

## Run
1. Configure environment variables:
- DB_URL=jdbc:postgresql://localhost:5432/dormdb
- DB_USER=postgres
- DB_PASS=postgres

2. Create DB schema:
- Run `src/main/resources/sql/schema.sql`

3. Run the app:
```bash
mvn javafx:run
```
