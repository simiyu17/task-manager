# ✅ Deployment Checklist

## Pre-Deployment Validation

### Environment Setup
- [ ] Docker 24.0+ installed
- [ ] Docker Compose 2.0+ installed
- [ ] Ports 80, 443, 8080, 8082, 5433 are available
- [ ] At least 4GB RAM available
- [ ] 10GB+ disk space available

### File Verification
- [x] `Dockerfile` created and optimized for Java 25
- [x] `docker-compose.yml` configured with all services
- [x] `.dockerignore` optimized for build
- [x] `.env.example` template provided
- [x] `nginx.conf` configured for Angular
- [x] `Dockerfile.angular` template ready
- [x] All documentation files created

### Code Verification
- [ ] Latest code committed to repository
- [ ] Application.yaml configured for Docker
- [ ] Build.gradle dependencies up to date
- [ ] Database migrations (Liquibase) ready

---

## Deployment Steps

### 1. Build Backend Image
```bash
cd /home/simiyu/kazi/omboi/task-manager/task-manager-backend
docker build -t task-manager-backend:latest .
```
**Expected**: Build completes in 5-6 minutes
- [ ] Build successful
- [ ] No errors in output
- [ ] Image created (~250-300 MB)

### 2. Create Environment File
```bash
cp .env.example .env
nano .env  # Edit passwords and configuration
```
**Update these values**:
- [ ] POSTGRES_PASSWORD (change from default)
- [ ] KEYCLOAK_ADMIN_PASSWORD (change from default)
- [ ] Any environment-specific URLs

### 3. Start Services
```bash
docker-compose up -d
```
**Monitor startup**:
- [ ] PostgreSQL starts (10-15s)
- [ ] Keycloak starts (60-90s)
- [ ] Backend starts (90-120s)
- [ ] Frontend starts (30s) - if configured

### 4. Verify All Services Healthy
```bash
docker-compose ps
```
**Check status**:
- [ ] postgres: healthy
- [ ] keycloak: healthy
- [ ] backend: healthy
- [ ] frontend: healthy (if configured)

### 5. Check Service Logs
```bash
docker-compose logs -f
```
**Look for**:
- [ ] No ERROR messages
- [ ] Database connected successfully
- [ ] Liquibase migrations completed
- [ ] Spring Boot started on port 8082
- [ ] No exceptions in startup

---

## Post-Deployment Configuration

### 6. Configure Keycloak
Access: http://localhost:8080

**Admin Login**:
- [ ] Login successful (admin/admin)
- [ ] Change admin password

**Create Realm**:
- [ ] Create new realm: `task-manager`
- [ ] Set realm as active

**Create Client**:
- [ ] Client ID: `task-manager-client`
- [ ] Client protocol: openid-connect
- [ ] Access type: public or confidential
- [ ] Valid redirect URIs: `http://localhost/*`
- [ ] Web origins: `http://localhost`
- [ ] Direct access grants: ENABLED

**Create Test User**:
- [ ] Username: testuser
- [ ] Email: test@example.com
- [ ] Email verified: ON
- [ ] Set password: test123
- [ ] Temporary: OFF

### 7. Test Backend API
```bash
# Health check
curl http://localhost:8082/task-manager/actuator/health

# Expected: {"status":"UP"}
```
- [ ] Health endpoint returns UP
- [ ] No errors in response

### 8. Test Database Connection
```bash
docker exec task-manager-postgres psql -U root -d task_manager_db -c "SELECT 1"
```
- [ ] Connection successful
- [ ] Query returns 1

### 9. Verify Keycloak Token
```bash
# Get token (replace with your test user credentials)
curl -X POST http://localhost:8080/realms/task-manager/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser" \
  -d "password=test123" \
  -d "grant_type=password" \
  -d "client_id=task-manager-client"
```
- [ ] Token received successfully
- [ ] No authentication errors

### 10. Test Backend with Token
```bash
# Use token from previous step
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  http://localhost:8082/task-manager/api/tasks
```
- [ ] API responds correctly
- [ ] Authentication successful

---

## Frontend Deployment (If Applicable)

### 11. Prepare Frontend
- [ ] Create `Dockerfile` in frontend directory (use Dockerfile.angular template)
- [ ] Copy `nginx.conf` to frontend directory
- [ ] Update `docker-compose.yml` frontend build context
- [ ] Configure environment variables in Angular

### 12. Build and Start Frontend
```bash
docker-compose up -d --build frontend
```
- [ ] Frontend builds successfully
- [ ] Frontend starts on port 80
- [ ] Nginx serves Angular app

### 13. Test Frontend
- [ ] Access http://localhost
- [ ] Page loads correctly
- [ ] Can navigate routes
- [ ] Can login via Keycloak
- [ ] Can call backend API

