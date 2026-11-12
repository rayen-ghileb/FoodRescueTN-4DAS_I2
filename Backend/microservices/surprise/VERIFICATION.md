# Execution verification

Implementation checks were executed on 11 November 2026 (Africa/Lagos). The Hello request was also sent successfully from the Postman desktop application on 12 November 2026.

## Verified results

| Check | Observed result |
| --- | --- |
| Java / Spring Boot | Java 17.0.8 / repository's Spring Boot 4.1.2 |
| `mvn -B -ntp clean verify` | BUILD SUCCESS; executable JAR packaged |
| Automated tests | 6 passed: 1 MVC test and 5 PostgreSQL integration tests; 0 failures, errors or skipped tests |
| PostgreSQL | Independent native PostgreSQL 18.4 instance on 127.0.0.1:5546 |
| Application database | `surprise_basket_db` |
| Business tables | `surprise_baskets`, `basket_orders`, `pickup_codes`, `basket_descriptions`, `payment_simulations` |
| Flyway | V1 and V2 applied successfully; V1 was not edited after application |
| Hibernate | Schema validation passed |
| Relationships | Basket/orders, optional pickup codes, multiple descriptions and optional payment simulation persisted and reloaded |
| Unique order relationships | PostgreSQL rejected a second pickup code or payment simulation for the same order |
| Foreign keys | Only service-owned basket/order relationships; no cross-service database foreign keys |
| Standalone application | Executable JAR started on port 8086 without startup errors |
| Live Hello | HTTP 200, text/plain, exact 24-byte body `hello im surprise busket`, without an added newline |
| Swagger UI | HTML, JavaScript, CSS, OpenAPI JSON and Swagger configuration returned HTTP 200 |
| Postman | GET /api/surprise-baskets/hello returned 200 OK and the exact body on 12 November 2026 |
| `docker compose config --quiet` | Passed |
| Existing Maven wrapper | Launched Maven 3.10.0 successfully |
| Change scope | Original 77 files outside Surprise matched their SHA-256 baseline before upstream synchronization |
| `git diff --check` | Passed |

The negative uniqueness tests intentionally produce SQLState 23505 warnings. Flyway creates its required `flyway_schema_history` metadata table in addition to the five business tables.

## Runtime limitations

Docker Desktop's engine was unavailable during execution. Compose configuration was validated, but no database container was launched. Runtime and persistence verification used installed PostgreSQL tools with separate application and integration-test databases, SCRAM password authentication, and a loopback-only listener on port 5546. The existing PostgreSQL instance on port 5432 was not changed.

Start Docker Desktop before following the README's Compose commands. If a native PostgreSQL verification instance is still using port 5546, stop that instance first or select another host port and update the JDBC URLs accordingly. Runtime process identifiers are machine-specific and are intentionally not stored here.

Integration tests use `surprise_basket_test_db`, a random HTTP port and transaction rollback. The Hello MVC test needs no database.

## Implementation scope

The original entry point, Maven wrapper, package names, Java version and Spring Boot version are preserved. All implementation changes belong to `Backend/microservices/surprise`.

- Added the Hello controller and replaced the empty TestController placeholder.
- Added five JPA entities and five Spring Data repositories.
- Added Flyway V1 for baskets, orders and pickup codes, and V2 for descriptions and payment simulations.
- Configured PostgreSQL environment variables, an isolated Compose project, Swagger UI, automated tests and service documentation.
- Added `.env.example` with public development defaults and ignored local credential files.

PaymentSimulation stores mock-payment data only. There is no payment execution, business order workflow, new CRUD API, RabbitMQ consumer or shared-infrastructure implementation. Swagger exposes the Hello endpoint.

The existing `SurpriseBasket` represents the diagram's `BasketListing`. Other naming/field differences and the deferred rule requiring a description before publication are documented in README.
