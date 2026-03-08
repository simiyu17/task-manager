# Docker Compose Setup Guide

## Architecture Overview

This docker-compose configuration deploys a complete Task Manager system with:
- **PostgreSQL 17**: Database for both application and Keycloak
- **Keycloak 26.0.5**: Identity and Access Management
- **Spring Boot Backend**: Task Manager API (Java 25)
- **Angular 21 Frontend**: User Interface with Nginx

## Prerequisites

- Docker 24.0+ installed
- Docker Compose 2.0+ installed
- At least 4GB RAM available
- Ports 80, 443, 8080, 8082, 5433 available

## Quick Start

### 1. Clone and Navigate
```bash
cd /path/to/task-manager
```

### 2. Verify Directory Structure
```
task-manager/
├── task-manager-backend/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── ... (Spring Boot code)
└── task-manager-frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── ... (Angular code)
```

### 3. Start All Services
```bash
cd task-manager-backend
docker-compose up -d
```

### 4. Check Status
```bash
docker-compose ps
docker-compose logs -f
```

### 5. Access Applications
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8082/task-manager
- **Keycloak Admin**: http://localhost:8080 (admin/admin)

## Service Details

### PostgreSQL Database
```yaml
Service: postgres
Image: postgres:17-alpine
Port: 5433:5432
Credentials:
  - Database: task_manager_db
  - User: root
  - Password: postgres
```

**Health Check**: Checks database readiness every 10s

**Volume**: Persistent data in `task-manager-postgres-data`

### Keycloak
```yaml
Service: keycloak
Image: quay.io/keycloak/keycloak:26.0.5
Port: 8080:8080
Admin Credentials:
  - Username: admin
  - Password: admin
```

**Features Enabled**:
- Development mode (start-dev)
- Health and metrics endpoints
- PostgreSQL database backend

**Initial Setup Required**:
1. Login to admin console: http://localhost:8080
2. Create realm: `task-manager`
3. Create client: `task-manager-client`
4. Configure redirect URIs: `http://localhost/*`
5. Enable Direct Access Grants

### Backend (Spring Boot)
```yaml
Service: backend
Build: ./task-manager-backend/Dockerfile
Port: 8082:8082
Profiles: docker
```

**Environment Variables**:
- Connected to `postgres` service (internal network)
- Keycloak issuer: `http://keycloak:8080/realms/task-manager`
- Upload directory: `/app/uploads` (persistent volume)

**JVM Settings**:
- Max RAM: 75% of container memory
- String deduplication enabled
- Container support enabled

### Frontend (Angular 21)
```yaml
Service: frontend
Build: ./task-manager-frontend/Dockerfile
Ports: 80:80, 443:443
Server: Nginx 1.27
```

**Features**:
- API proxy to backend (avoids CORS)
- Keycloak proxy at `/auth/`
- Static asset caching (1 year)
- Gzip compression
- Security headers

## Configuration Files

### Backend Dockerfile Optimizations
✅ Multi-stage build (Gradle + JRE)
✅ Layer caching for dependencies
✅ Java 25 container support
✅ Non-root user
✅ Health checks
✅ JVM tuning for containers

### Frontend Dockerfile
✅ Node 22 build stage
✅ Nginx 1.27 runtime
✅ Production build optimization
✅ Custom nginx configuration
✅ Non-root nginx user

### Nginx Configuration
✅ API proxy to backend
✅ Keycloak proxy
✅ Angular SPA routing
✅ Gzip compression
✅ Static asset caching
✅ Security headers

## Docker Compose Commands

### Start services
```bash
# Start all services in background
docker-compose up -d

# Start specific service
docker-compose up -d backend

# Start with rebuild
docker-compose up -d --build
```

### Stop services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Stop and remove images
docker-compose down --rmi all
```

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Service management
```bash
# Restart service
docker-compose restart backend

# Rebuild and restart
docker-compose up -d --build backend

# Scale service (if stateless)
docker-compose up -d --scale backend=3
```

### Health checks
```bash
# Check all service health
docker-compose ps

# Detailed health status
docker inspect task-manager-backend --format='{{.State.Health.Status}}'
docker inspect task-manager-keycloak --format='{{.State.Health.Status}}'
```

## Environment Variables

### Override defaults
Create `.env` file in same directory as docker-compose.yml:

```env
# Database
POSTGRES_PASSWORD=super_secure_password
POSTGRES_DB=task_manager_db

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=change_me_in_production

# Backend
SPRING_PROFILES_ACTIVE=docker
JAVA_OPTS=-XX:MaxRAMPercentage=80.0

# Frontend
API_URL=http://localhost:8082/task-manager
```

### Use custom env file
```bash
docker-compose --env-file .env.production up -d
```

## Network Architecture

```
Docker Network: task-manager-network (bridge)

