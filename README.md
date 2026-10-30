# Calculator DevOps

A Calculator REST API in **Spring Boot 4 / Java 21**, delivered through a fully automated CI/CD pipeline:
**GitHub → Jenkins → Docker Hub → Docker Compose** (Nginx reverse proxy in front of 2 API replicas).

CSE 816 Software Production Engineering — Mini-Project.

## API

| Operation | Endpoint | Example response |
| --- | --- | --- |
| Add | `GET /api/v1/add?a=2&b=3` | `{"operation":"add","a":2.0,"b":3.0,"result":5.0}` |
| Subtract | `GET /api/v1/subtract?a=10&b=4` | `{"operation":"subtract",...,"result":6.0}` |
| Multiply | `GET /api/v1/multiply?a=6&b=7` | `{"operation":"multiply",...,"result":42.0}` |
| Divide | `GET /api/v1/divide?a=9&b=3` | `{"operation":"divide",...,"result":3.0}` |
| Health | `GET /actuator/health` | `{"status":"UP",...}` |

Invalid input returns **HTTP 400** with a structured body:

```json
{"timestamp":"...","status":400,"error":"Bad Request","message":"Division by zero is not allowed","path":"/api/v1/divide"}
```

Covered cases: divide by zero, missing `a`/`b`, non-numeric values, `NaN`/`Infinity`.

## Build, test, run (needs JDK 21)

```bash
./mvnw clean package          # build the executable JAR (runs tests)
./mvnw test                   # JUnit 5 + MockMvc tests; XML reports in target/surefire-reports/
java -jar target/calculator-0.0.1-SNAPSHOT.jar --server.port=9090
curl "http://localhost:9090/api/v1/add?a=2&b=3"
```

## Docker

```bash
docker build -t calculator .
docker run -p 9090:8080 calculator
curl "http://localhost:9090/api/v1/divide?a=1&b=0"
```

Multi-stage build: `maven:3.9-eclipse-temurin-21` compiles the JAR; the runtime image is `eclipse-temurin:21-jre-alpine`
running as a non-root user, with the dependency layer cached separately from the source.

Image on Docker Hub: `docker pull dawoodzargar/calculator:latest`

## Docker Compose

```bash
DOCKER_IMAGE=dawoodzargar/calculator docker compose up -d --wait
curl "http://localhost:8088/api/v1/multiply?a=6&b=7"
docker compose ps
```

- `app` — 2 replicas, health check on `/actuator/health`, `restart: unless-stopped`, not exposed to the host
- `nginx` — reverse proxy on host port **8088**, round-robins across replicas (see the `X-Upstream` response header),
  starts only after `app` is healthy
- both on the `calc-net` bridge network

## CI/CD pipeline (`Jenkinsfile`)

| Stage | What it does |
| --- | --- |
| Checkout | Pulls the commit that triggered the build |
| Build | `./mvnw clean package -DskipTests` |
| Test | `./mvnw test`, publishes JUnit results, fails the build on any test failure |
| Docker Build | Builds the image tagged with the Jenkins build number and `latest` |
| Push | Logs in with the `dockerhub-creds` Jenkins credential and pushes both tags |
| Deploy | `docker compose up -d --wait`, then polls `/actuator/health` through Nginx |

Triggered by a GitHub webhook (`/github-webhook/`, exposed with ngrok), with Poll SCM as a fallback.
Success/failure emails are sent with the Email Extension plugin. No credentials are stored in this repository.

## Project layout

```
src/main/java/com/spe/calculator/
  controller/   REST endpoints
  service/      arithmetic + input checks
  dto/          JSON response records
  exception/    custom exception + global 400 handler
src/test/java/...  unit tests (service) and MockMvc tests (controller, actuator)
Dockerfile, .dockerignore, docker-compose.yml, nginx/nginx.conf, Jenkinsfile
```
