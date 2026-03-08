# Environment Variables Reference

This document describes all environment variables used in the Task Manager application deployment.

## Required Variables

### Domain & SSL Configuration
```env
DOMAIN=your-domain.com              # Your domain name for the application
ACME_EMAIL=your-email@example.com   # Email for Let's Encrypt SSL certificates
```

## Database Configuration

### Application Database (PostgreSQL)
```env
POSTGRESQL_HOST=localhost           # PostgreSQL host (use host.docker.internal in Docker)
POSTGRESQL_PORT=5432                # PostgreSQL port
POSTGRESQL_DATABASE=taskmanager     # Database name for application
POSTGRESQL_USER=taskmanager         # Database user for application
POSTGRESQL_PASSWORD=your_password   # Database password for application
```

**Spring Boot Mapping:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRESQL_HOST}:${POSTGRESQL_PORT}/${POSTGRESQL_DATABASE}
    username: ${POSTGRESQL_USER}
    password: ${POSTGRESQL_PASSWORD}
```

### Keycloak Database (PostgreSQL)
```env
KC_DB_USERNAME=keycloak             # Database user for Keycloak
KC_DB_PASSWORD=your_password        # Database password for Keycloak
```

**Docker Compose Mapping:**
```yaml
environment:
  KC_DB_URL: jdbc:postgresql://host.docker.internal:5432/keycloak
  KC_DB_USERNAME: ${KC_DB_USERNAME}
  KC_DB_PASSWORD: ${KC_DB_PASSWORD}
```

## Spring Boot Configuration

### Server Configuration
```env
SERVER_PORT=8082                    # Spring Boot application port
SERVER_CONTEXT_PATH=/task-manager   # Application context path
```

**Spring Boot Mapping:**
```yaml
server:
  port: ${SERVER_PORT:8082}
  servlet:
    context-path: ${SERVER_CONTEXT_PATH:/task-manager}
```

**Result:** Application accessible at `http://localhost:8082/task-manager`

### Database Connection Pool (HikariCP)
```env
DB_HIKARI_MAX_POOL_SIZE=20          # Maximum number of connections in pool
DB_HIKARI_MIN_IDLE=10               # Minimum idle connections
DB_HIKARI_CONNECTION_TIMEOUT=60000  # Connection timeout in ms
DB_HIKARI_IDLE_TIMEOUT=600000       # Idle timeout in ms
DB_HIKARI_MAX_LIFETIME=1800000      # Maximum lifetime of connection in ms
```

**Spring Boot Mapping:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: ${DB_HIKARI_MAX_POOL_SIZE:20}
      minimum-idle: ${DB_HIKARI_MIN_IDLE:10}
      connection-timeout: ${DB_HIKARI_CONNECTION_TIMEOUT:60000}
      idle-timeout: ${DB_HIKARI_IDLE_TIMEOUT:600000}
      max-lifetime: ${DB_HIKARI_MAX_LIFETIME:1800000}
```

### File Upload Configuration
```env
FILE_UPLOAD_DIR=./uploads           # Directory for uploaded files
```

**Spring Boot Mapping:**
```yaml
file:
  upload-dir: ${FILE_UPLOAD_DIR:./uploads}
```

## Keycloak Configuration

### Admin Configuration
```env
KEYCLOAK_ADMIN=admin                # Keycloak admin username
KEYCLOAK_ADMIN_PASSWORD=your_pass   # Keycloak admin password
```

### Realm & Client Configuration
```env
KEYCLOAK_REALM=task-manager                    # Keycloak realm name
KEYCLOAK_RESOURCE_API=task-manager-api-client  # Backend client ID
KEYCLOAK_RESOURCE_UI=task-manager-ui-client    # Frontend client ID
```

**Spring Boot Mapping:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/${KEYCLOAK_REALM}
```

### Keycloak URLs
```env
AUTH_BASE_URL=http://keycloak:8080  # Keycloak base URL (Docker internal)
AUTH_REALM_NAME=task-manager         # Realm name for Angular app
AUTH_CLIENT_ID=task-manager-ui-client # Client ID for Angular app
```

**Angular Environment Mapping:**
```typescript
export const environment = {
  authBaseUrl: process.env.AUTH_BASE_URL,
  authRealmName: process.env.AUTH_REALM_NAME,
  authClientId: process.env.AUTH_CLIENT_ID
};
```

## Angular Configuration

### API Configuration
```env
API_BASE_URL=http://task-manager-api:8082/task-manager/api/v1
```

**Angular Environment Mapping:**
```typescript
export const environment = {
  apiBaseUrl: 'http://task-manager-api:8082/task-manager/api/v1'
};
```