Internet
   │
   ├─→ :80, :443 ──→ Frontend (Nginx)
   │                    │
   │                    ├─→ /task-manager/api/* ──→ Backend :8082
   │                    └─→ /auth/* ──→ Keycloak :8080
   │
   ├─→ :8082 ──→ Backend (Spring Boot)
   │               │
   │               ├─→ postgres:5432 (internal)
   │               └─→ keycloak:8080 (internal)
   │
   ├─→ :8080 ──→ Keycloak
   │               │
   │               └─→ postgres:5432 (internal)
   │
   └─→ :5433 ──→ PostgreSQL :5432
```

## Volume Management

### List volumes
```bash
docker volume ls | grep task-manager
```

### Inspect volume
```bash
docker volume inspect task-manager-postgres-data
docker volume inspect task-manager-backend-uploads
```

### Backup volumes
```bash
# Backup database
docker exec task-manager-postgres pg_dump -U root task_manager_db > backup.sql

# Backup uploads
docker run --rm -v task-manager-backend-uploads:/data -v $(pwd):/backup \
  alpine tar czf /backup/uploads-backup.tar.gz -C /data .
```

### Restore volumes
```bash
# Restore database
cat backup.sql | docker exec -i task-manager-postgres psql -U root task_manager_db

# Restore uploads
docker run --rm -v task-manager-backend-uploads:/data -v $(pwd):/backup \
  alpine tar xzf /backup/uploads-backup.tar.gz -C /data
```

## Production Considerations

### Security
- [ ] Change default passwords in `.env` file
- [ ] Use secrets management (Docker secrets or external vault)
- [ ] Enable HTTPS with SSL certificates
- [ ] Configure firewall rules
- [ ] Use non-default database ports
- [ ] Enable PostgreSQL SSL connections
- [ ] Configure Keycloak production mode

### Performance
- [ ] Adjust JVM heap sizes based on load
- [ ] Configure database connection pooling
- [ ] Enable database query caching
- [ ] Add Redis for session management
- [ ] Configure CDN for static assets
- [ ] Enable database replication for read scaling

### Monitoring
```yaml
# Add to docker-compose.yml
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
```

### High Availability
- Use Docker Swarm or Kubernetes
- Configure database replication
- Load balancer for multiple backend instances
- Shared volume for uploads (NFS, S3, etc.)

## Troubleshooting

### Service won't start
```bash
# Check logs
docker-compose logs service_name

# Check if port is already in use
sudo netstat -tulpn | grep :8080

# Verify Docker resources
docker system df
docker system prune
```

### Database connection issues
```bash
# Test database connectivity
docker exec task-manager-postgres psql -U root -d task_manager_db -c "SELECT 1"

# Check backend can reach database
docker exec task-manager-backend nc -zv postgres 5432
```

### Keycloak issues
```bash
# Check Keycloak logs
docker-compose logs -f keycloak

# Verify realm configuration
# Login to http://localhost:8080 and check realm settings
```

### Frontend can't reach backend
```bash
# Test nginx proxy
docker exec task-manager-frontend wget -O- http://backend:8082/task-manager/actuator/health

# Check nginx configuration
docker exec task-manager-frontend nginx -t

# Reload nginx
docker exec task-manager-frontend nginx -s reload
```

### Clean restart
```bash
# Stop all services
docker-compose down -v

# Remove all related containers
docker rm -f $(docker ps -a | grep task-manager | awk '{print $1}')

# Remove all related volumes
docker volume rm $(docker volume ls | grep task-manager | awk '{print $2}')

# Rebuild and start
docker-compose up -d --build
```

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to server
        run: |
          docker-compose -f docker-compose.yml pull
          docker-compose -f docker-compose.yml up -d --build
```

### GitLab CI Example
```yaml
deploy:
  stage: deploy
  script:
    - docker-compose down
    - docker-compose up -d --build
  only:
    - main
```

## Development vs Production

### Development (docker-compose.yml)
- Uses development mode for Keycloak
- Exposes all ports
- Volume mounts for hot reload
- Debug logging enabled

### Production (docker-compose.prod.yml)
Create a separate file for production:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

Key differences:
- Keycloak in production mode
- Restricted port exposure
- No volume mounts for code
- Production JVM settings
- SSL/TLS enabled
- Secrets management

## Next Steps

1. **Keycloak Configuration**
   - Create realm and client
   - Configure user federation
   - Set up roles and permissions

2. **Database Initialization**
   - Liquibase will run automatically
   - Verify schema creation

3. **Frontend Configuration**
   - Update environment files
   - Configure Keycloak client settings

4. **Testing**
   - Test user registration
   - Test API endpoints
   - Load testing with tools like k6

5. **Monitoring Setup**
   - Configure application logging
   - Set up metrics collection
   - Create dashboards

## Support

For issues or questions:
- Check service logs: `docker-compose logs -f`
- Verify health checks: `docker-compose ps`
- Review DOCKER.md for detailed configuration


