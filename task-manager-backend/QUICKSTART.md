# 🚀 Quick Start Guide - Task Manager Docker Deployment

## Prerequisites Check ✓

```bash
# Check Docker
docker --version  # Should be 24.0+

# Check Docker Compose
docker-compose --version  # Should be 2.0+

# Check available ports
netstat -tuln | grep -E ':(80|443|8080|8082|5433)'
```

## 5-Minute Setup 🎯

### Step 1: Build Backend Image
```bash
cd /home/simiyu/kazi/omboi/task-manager/task-manager-backend
docker build -t task-manager-backend:latest .
```

**Expected Output**: 
- Build completes successfully
- Final image size: ~250-300 MB

### Step 2: Start All Services
```bash
docker-compose up -d
```

**Services Starting**:
1. PostgreSQL (ready in ~10s)
2. Keycloak (ready in ~60s)  
3. Backend (ready in ~90s)
4. Frontend (ready in ~30s)

### Step 3: Monitor Startup
```bash
# Watch all logs
docker-compose logs -f

# Or check status
docker-compose ps
```

**Wait for**:
```
✓ postgres  - healthy
✓ keycloak  - healthy  
✓ backend   - healthy
✓ frontend  - healthy
```

### Step 4: Access Applications

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost | Configure after Keycloak setup |
| **Backend API** | http://localhost:8082/task-manager | Bearer token required |
| **Keycloak Admin** | http://localhost:8080 | admin / admin |
| **PostgreSQL** | localhost:5433 | root / postgres |

### Step 5: Configure Keycloak (First Time Only)

```bash
# 1. Open Keycloak Admin Console
open http://localhost:8080

# 2. Login with: admin / admin

# 3. Create Realm
- Click: "Create Realm"
- Name: task-manager
- Click: Create

# 4. Create Client
- Click: Clients → Create Client
- Client ID: task-manager-client
- Client Protocol: openid-connect
- Click: Next
- Enable: Direct access grants
- Valid redirect URIs: http://localhost/*
- Web origins: http://localhost
- Click: Save

# 5. Create Test User
- Click: Users → Add user
- Username: testuser
- Email: test@example.com
- Email verified: ON
- Click: Create
- Go to Credentials tab
- Set password: test123
- Temporary: OFF
```

### Step 6: Test the Setup

```bash
# Test database
docker exec task-manager-postgres psql -U root -d task_manager_db -c "SELECT 1"

# Test backend health
curl http://localhost:8082/task-manager/actuator/health

# Test frontend
curl http://localhost/

# Test Keycloak
curl http://localhost:8080/realms/task-manager
```

## Common Commands 📝

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Last 50 lines
docker-compose logs --tail=50 backend
```

### Restart Service
```bash
docker-compose restart backend
```

### Stop All Services
```bash
docker-compose down
```

### Stop and Remove All Data
```bash
docker-compose down -v
```

### Rebuild After Code Changes
```bash
# Backend only
docker-compose up -d --build backend

# All services
docker-compose up -d --build
```

## Troubleshooting 🔧

### Backend won't start
```bash
# Check database is ready
docker-compose logs postgres | grep "ready to accept connections"

# Check backend logs
docker-compose logs backend | tail -50

# Restart backend
docker-compose restart backend
```

### Keycloak not accessible
```bash
# Check Keycloak logs
docker-compose logs keycloak | tail -50

# Wait longer (can take 90s on first start)
sleep 90 && curl http://localhost:8080/health/ready
```

### Port already in use
```bash
# Find what's using the port
sudo netstat -tulpn | grep :8080

# Kill the process or change port in docker-compose.yml
```

### Clean slate restart
```bash
# Nuclear option - removes everything
docker-compose down -v
docker system prune -af
docker volume prune -f

# Then rebuild
docker-compose up -d --build
```

## Verification Checklist ✅

After startup, verify:

- [ ] All containers running: `docker-compose ps`
- [ ] All services healthy: Check health status
- [ ] Frontend accessible: http://localhost
- [ ] Backend API responding: http://localhost:8082/task-manager/actuator/health
- [ ] Keycloak admin accessible: http://localhost:8080
- [ ] Database accessible: Can connect on port 5433
- [ ] No errors in logs: `docker-compose logs | grep -i error`

## What's Next? 🎯

1. **Configure Keycloak** (see Step 5 above)
2. **Test API Endpoints** with Postman/curl
3. **Configure Frontend** to connect to backend
4. **Create Initial Data** in database
5. **Set up CI/CD Pipeline** for automated deployment
6. **Configure Monitoring** (Prometheus/Grafana)
7. **Enable HTTPS** for production

## Architecture Overview 🏗️

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       v
┌─────────────────────┐
│  Frontend (Nginx)   │ :80
│  Angular 21         │
└──────┬──────────────┘
       │
       ├─────> Backend API :8082
       │       (Spring Boot + Java 25)
       │       └─> PostgreSQL :5432
       │
       └─────> Keycloak :8080
               └─> PostgreSQL :5432
```

## Performance Tips 🚀

### For Development
```bash
# Use less memory
docker-compose up -d --scale backend=1

# Mount code for hot reload (requires changes to docker-compose.yml)
```

### For Production
```bash
# Use production environment
docker-compose -f docker-compose.prod.yml up -d

# Set resource limits in docker-compose.yml
# Enable monitoring and logging
# Use external database
```

## Getting Help 📚

1. **Check logs**: `docker-compose logs -f service_name`
2. **Check health**: `docker inspect service_name --format='{{.State.Health.Status}}'`
3. **Read docs**: 
   - `DOCKER.md` - Detailed Docker configuration
   - `DOCKER-COMPOSE.md` - Complete docker-compose guide
   - `DOCKERFILE-VALIDATION.md` - Validation report

## Success! 🎉

If all services are healthy and accessible, you're ready to develop!

```bash
# Your task manager is now running:
✓ Frontend:  http://localhost
✓ Backend:   http://localhost:8082/task-manager
✓ Keycloak:  http://localhost:8080
✓ Database:  localhost:5433
```

Happy coding! 🚀

