# Docker Deployment Guide

This guide provides instructions for deploying the Task Manager application using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB of available RAM
- Ports 80, 8080, 8180, and 5432 available

## Architecture

The Docker Compose setup includes:

1. **task-manager-ui** (Angular Frontend) - Port 80
2. **task-manager-api** (Spring Boot 3.5.10 Backend) - Port 8080
3. **keycloak** (Keycloak 26.0.5) - Port 8180
4. **postgres-db** (PostgreSQL 16 for Application) - Port 5432
5. **keycloak-db** (PostgreSQL 16 for Keycloak)

## Quick Start

### 1. Build and Start All Services

```bash
docker-compose up -d
```

This will:
- Build the Angular app and Spring Boot API
- Pull Keycloak and PostgreSQL images
- Start all services in detached mode

### 2. Check Service Status

```bash
docker-compose ps
```

### 3. View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f task-manager-ui
docker-compose logs -f task-manager-api
docker-compose logs -f keycloak
```

## Access the Application

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Keycloak Admin Console**: http://localhost:8180
  - Username: `admin`
  - Password: `admin`

## Keycloak Configuration

After starting the services, configure Keycloak:

### 1. Create Realm

1. Access Keycloak at http://localhost:8180
2. Login with admin/admin
3. Create a new realm named `task-manager`

### 2. Create Clients

#### Frontend Client (task-manager-ui-client)
```
Client ID: task-manager-ui-client
Client Protocol: openid-connect
Access Type: public
Standard Flow Enabled: ON
Valid Redirect URIs: 
  - http://localhost/*
  - http://localhost:4200/*
Web Origins:
  - http://localhost
  - http://localhost:4200
```

#### Backend Client (task-manager-api-client)
```
Client ID: task-manager-api-client
Client Protocol: openid-connect
Access Type: confidential
Service Accounts Enabled: ON
Authorization Enabled: ON
Valid Redirect URIs: 
  - http://localhost:8080/*
```

### 3. Create Users and Roles

Create necessary users and assign appropriate roles for your application.

For detailed Keycloak setup, refer to [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md)

## Managing Services

### Stop All Services
```bash
docker-compose stop
```

### Start Stopped Services
```bash
docker-compose start
```

### Restart a Service
```bash
docker-compose restart task-manager-ui
```

### Rebuild and Restart a Service
```bash
# Rebuild frontend
docker-compose up -d --build task-manager-ui

# Rebuild backend
docker-compose up -d --build task-manager-api
```

### Stop and Remove All Services
```bash
docker-compose down
```

### Stop and Remove with Volumes
```bash
docker-compose down -v
```

## Development Workflow

### Live Development with Docker

For development, you might want to run services selectively:

```bash
# Start only backend services
docker-compose up -d keycloak postgres-db

# Run frontend locally
npm start
```

### Rebuilding After Code Changes

#### Frontend Changes
```bash
docker-compose up -d --build task-manager-ui
```

#### Backend Changes
```bash
docker-compose up -d --build task-manager-api
```

## Troubleshooting

### Service Won't Start

1. Check logs:
```bash
docker-compose logs <service-name>
```

2. Check if ports are already in use:
```bash
# Linux/Mac
sudo netstat -tlnp | grep -E '80|8080|8180|5432'

# Or use lsof
sudo lsof -i :80
sudo lsof -i :8080
```

3. Verify service health:
```bash
docker-compose ps
```

### Database Connection Issues

If backend can't connect to database:

```bash
# Check if postgres is healthy
docker-compose ps postgres-db

# Restart the backend
docker-compose restart task-manager-api
```

### Keycloak Not Ready

Keycloak takes ~60 seconds to start. Wait for the health check:

```bash
docker-compose logs -f keycloak
```

Look for: `Keycloak 26.0.5 started`

### Frontend Can't Reach Backend

1. Check if backend is running:
```bash
curl http://localhost:8080/actuator/health
```

2. Verify network connectivity inside container:
```bash
docker exec task-manager-ui ping task-manager-api
```

### Clear Everything and Start Fresh

```bash
# Stop and remove containers, networks, and volumes
docker-compose down -v

# Remove images
docker rmi task-manager-ui task-manager-api

# Rebuild and start
docker-compose up -d --build
```

## Production Considerations

For production deployment, update the following:

### 1. Environment Variables

Create a `.env` file:
```env
# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=<strong-password>
KC_HOSTNAME=your-domain.com

# Database
POSTGRES_PASSWORD=<strong-password>
KEYCLOAK_DB_PASSWORD=<strong-password>

# Application
SPRING_PROFILES_ACTIVE=prod
```

### 2. Update docker-compose.yml

- Use environment-specific configurations
- Enable HTTPS/SSL
- Configure proper secrets management
- Set resource limits
- Use production-grade database settings

### 3. Security

- Change all default passwords
- Use secrets management (Docker Secrets, Vault)
- Enable SSL/TLS
- Configure proper firewalls
- Set up monitoring and logging

### 4. Networking

- Use reverse proxy (nginx, traefik)
- Configure proper domain names
- Set up load balancing if needed

## Data Persistence

Data is persisted in Docker volumes:

- `postgres_data`: Application database
- `keycloak_postgres_data`: Keycloak database

### Backup Volumes

```bash
# Backup application database
docker exec postgres-db pg_dump -U taskmanager taskmanager > backup.sql

# Backup keycloak database
docker exec keycloak-db pg_dump -U keycloak keycloak > keycloak_backup.sql
```

### Restore Volumes

```bash
# Restore application database
docker exec -i postgres-db psql -U taskmanager taskmanager < backup.sql

# Restore keycloak database
docker exec -i keycloak-db psql -U keycloak keycloak < keycloak_backup.sql
```

## Health Checks

All services include health checks:

```bash
# Check health status
docker inspect --format='{{.State.Health.Status}}' task-manager-ui
docker inspect --format='{{.State.Health.Status}}' task-manager-api
docker inspect --format='{{.State.Health.Status}}' keycloak
```

## Resource Monitoring

```bash
# Monitor resource usage
docker stats

# Specific container
docker stats task-manager-ui
```

## Support

For issues related to:
- **Frontend**: Check [README.md](README.md)
- **Keycloak**: Check [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md)
- **Environment**: Check [ENVIRONMENT.md](ENVIRONMENT.md)
