# Docker Configuration for Task Manager Backend

## Dockerfile Optimizations for Java 25

### ✅ Key Optimizations Applied

#### 1. **Multi-Stage Build**
- **Build Stage**: Uses `gradle:8.11-jdk25-alpine` for compiling
- **Runtime Stage**: Uses `eclipse-temurin:25-jre-alpine` (smaller, JRE only)
- **Benefit**: Reduces final image size by ~60% (no build tools in runtime)

#### 2. **Layer Caching Strategy**
```dockerfile
# Dependencies layer (cached unless build.gradle changes)
COPY gradlew gradle build.gradle settings.gradle
RUN ./gradlew dependencies

# Source code layer (changes frequently)
COPY src src
RUN ./gradlew bootJar
```
- Dependency downloads are cached separately from source code
- Rebuilds are faster when only source changes

#### 3. **Java 25 Optimized JVM Flags**
```bash
-XX:+UseContainerSupport          # Enable container awareness
-XX:MaxRAMPercentage=75.0         # Use 75% of container memory
-XX:InitialRAMPercentage=50.0     # Start with 50% allocation
-XX:+OptimizeStringConcat         # String performance boost
-XX:+UseStringDeduplication       # Reduce memory for duplicate strings
-XX:+ExitOnOutOfMemoryError       # Exit cleanly on OOM (for orchestration)
-Djava.security.egd=file:/dev/./urandom  # Faster startup (non-blocking entropy)
```

#### 4. **Security Best Practices**
- ✅ Non-root user (`spring:spring`)
- ✅ Minimal base image (Alpine Linux)
- ✅ Explicit file permissions
- ✅ No unnecessary tools in runtime image

#### 5. **Health Check Configuration**
- Endpoint: `/task-manager/actuator/health`
- Interval: 30s (check every 30 seconds)
- Timeout: 3s (request timeout)
- Start Period: 60s (grace period for app startup)
- Retries: 3 (mark unhealthy after 3 failures)

#### 6. **Production Readiness**
- ✅ Dedicated uploads directory (`/app/uploads`)
- ✅ Proper signal handling with `sh -c` wrapper
- ✅ Build uses `bootJar` (optimized Spring Boot packaging)
- ✅ Tests skipped in Docker build (run in CI/CD pipeline instead)
- ✅ `--no-daemon` flag for Gradle (better for containers)

### Image Size Comparison
| Stage | Base Image | Approximate Size |
|-------|------------|------------------|
| Build | gradle:8.11-jdk25-alpine | ~800 MB |
| Runtime | eclipse-temurin:25-jre-alpine | ~180 MB |
| **Final Image** | **With Application** | **~250-300 MB** |

### Build Commands

#### Build the Docker image
```bash
docker build -t task-manager-backend:latest .
```

#### Build with custom tag
```bash
docker build -t task-manager-backend:1.0.0 .
```

#### Build and show progress
```bash
docker build --progress=plain -t task-manager-backend:latest .
```

### Run Commands

#### Run standalone (development)
```bash
docker run -d \
  --name task-manager-backend \
  -p 8082:8082 \
  -e POSTGRESQL_HOST=host.docker.internal \
  -e POSTGRESQL_PORT=5433 \
  -e POSTGRESQL_DATABASE=task_manager_db \
  -e POSTGRESQL_USER=root \
  -e POSTGRESQL_PASSWORD=postgres \
  task-manager-backend:latest
```

#### Run with volume for uploads
```bash
docker run -d \
  --name task-manager-backend \
  -p 8082:8082 \
  -v $(pwd)/uploads:/app/uploads \
  -e POSTGRESQL_HOST=postgres \
  task-manager-backend:latest
```

#### Check health status
```bash
docker inspect --format='{{.State.Health.Status}}' task-manager-backend
```

#### View logs
```bash
docker logs -f task-manager-backend
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRESQL_HOST` | localhost | PostgreSQL host |
| `POSTGRESQL_PORT` | 5433 | PostgreSQL port |
| `POSTGRESQL_DATABASE` | task_manager_db | Database name |
| `POSTGRESQL_USER` | root | Database username |
| `POSTGRESQL_PASSWORD` | postgres | Database password |
| `FILE_UPLOAD_DIR` | ./uploads | File upload directory |
| `JAVA_OPTS` | (see Dockerfile) | JVM options |

### Java 25 Specific Considerations

#### Why Eclipse Temurin 25?
- Official OpenJDK builds from Adoptium
- Well-tested and production-ready
- Regular security updates
- Alpine variant for minimal size

#### Java 25 Features Utilized
- Enhanced container support (better memory detection)
- Improved GC for containerized environments
- Virtual threads support (if enabled in code)
- Better startup performance

### Troubleshooting

#### Issue: Health check failing
```bash
# Check if actuator is enabled
docker exec task-manager-backend wget -qO- http://localhost:8082/task-manager/actuator/health
```

#### Issue: Out of memory
```bash
# Increase container memory limit
docker run -m 1g task-manager-backend:latest

# Or adjust JVM percentage
docker run -e JAVA_OPTS="-XX:MaxRAMPercentage=60.0" task-manager-backend:latest
```

#### Issue: Slow startup
```bash
# Check logs for initialization issues
docker logs task-manager-backend 2>&1 | grep -i error

# Increase health check start period
# Edit Dockerfile: --start-period=90s
```

### Performance Tuning

#### For production (4GB container)
```bash
docker run -m 4g \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=60.0" \
  task-manager-backend:latest
```

#### For development (1GB container)
```bash
docker run -m 1g \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=80.0 -XX:InitialRAMPercentage=50.0" \
  task-manager-backend:latest
```

### Next Steps
1. Create `docker-compose.yml` for multi-service deployment
2. Configure Keycloak integration
3. Set up Angular frontend
4. Configure reverse proxy (nginx)
5. Add environment-specific configs (dev, staging, prod)