## Environment-Specific Configurations

### Development (Local)
```env
# API accessible at localhost
API_BASE_URL=http://localhost:8082/task-manager/api/v1
AUTH_BASE_URL=http://localhost:8080
POSTGRESQL_HOST=localhost
```

### Docker Deployment
```env
# Services use Docker internal networking
API_BASE_URL=http://task-manager-api:8082/task-manager/api/v1
AUTH_BASE_URL=http://keycloak:8080
POSTGRESQL_HOST=host.docker.internal
```

### Production (with Traefik)
```env
# All services behind Traefik reverse proxy
DOMAIN=your-domain.com
API_BASE_URL=https://your-domain.com/task-manager/api/v1
AUTH_BASE_URL=https://your-domain.com/auth
```

## Application Profiles

### Spring Boot Profiles
```env
SPRING_PROFILES_ACTIVE=docker       # Active Spring profile
```

**Available Profiles:**
- `default` - Local development
- `docker` - Docker deployment
- `prod` - Production deployment
- `staging` - Staging deployment

## URL Structure

### Application URLs by Environment

#### Development (Local)
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8082/task-manager/api/v1
- **Backend Health**: http://localhost:8082/task-manager/actuator/health
- **Keycloak**: http://localhost:8080
- **Keycloak Admin**: http://localhost:8080/admin

#### Docker (Internal Network)
- **Frontend**: http://task-manager-ui (internal)
- **Backend API**: http://task-manager-api:8082/task-manager/api/v1
- **Keycloak**: http://keycloak:8080

#### Production (via Traefik)
- **Frontend**: https://your-domain.com
- **Backend API**: https://your-domain.com/task-manager/api/v1
- **Keycloak**: https://your-domain.com/auth
- **Traefik Dashboard**: https://traefik.your-domain.com

## Default Values

All environment variables have sensible defaults defined in the docker-compose.yml:

```yaml
environment:
  POSTGRESQL_PORT: ${POSTGRESQL_PORT:-5432}
  POSTGRESQL_DATABASE: ${POSTGRESQL_DATABASE:-taskmanager}
  SERVER_PORT: ${SERVER_PORT:-8082}
  DB_HIKARI_MAX_POOL_SIZE: ${DB_HIKARI_MAX_POOL_SIZE:-20}
```

The `:-` syntax means "use the environment variable if set, otherwise use the default value".

## Security Considerations

### Sensitive Variables

These variables contain sensitive information and should be kept secure:

- `POSTGRESQL_PASSWORD`
- `KC_DB_PASSWORD`
- `KEYCLOAK_ADMIN_PASSWORD`
- `ACME_EMAIL` (not sensitive, but personally identifiable)

### Best Practices

1. **Never commit `.env` file** - It's in `.gitignore`
2. **Use strong passwords** - Minimum 16 characters with mixed case, numbers, and symbols
3. **Rotate passwords regularly** - Especially in production
4. **Use secrets management** - For production, consider Docker Secrets or HashiCorp Vault
5. **Restrict file permissions** - `chmod 600 .env`

## Validation

### Check Configuration

```bash
# View active configuration (without sensitive values)
docker-compose config

# Check specific service environment
docker exec task-manager-api env | grep POSTGRESQL

# Test database connection
docker exec task-manager-api curl http://localhost:8082/task-manager/actuator/health
```

### Troubleshooting

**Issue: Application can't connect to database**
```bash
# Check environment variables are set correctly
docker exec task-manager-api env | grep -E "POSTGRESQL|DB_"

# Test database connection from container
docker exec task-manager-api pg_isready -h host.docker.internal -p 5432
```

**Issue: Keycloak configuration incorrect**
```bash
# Check Keycloak environment
docker exec keycloak env | grep -E "KC_|KEYCLOAK"

# Verify realm is accessible
curl http://localhost:8080/realms/task-manager
```

## Migration from Old Configuration

If upgrading from previous configuration:

### Old Format
```env
DB_NAME=taskmanager
DB_USERNAME=taskmanager
DB_PASSWORD=password
```

### New Format
```env
POSTGRESQL_DATABASE=taskmanager
POSTGRESQL_USER=taskmanager
POSTGRESQL_PASSWORD=password
```

### Update Script
```bash
# Convert old .env to new format
sed -i 's/DB_NAME=/POSTGRESQL_DATABASE=/' .env
sed -i 's/DB_USERNAME=/POSTGRESQL_USER=/' .env
sed -i 's/DB_PASSWORD=/POSTGRESQL_PASSWORD=/' .env
```

## Complete Example

See [.env.example](.env.example) for a complete configuration template with all variables and their default values.
