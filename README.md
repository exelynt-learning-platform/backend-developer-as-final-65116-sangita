# Resource Booking API

Backend assignment for a resource booking system (rooms, vehicles, equipment).

Stack: Spring Boot 3.5, Java 17, Spring Security + JWT, JPA, MySQL.

## Run locally

You need JDK 17+ and MySQL.

1. Create a database called `resource_booking` (or leave it, the JDBC URL can create it).
2. If your MySQL user/password is not `root` / `root`, change it in `application.yml` or set env vars.
3. Start with the `dev` profile (local JWT key + seed users):

```
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

`JWT_SECRET` is required outside `dev` / `h2` / test. There is no default key. Treat the value as a raw UTF-8 string unless you prefix it with `base64:` (or `raw:`). For default or `prod`, set a secret of at least 32 characters first:

```
set JWT_SECRET=put-your-own-long-random-secret-here-32+
mvnw.cmd spring-boot:run
```

No MySQL? Use H2 instead:

```
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

Docker (optional):

```
docker compose up -d mysql
```

Production: use `--spring.profiles.active=prod` and set `JWT_SECRET`. Seed users only exist on `dev`/`h2` (`@Profile`) and stay off in prod. CORS origins are an explicit list (`app.cors.allowed-origins`); wildcards are rejected.

## Env vars

| Name | Default |
| --- | --- |
| SERVER_PORT | 8080 |
| DB_URL | jdbc:mysql://localhost:3306/resource_booking?... |
| DB_USERNAME | root |
| DB_PASSWORD | root |
| JWT_SECRET | **required** (no default; min 32 chars; optional `base64:` / `raw:` prefix) |
| JWT_EXPIRATION_MS | 86400000 |
| APP_SEED_ENABLED | false (true in `dev` / `h2` only) |
| SEED_ADMIN_PASSWORD / SEED_USER_PASSWORD | local demo defaults in `dev` / `h2` |
| APP_CORS_ORIGIN | prod fallback `http://localhost:8080` |

See `.env.example`.

## Test users (opt-in)

Created only on the `dev` and `h2` profiles when `app.seed.enabled=true`. Staging/prod cannot seed these users. Passwords can be overridden with `SEED_ADMIN_PASSWORD` and `SEED_USER_PASSWORD`. Documented demo passwords are refused outside `dev`/`h2`.

- admin / Admin@123  (ADMIN)
- user / User@123    (USER)
- user2 / User@123   (USER)

## Docs

- Swagger: http://localhost:8080/swagger-ui.html
- Postman collection: `docs/Resource-Booking.postman_collection.json`

Login with `POST /auth/login`, then send `Authorization: Bearer <token>`.

## Roles

USER:
- can view resources
- can create reservations (owner is taken from the JWT, not the body)
- can view / cancel only their own reservations

ADMIN:
- full CRUD on resources and reservations
- can see everyone's reservations

## Main APIs

Resources: `/api/resources`  (GET for both, POST/PUT/DELETE admin only)

Reservations: `/api/reservations`

Filters on list:

```
GET /api/reservations?status=PENDING&minPrice=50&maxPrice=200&page=0&size=10&sort=price,asc
```

Statuses: PENDING, CONFIRMED, CANCELLED

If you don't send `price` on create, it is calculated from hourly rate * hours.

## Tests

```
mvnw.cmd test
```

Uses H2, no MySQL needed.