---

## Monitoring & Maintenance

### 14. Set Up Monitoring
```bash
# Check container stats
docker stats
```
- [ ] Monitor CPU usage
- [ ] Monitor memory usage
- [ ] Monitor disk I/O

### 15. Configure Logging
```bash
# View logs
docker-compose logs -f backend
```
- [ ] Configure log rotation
- [ ] Set up centralized logging (optional)
- [ ] Configure log levels

### 16. Backup Strategy
```bash
# Database backup
docker exec task-manager-postgres pg_dump -U root task_manager_db > backup.sql
```
- [ ] Set up automated backups
- [ ] Test restore procedure
- [ ] Document backup location

### 17. Security Hardening
- [ ] Change all default passwords
- [ ] Restrict port access (firewall rules)
- [ ] Enable HTTPS/SSL certificates
- [ ] Configure PostgreSQL SSL
- [ ] Use Docker secrets for sensitive data
- [ ] Regular security updates

---

## Production Considerations

### 18. Performance Optimization
- [ ] Adjust JVM heap sizes based on load
- [ ] Configure database connection pooling
- [ ] Enable query caching
- [ ] Set up CDN for static assets
- [ ] Configure load balancer (if scaling)

### 19. High Availability
- [ ] Database replication
- [ ] Multiple backend instances
- [ ] Shared storage for uploads
- [ ] Load balancer configuration
- [ ] Health check monitoring

### 20. CI/CD Integration
- [ ] Automated builds
- [ ] Automated testing
- [ ] Automated deployment
- [ ] Rollback strategy
- [ ] Blue-green deployment

---

## Troubleshooting Reference

### Common Issues Checklist

#### Service Won't Start
- [ ] Check logs: `docker-compose logs service_name`
- [ ] Check port conflicts: `netstat -tulpn | grep :PORT`
- [ ] Verify Docker resources: `docker system df`
- [ ] Check dependencies: `docker-compose ps`

#### Database Connection Failed
- [ ] Verify PostgreSQL is running: `docker-compose ps postgres`
- [ ] Check connection string in logs
- [ ] Test connection: `docker exec backend nc -zv postgres 5432`
- [ ] Verify credentials in .env file

#### Health Check Failing
- [ ] Wait for full startup (90s for backend)
- [ ] Test endpoint manually: `docker exec backend wget -qO- http://localhost:8082/task-manager/actuator/health`
- [ ] Check application logs for errors
- [ ] Verify actuator is enabled

#### Out of Memory
- [ ] Check container stats: `docker stats`
- [ ] Adjust JVM settings in docker-compose.yml
- [ ] Increase container memory limit
- [ ] Review application memory leaks

---

## Final Verification

### System Health Check
```bash
# All services running
docker-compose ps | grep healthy

# No errors in logs
docker-compose logs | grep -i error

# Ports accessible
curl http://localhost:80          # Frontend
curl http://localhost:8080        # Keycloak
curl http://localhost:8082/task-manager/actuator/health  # Backend

# Database accessible
docker exec task-manager-postgres psql -U root -d task_manager_db -c "SELECT 1"
```

**All checks passing**:
- [ ] All services show "healthy"
- [ ] No critical errors in logs
- [ ] All ports responding
- [ ] Database queries working

---

## Sign Off

### Deployment Completed By
- **Name**: _________________
- **Date**: _________________
- **Environment**: [ ] Development [ ] Staging [ ] Production

### Verification
- [ ] All services running
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Backup configured
- [ ] Monitoring enabled

### Notes
```
Add any deployment-specific notes here:
- Custom configurations
- Known issues
- Special instructions
```

---

## 📞 Emergency Contacts

**In case of issues during deployment**:
1. Check logs first: `docker-compose logs -f`
2. Review documentation: QUICKSTART.md, DOCKER.md
3. Check validation report: DOCKERFILE-VALIDATION.md
4. Restart services: `docker-compose restart`
5. Nuclear option: `docker-compose down -v && docker-compose up -d --build`

---

## 🎉 Success Criteria

Deployment is successful when:
- ✅ All services are healthy
- ✅ Frontend accessible at http://localhost
- ✅ Backend API responding correctly
- ✅ Keycloak authentication working
- ✅ Database migrations completed
- ✅ Test user can login
- ✅ No errors in logs
- ✅ Health checks passing

**Status**: [ ] ✅ COMPLETE [ ] ⚠️ ISSUES [ ] ❌ FAILED

---

**Last Updated**: 2026-03-08  
**Version**: 1.0.0  
**Java**: 25  
**Spring Boot**: 3.5.10  

