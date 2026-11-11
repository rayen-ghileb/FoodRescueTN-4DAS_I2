# FoodRescue TN

FoodRescue TN is a distributed web platform designed to reduce food waste in Tunisia by connecting businesses with charities, volunteer drivers, and citizens. Donors publish edible surplus, the platform matches it with a compatible nearby association, volunteers coordinate collection, and the resulting impact is measured in food saved, meals provided, and CO2e avoided.

The platform also supports discounted **surprise baskets**, allowing citizens to reserve unsold food from participating businesses instead of letting it go to waste.

> **Project status:** The repository currently contains the initial Spring Boot microservice projects and technical design. The API contracts and infrastructure described below are the approved/proposed target design and should not be read as evidence that every endpoint has already been implemented.

## Core capabilities

- Donors publish, update, cancel, and track surplus food offers.
- Verified associations define their needs, accept matches, and confirm deliveries.
- Volunteer drivers declare availability and manage assigned pickups.
- Citizens browse discounted surprise baskets, place orders, and redeem pickup codes.
- Administrators verify partners, manage users, moderate issues, and monitor impact.
- Partner verification, food-safety checklists, ratings, status history, and notifications provide trust and traceability.
- Impact dashboards aggregate kilograms saved, estimated meals, CO2e avoided, governorate totals, reports, and planned demand forecasts.

## Actors

| Actor | Main responsibilities |
| --- | --- |
| Donor | Publish surplus offers, sell unsold stock as baskets, and view donation impact |
| Association / charity | Specify needs, accept compatible donations, monitor collection, complete safety checks, and rate partners |
| Volunteer driver | Declare availability, accept nearby pickups, and update collection and delivery status |
| Citizen | Browse, reserve, and collect discounted surprise baskets |
| Administrator | Verify partners, manage users, supervise reported issues, and view impact |

## Architecture

The target system uses one bounded context and one owned datastore per service. The Angular frontend communicates through the API Gateway; services never share databases.

```text
Angular frontend
        |
HTTPS + JWT
        v
Spring Cloud API Gateway
        |
        +--> Donation Offer -------- MongoDB
        +--> Matching & Pickup ------ PostgreSQL + PostGIS
        +--> Partner Registry ------- PostgreSQL
        +--> Impact Analytics -------- PostgreSQL + Redis cache
        +--> Volunteer Driver ------- PostgreSQL
        +--> Surprise Basket --------- MongoDB
        +--> User + Notification ---- PostgreSQL

Shared infrastructure: Eureka, Config Server, Keycloak, RabbitMQ, Docker Compose
```

### Services

| Service | Responsibility | Primary data |
| --- | --- | --- |
| **Donation Offer** | Surplus offers, food items, stock, photos, pickup locations, and reservations | MongoDB |
| **Matching & Pickup** | Geographic charity matching, pickup lifecycle, status history, and driver assignment | PostgreSQL + PostGIS |
| **Partner Registry & Trust** | Partner profiles, branches, verification, food-safety checks, and reviews | PostgreSQL |
| **Impact Analytics** | Event ledger, conversion factors, aggregates, reports, and demand forecasts | PostgreSQL + Redis |
| **Volunteer Driver** | Driver profiles, vehicles, availability, locations, and reviews | PostgreSQL |
| **Surprise Basket Marketplace** | Basket listings, orders, pickup codes, and simulated payment | MongoDB |
| **Common User + Notification** | Application profiles, preferences, notifications, delivery attempts, and device tokens | PostgreSQL; Keycloak owns credentials |

The current backend directories are under [`Backend/microservices`](Backend/microservices):

- [`donation`](Backend/microservices/donation)
- [`matching`](Backend/microservices/matching)
- [`partner`](Backend/microservices/partner)
- [`impact`](Backend/microservices/impact)
- [`volunteer`](Backend/microservices/volunteer)
- [`surprise`](Backend/microservices/surprise)

The shared User + Notification service, API Gateway, Eureka, Config Server, Keycloak realm, RabbitMQ topology, and Angular frontend are part of the target design and are not yet represented as complete applications in this repository.

## Communication model

FoodRescue TN uses two communication styles:

- **Synchronous REST/OpenFeign:** immediate validation and commands, including stock reservations, partner eligibility checks, and available-driver lookup.
- **Asynchronous RabbitMQ events:** facts that can be processed independently, including notifications and impact updates.

Important domain events include:

| Publisher | Event | Consumers |
| --- | --- | --- |
| Donation Offer | `OfferPublished` | Matching |
| Matching & Pickup | `MatchConfirmed` | Common User + Notification |
| Matching & Pickup | `DeliveryCompleted` | Impact Analytics |
| Surprise Basket | `BasketCollected` | Impact Analytics |

The Donation Offer service is the only owner of food stock and reservations. Other services reference external entities by identifier and must use the owning service API rather than accessing its database. Consumers deduplicate events by `eventId`; unused reservations are released after failed workflows.

## Main business flows

### Charity rescue

