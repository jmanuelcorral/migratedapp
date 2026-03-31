# TaskManager - Migrated Application

Enterprise task management system migrated from .NET Framework 4.8 WCF/WebForms to Spring Boot and Angular.

## Architecture

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Angular   │─────▶│ Spring Boot │─────▶│  Oracle XE  │
│  Frontend   │      │   Backend   │      │  Database   │
│   (3000)    │      │   (8080)    │      │   (1521)    │
└─────────────┘      └─────────────┘      └─────────────┘
```

**Components:**
- **Frontend**: Angular 18 standalone components with TypeScript
- **Backend**: Spring Boot 3.3.x with Java 21, REST API
- **Database**: Oracle XE 21c with gvenzl/oracle-xe container
- **Infrastructure**: Docker Compose (local), Azure AKS + Terraform (production)

## Prerequisites

- **Docker Desktop** 4.25+ with Docker Compose
- **Java Development Kit (JDK)** 21
- **Node.js** 20.x LTS
- **Angular CLI** 18.x (`npm install -g @angular/cli`)
- **Maven** 3.9+ (or use included `mvnw` wrapper)
- **Azure CLI** 2.50+ (for Azure deployment)
- **Terraform** 1.5+ (for infrastructure as code)
- **kubectl** 1.28+ (for Kubernetes management)

## Quick Start (Docker Compose)

### 1. Clone and Navigate
```bash
cd D:\gitrepos\personal\migratedapp
```

### 2. Start All Services
```bash
docker-compose up -d
```

This starts:
- Oracle XE database on `localhost:1521`
- Spring Boot backend on `localhost:8080`
- Angular frontend on `localhost:3000`

### 3. Verify Services
```bash
# Check service status
docker-compose ps

# View backend logs
docker-compose logs -f backend

# View frontend logs
docker-compose logs -f frontend

# Check Oracle health
docker-compose logs oracle-db
```

### 4. Access Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health

### 5. Stop Services
```bash
docker-compose down
```

To remove volumes (database data):
```bash
docker-compose down -v
```

## Development Setup

### Backend (Spring Boot)

```bash
cd backend

# Install dependencies
./mvnw clean install

# Run tests
./mvnw test

# Run locally (requires Oracle running)
./mvnw spring-boot:run
```

**Configuration**: `backend/src/main/resources/application.yml`

### Frontend (Angular)

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm start
# Access at http://localhost:4200 (dev server) or http://localhost:3000 (Docker)

# Run tests
npm test

# Lint code
npm run lint

# Build for production
npm run build
```

**Configuration**: `frontend/src/environments/`

## API Endpoints

### Tasks API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/tasks` | Get all tasks |
| `GET` | `/api/tasks/{id}` | Get task by ID |
| `POST` | `/api/tasks` | Create new task |
| `PUT` | `/api/tasks/{id}` | Update existing task |
| `DELETE` | `/api/tasks/{id}` | Delete task |
| `GET` | `/api/tasks/status/{status}` | Get tasks by status |

### Task Status Values

- `0` - Pending
- `1` - In Progress
- `2` - Completed

### Example Request (Create Task)

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Task",
    "description": "Task description",
    "status": 0,
    "dueDate": "2024-12-31T23:59:59"
  }'
```

## Environment Variables

### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| `ORACLE_HOST` | `oracle-db` | Oracle database host |
| `ORACLE_PORT` | `1521` | Oracle database port |
| `ORACLE_SID` | `XEPDB1` | Oracle SID (pluggable database) |
| `ORACLE_USER` | `taskmanager` | Database username |
| `ORACLE_PASSWORD` | `taskmanager123` | Database password |
| `SERVER_PORT` | `8080` | Spring Boot server port |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `BACKEND_URL` | `http://backend:8080` | Backend API URL |

## Azure Deployment

### Prerequisites

1. Azure CLI logged in: `az login`
2. Azure subscription set: `az account set --subscription <subscription-id>`
3. Terraform installed and initialized

### Deploy Infrastructure

```bash
cd infra

# Initialize Terraform
terraform init

# Plan deployment
terraform plan -var="oracle_admin_password=YourSecurePassword123!"

# Apply infrastructure
terraform apply -var="oracle_admin_password=YourSecurePassword123!"

# Get outputs
terraform output
```

### Build and Push Container Images

```bash
# Get ACR login server from Terraform output
ACR_LOGIN_SERVER=$(terraform output -raw acr_login_server)

# Login to ACR
az acr login --name ${ACR_LOGIN_SERVER%%.*}

# Build and push backend
cd ../backend
docker build -t $ACR_LOGIN_SERVER/taskmanager-backend:latest .
docker push $ACR_LOGIN_SERVER/taskmanager-backend:latest

# Build and push frontend
cd ../frontend
docker build -t $ACR_LOGIN_SERVER/taskmanager-frontend:latest .
docker push $ACR_LOGIN_SERVER/taskmanager-frontend:latest
```

### Deploy to AKS

