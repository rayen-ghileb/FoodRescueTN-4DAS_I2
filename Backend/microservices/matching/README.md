# FoodRescue TN — Matching & Pickup Microservice

Spring Boot 4.1.2 · Java 17 · Spring Data JPA · PostgreSQL · Port **8082**

This module is integrated into the team repository at `Backend/microservices/matching`.
The entry point is `foodrescuetn4das_i2.matching.MatchingApplication`.

## Before running

1. Start your existing local PostgreSQL server and create the database `matching_db`. Alternatively, use the optional Docker Compose setup below.
2. Optionally enable PostGIS in `matching_db` using `CREATE EXTENSION IF NOT EXISTS postgis;` if your local PostgreSQL installation provides PostGIS. This first version does **not** yet store geometry or perform distance-based matching.
3. Set environment variable `DB_PASSWORD` to your PostgreSQL password (IntelliJ > Run > Edit Configurations > Environment variables). Never commit a real password.
4. If needed, override `DB_URL` and `DB_USER`; default values are `jdbc:postgresql://localhost:5432/matching_db` and `postgres`.
5. Run `MatchingApplication.java` in IntelliJ or run `./mvnw spring-boot:run` (`.\mvnw.cmd spring-boot:run` on Windows).

### Optional Docker/PostGIS database (instead of local pgAdmin PostgreSQL)

Set `DB_PASSWORD` in your PowerShell session, and start this module's Docker Compose service:

```powershell
$env:DB_PASSWORD = "your-postgresql-password"
docker compose up -d
```

This container listens on **localhost:5433** to avoid clashing with an existing PostgreSQL server on port 5432. In the same terminal or IntelliJ Run configuration, set:

`DB_URL=jdbc:postgresql://localhost:5433/matching_db`

The Docker image includes PostGIS and initializes the database for you. Do not change a running production database's PostGIS major version by simply changing the image tag.

To test compilation without starting PostgreSQL, use `./mvnw clean test`; the test suite uses an in-memory H2 database.

## REST endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/matching/hello` | Health smoke test: returns `hello im matching and pickup service` |
| POST | `/api/matches` | Create a donation/association match |
| GET | `/api/matches/{id}` | Read a match |
| POST | `/api/pickups` | Schedule one pickup for a match |
| GET | `/api/pickups/{id}` | Read a pickup |
| POST | `/api/pickups/{id}/assign` | Assign a driver |
| PATCH | `/api/pickups/{id}/status` | Update the status |

All endpoint addresses are relative to `http://localhost:8082`.

### 1. Create match

`POST /api/matches`

```json
{
  "offerId": "550e8400-e29b-41d4-a716-446655440000",
  "associationId": "550e8400-e29b-41d4-a716-446655440001",
  "quantityKg": 15
}
```

Returns HTTP 201. Copy `id` from the response; it is the match UUID.

### 2. Schedule pickup

`POST /api/pickups`

```json
{
  "matchId": "<match-uuid-from-the-previous-response>",
  "scheduledAt": "2026-11-13T16:00:00"
}
```

Returns HTTP 201. Copy the pickup `id` from this response.

### 3. Assign driver

`POST /api/pickups/{pickupId}/assign`

```json
{
  "driverId": "550e8400-e29b-41d4-a716-446655440002"
}
```

Returns HTTP 201.

### 4. Update pickup status

`PATCH /api/pickups/{pickupId}/status`

```json
{ "status": "DELIVERED" }
```

Allowed values: `SCHEDULED`, `PICKED_UP`, `DELIVERED`, `CANCELLED`.

## Database tables

With `spring.jpa.hibernate.ddl-auto=update` (development only), Hibernate creates:

- `donation_matches` (`offerId`, `associationId`, quantity, status, matched time)
- `pickups` (one pickup per match, scheduled time, delivery status)
- `driver_assignments` (driver ID, pickup ID, assignment status)

`offerId`, `associationId`, and `driverId` are **identifiers owned by other microservices**, not foreign keys in this database. In this version, they are not yet validated through REST, events, or the API Gateway.

## Current scope and next steps

This implements the initial CRUD-style workflow from our Matching & Pickup plan. For full team integration, add validation against the Donation, Partner, and Volunteer services; real geospatial matching with PostGIS; authentication; event publishing; and robust concurrent assignment rules. Do not consider this production ready.