1. A donor publishes an offer with food type, quantity, location, and pickup window.
2. Matching finds a nearby compatible association and reserves the required stock.
3. An available volunteer driver is assigned to the pickup.
4. The driver records collection and delivery status.
5. `DeliveryCompleted` updates the impact ledger and sends relevant notifications.

### Surprise basket

1. A donor publishes a discounted basket from unsold stock.
2. A citizen browses available baskets and places an order.
3. The system returns a unique pickup code.
4. The citizen presents the code and the order is collected.
5. `BasketCollected` contributes to impact totals.

### Trust review

1. A donor or association creates a partner profile.
2. An administrator reviews the organization and supporting information.
3. The partner is approved or rejected.
4. Approved partners can participate in donation and pickup workflows.

## API overview

Requests are intended to enter through the API Gateway. The following endpoints are the documented contracts; they are currently proposed and not yet Swagger-verified.

| Service | Endpoints |
| --- | --- |
| Donation Offer | `POST /api/offers`, `GET /api/offers/{id}`, `POST /api/offers/{id}/reservations`, `POST /api/reservations/{id}/release` |
| Matching & Pickup | `POST /api/matches`, `POST /api/pickups/{id}/assign`, `PATCH /api/pickups/{id}/status`, `GET /api/pickups?driverId=` |
| Partner Registry | `POST /api/partners`, `GET /api/partners/{id}`, `PATCH /api/partners/{id}/verification`, `POST /api/partners/{id}/reviews` |
| Impact Analytics | `GET /api/impact/summary`, `GET /api/impact/governorates/{id}`, `GET /api/impact/forecast?month=`, `GET /api/impact/reports?period=` |
| Volunteer Driver | `POST /api/drivers`, `GET /api/drivers/{id}`, `PUT /api/drivers/{id}/availability`, `GET /api/drivers/available?lat=&lng=` |
| Surprise Basket | `GET /api/baskets?governorate=`, `POST /api/orders`, `GET /api/orders/{id}`, `POST /api/orders/{id}/collect` |
| User + Notification | `GET /api/users/me`, `PATCH /api/users/me`, `GET /api/notifications`, `PATCH /api/notifications/{id}/read` |

Expected error semantics include `401` for unauthenticated requests, `403` for forbidden operations, `404` for missing resources, `409` for stock or state conflicts, and `503` when a critical dependency is unavailable.

## Technology stack

- Java 17
- Spring Boot and Spring Cloud
- Spring MVC/WebFlux, Spring Data JPA/JDBC/R2DBC, REST/OpenFeign, and Springdoc OpenAPI
- Spring Modulith where applicable
- PostgreSQL, PostGIS, MongoDB, and Redis
- RabbitMQ for event-driven communication
- Keycloak with OAuth2/JWT and role-based access control
- Eureka service discovery and Spring Cloud Config
- Angular frontend
- Docker and Docker Compose
- Maven Wrapper for each backend service

## Running a backend service

Each service is an independent Maven/Spring Boot application. From a service directory:

```powershell
cd Backend\microservices\donation
.\mvnw.cmd spring-boot:run
```

Replace `donation` with `matching`, `partner`, `impact`, `volunteer`, or `surprise` as needed. To run the tests for an individual service:

```powershell
.\mvnw.cmd test
```

The `donation` and `matching` modules currently include a local `compose.yaml` for PostgreSQL and Redis development dependencies. Full multi-service startup through the planned shared Docker Compose environment is still to be completed.

## Project organization

```text
.
├── Backend/
│   └── microservices/
│       ├── donation/
│       ├── matching/
│       ├── partner/
│       ├── impact/
│       ├── volunteer/
│       └── surprise/
├── Documentation/
│   ├── Diagrams/
│   ├── Presentation/
│   ├── FoodRescue_TN.pdf
│   ├── FoodRescue_TN_API_Contracts_V2.pdf
│   ├── Overall-microservices-architecture.pdf
│   ├── Overall-Service-Dependencies.pdf
│   └── specification-outline.pdf
└── README.md
```

## Documentation

- [Technical design report](Documentation/FoodRescue_TN.pdf)
- [API contracts, version 2](Documentation/FoodRescue_TN_API_Contracts_V2.pdf)
- [Technical specification outline](Documentation/specification-outline.pdf)
- [Microservices architecture](Documentation/Overall-microservices-architecture.pdf)
- [Service dependencies](Documentation/Overall-Service-Dependencies.pdf)
- [Use-case diagram](Documentation/Diagrams/FoodRescue_TN_Use_Case_Diagram_Clean.pdf)
- [Seven service diagrams](Documentation/Diagrams/FoodRescue_TN_All_7_Readable_Diagrams_FIXED.pdf)
- [Project presentation](Documentation/Presentation/Presentation_MiniProject_AWD_2026_2027_EN.pptx)

## Development methodology

The team follows Scrum with a design/inception sprint followed by short development sprints. The planned increments cover foundations, security and synchronous workflows, events and notifications, trust and impact, then hardening and release. The service boundaries, API contracts, event contracts, and data ownership rules should be kept aligned as implementation progresses.