```bash
# Get AKS credentials
az aks get-credentials --resource-group taskmanager-dev-rg --name taskmanager-dev-aks

# Update K8s manifests with ACR login server
export ACR_LOGIN_SERVER=$(terraform output -raw acr_login_server)
envsubst < infra/k8s/backend-deployment.yaml | kubectl apply -f -
envsubst < infra/k8s/frontend-deployment.yaml | kubectl apply -f -
kubectl apply -f infra/k8s/ingress.yaml

# Check deployment status
kubectl get pods
kubectl get services
kubectl get ingress

# View logs
kubectl logs -l app=taskmanager-backend
kubectl logs -l app=taskmanager-frontend
```

### Install NGINX Ingress Controller

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

## Database Management

### Access Oracle Database

```bash
# Connect via Docker
docker exec -it $(docker-compose ps -q oracle-db) sqlplus taskmanager/taskmanager123@XEPDB1

# Or connect via SQL*Plus locally
sqlplus taskmanager/taskmanager123@localhost:1521/XEPDB1
```

### Database Schema

```sql
-- TASKS table
CREATE TABLE TASKS (
    ID NUMBER(10) PRIMARY KEY,
    TITLE VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(2000),
    STATUS NUMBER(1) DEFAULT 0 NOT NULL,
    CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    DUE_DATE TIMESTAMP NULL,
    CONSTRAINT CHK_STATUS CHECK (STATUS IN (0, 1, 2))
);

-- TASKS_SEQ sequence
CREATE SEQUENCE TASKS_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;
```

## Project Structure

```
migratedapp/
├── backend/                   # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/taskmanager/
│   │   │   │       ├── controller/     # REST controllers
│   │   │   │       ├── model/          # Domain models
│   │   │   │       ├── repository/     # Data access
│   │   │   │       └── service/        # Business logic
│   │   │   └── resources/
│   │   │       └── application.yml     # Configuration
│   │   └── test/                       # Unit tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                  # Angular frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/             # UI components
│   │   │   ├── models/                 # TypeScript models
│   │   │   └── services/               # API services
│   │   └── environments/               # Environment configs
│   ├── Dockerfile
│   ├── package.json
│   └── angular.json
├── db/
│   └── init.sql               # Database initialization
├── infra/
│   ├── main.tf                # Root Terraform config
│   ├── variables.tf           # Terraform variables
│   ├── outputs.tf             # Terraform outputs
│   ├── modules/
│   │   ├── oracle-db/         # Oracle VM module
│   │   └── aks/               # AKS cluster module
│   └── k8s/                   # Kubernetes manifests
│       ├── backend-deployment.yaml
│       ├── frontend-deployment.yaml
│       └── ingress.yaml
├── docker-compose.yml         # Local development stack
├── .gitignore
└── README.md                  # This file
```

## Tech Stack Summary

### Frontend
- **Framework**: Angular 18.x
- **Language**: TypeScript 5.x
- **HTTP Client**: Angular HttpClient
- **Styling**: CSS3 with Angular Material (optional)
- **Build Tool**: Angular CLI with Webpack

### Backend
- **Framework**: Spring Boot 3.3.x
- **Language**: Java 21 (LTS)
- **Web**: Spring Web MVC
- **Data Access**: Spring Data JDBC
- **Database Driver**: Oracle JDBC 21.x
- **API Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven 3.9+

### Database
- **Database**: Oracle Database XE 21c
- **Container**: gvenzl/oracle-xe:21-slim
- **Connection**: JDBC Thin Driver

### DevOps
- **Containerization**: Docker, Docker Compose
- **Orchestration**: Kubernetes (Azure AKS)
- **IaC**: Terraform 1.5+
- **Cloud Provider**: Microsoft Azure
- **Container Registry**: Azure Container Registry (ACR)

## Troubleshooting

### Oracle Container Won't Start

```bash
# Check logs
docker-compose logs oracle-db

# Common issue: Not enough memory allocated to Docker
# Solution: Increase Docker Desktop memory to at least 4GB

# Remove and recreate
docker-compose down -v
docker-compose up -d oracle-db
```

### Backend Can't Connect to Database

```bash
# Verify Oracle is healthy
docker-compose exec oracle-db healthcheck.sh

# Check connection from backend
docker-compose exec backend curl -v telnet://oracle-db:1521

# Verify credentials
docker-compose exec oracle-db sqlplus taskmanager/taskmanager123@XEPDB1
```

### Frontend Can't Reach Backend

```bash
# Check backend health
curl http://localhost:8080/actuator/health

# Verify backend is accessible from frontend container
docker-compose exec frontend curl http://backend:8080/actuator/health

# Check environment variable
docker-compose exec frontend env | grep BACKEND_URL
```

## License

Proprietary - Internal use only

## Support

For issues and questions:
- **Backend Issues**: Create issue with label `backend`
- **Frontend Issues**: Create issue with label `frontend`
- **Infrastructure Issues**: Create issue with label `infrastructure`
- **Database Issues**: Create issue with label `database`
