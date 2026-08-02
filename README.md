# Employee API

A production-ready **Spring Boot 3** REST API demonstrating Java development, CI/CD, testing, and AWS deployment.

## What it does

- **Employee CRUD** — create, read, update and delete employee records (H2 in-memory database)
- **JWT Authentication** — login endpoint issues a signed token; all employee endpoints require it
- **Full test suite** — unit tests (Mockito), integration tests (DataJpaTest), API tests (MockMvc)
- **HTML test report** — Maven Surefire report generated in CI and published as a GitHub Actions artifact
- **Deployed to AWS EC2** — Ubuntu 22.04, Java 17 JRE, nginx reverse proxy, systemd service

## Architecture

```
Client → nginx :80 → Spring Boot :8080 → H2 in-memory DB
                         ↑
              GitHub Actions CI/CD (provision → configure → verify)
```

See `.udap/architecture.d2` for the source-of-truth architecture diagram.

## API Endpoints

| Method | Path                      | Auth     | Description                  |
|--------|---------------------------|----------|------------------------------|
| POST   | `/api/auth/login`         | Public   | Login, returns JWT token     |
| GET    | `/api/employees`          | Required | List all employees           |
| GET    | `/api/employees/{id}`     | Required | Get employee by ID           |
| POST   | `/api/employees`          | Required | Create a new employee        |
| PUT    | `/api/employees/{id}`     | Required | Update employee by ID        |
| DELETE | `/api/employees/{id}`     | Required | Delete employee by ID        |
| GET    | `/health`                 | Public   | Health check                 |
| GET    | `/actuator/health`        | Public   | Spring Actuator health       |

## Run locally

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone and run
git clone https://github.com/<your-org>/employee-api.git
cd employee-api
./mvnw spring-boot:run
# API available at http://localhost:8080
```

**With Docker:**
```bash
docker build -t employee-api .
docker run -p 8080:8080 employee-api
```

## Quick start (curl)

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 2. Create an employee
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","department":"Engineering","salary":90000}'

# 3. List employees
curl http://localhost:8080/api/employees \
  -H "Authorization: Bearer $TOKEN"
```

Default credentials: `admin` / `admin123`

## Running tests

```bash
# Unit + integration + API tests
./mvnw test

# All tests + generate Surefire HTML report → target/site/surefire-report.html
./mvnw verify -Ptest-report
```

Test classes:
- `unit/EmployeeServiceTest.java` — 8 unit tests (Mockito)
- `unit/JwtUtilTest.java` — 6 JWT utility unit tests
- `api/EmployeeControllerApiTest.java` — 9 MockMvc API tests
- `api/AuthControllerApiTest.java` — 5 auth endpoint API tests
- `integration/EmployeeRepositoryIT.java` — 6 JPA integration tests

## Pipeline stages

| Stage       | What it does                                             |
|-------------|----------------------------------------------------------|
| `test`      | Runs all tests, generates Surefire HTML report artifact  |
| `build`     | Builds the JAR, uploads as CI artifact                   |
| `provision` | Terraform: EC2 + EIP + Security Group                    |
| `configure` | Ansible: installs Java, deploys JAR, configures nginx    |
| `verify`    | Health-checks `GET /health` with retry + backoff         |

An auxiliary `post-deploy-report` workflow (manual dispatch) SSHes into the server to fetch the HTML report.

## Configuration

| Variable       | Default (dev)                           | Description                     |
|----------------|-----------------------------------------|---------------------------------|
| `JWT_SECRET`   | `dev-only-insecure-secret-change...`    | JWT signing key (32+ chars)     |
| `SERVER_PORT`  | `8080`                                  | Internal application port       |

Override via environment variable. In production, `JWT_SECRET` is supplied via the `.env` file written by Ansible from the `JWT_SECRET` CI secret.

## Operations

```bash
# SSH into the server (IP available after first deploy)
ssh ubuntu@<EC2_IP>

# Check service status
sudo systemctl status employee-api

# View application logs
sudo journalctl -u employee-api -f

# Restart the service
sudo systemctl restart employee-api

# Reload nginx
sudo systemctl reload nginx

# Destroy infrastructure (via GitHub Actions → Destroy workflow)
```

## Infrastructure

- **Cloud:** AWS `us-east-2`
- **Instance:** EC2 `t3.small`, Ubuntu 22.04 LTS
- **Networking:** Default VPC, Elastic IP, Security Group (22/80/443)
- **Database:** H2 in-memory (data resets on restart)
- **IaC:** Terraform `infra/` with platform-managed S3 state
- **Config:** Ansible `ansible/playbook.yml`
- **Cost:** ~$20–28/month (on-demand t3.small + EIP)
