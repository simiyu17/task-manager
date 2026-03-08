# Traefik Reverse Proxy Setup Guide

This guide covers deploying the Task Manager application using Traefik as a reverse proxy with automatic SSL certificate management via Let's Encrypt.

## Why Traefik?

Traefik offers several advantages over traditional reverse proxies:

- **Automatic Service Discovery**: Automatically detects Docker containers
- **Built-in Let's Encrypt**: SSL certificates managed automatically
- **Dynamic Configuration**: No need to restart when adding/removing services
- **Docker Native**: Designed specifically for containerized applications
- **Dashboard**: Built-in monitoring dashboard
- **Zero Downtime**: Updates happen without restarting

## Architecture

```
Internet (HTTPS) → Traefik (Port 443)
                    ├─→ https://your-domain.com/ → Angular App
                    ├─→ https://your-domain.com/task-manager/api/ → Spring Boot API (port 8082)
                    ├─→ https://your-domain.com/auth/ → Keycloak
                    └─→ https://traefik.your-domain.com → Traefik Dashboard

External PostgreSQL (host:5432)
  ├─→ keycloak database
  └─→ taskmanager database
```

## Prerequisites

- Docker and Docker Compose installed
- PostgreSQL with databases configured (see `init-postgres.sh`)
- Domain name with DNS pointing to your server
- Ports 80, 443 available
- Valid email for Let's Encrypt certificates

## Quick Start

### 1. Initialize PostgreSQL (if not done)

```bash
sudo ./init-postgres.sh
```

### 2. Configure Environment

```bash
cp .env.example .env
nano .env
```

Required values:
```env
# Domain
DOMAIN=your-domain.com

# Email for Let's Encrypt
ACME_EMAIL=your-email@example.com

# Database passwords
KC_DB_PASSWORD=your_keycloak_password
DB_PASSWORD=your_taskmanager_password

# Keycloak admin
KEYCLOAK_ADMIN_PASSWORD=your_admin_password
```

### 3. Start Services with Traefik

```bash
docker-compose up -d
```

### 4. Verify Services

```bash
# Check all containers are running
docker-compose ps

# Check Traefik logs
docker logs traefik -f

# Test endpoints
curl https://your-domain.com
curl https://your-domain.com/task-manager/api/v1/actuator/health
curl https://your-domain.com/auth/health/ready
```

### 5. Access Services

- **Frontend**: https://your-domain.com
- **API**: https://your-domain.com/task-manager/api/v1/
- **Keycloak**: https://your-domain.com/auth/
- **Traefik Dashboard**: https://traefik.your-domain.com (or http://your-server:8081)

## Traefik Configuration Explained

### Labels Overview

Traefik uses Docker labels for configuration. Each service has labels that define:

1. **Enable Traefik**: `traefik.enable=true`
2. **Router Rule**: Which requests go to this service
3. **Entrypoint**: HTTP (80) or HTTPS (443)
4. **TLS**: Certificate resolver for SSL
5. **Service Port**: Internal container port
6. **Middlewares**: CORS, auth, headers, etc.

### Example Service Configuration

```yaml
labels:
  - "traefik.enable=true"
  - "traefik.http.routers.api.rule=Host(`example.com`) && PathPrefix(`/api`)"
  - "traefik.http.routers.api.entrypoints=websecure"
  - "traefik.http.routers.api.tls.certresolver=letsencrypt"
  - "traefik.http.services.api.loadbalancer.server.port=8080"
```

## Dashboard Access

### Option 1: Subdomain (Recommended)

Access at: https://traefik.your-domain.com

Default credentials:
- Username: `admin`
- Password: `admin`

**Change the password!** Generate new credentials:
```bash
# Install htpasswd
sudo apt install apache2-utils

# Generate new password (replace 'newpassword')
htpasswd -nb admin newpassword

# Output will look like:
# admin:$apr1$xyz...

# Copy the output and update docker-compose.traefik.yml
# In the traefik service labels, find:
# - "traefik.http.middlewares.traefik-auth.basicauth.users=..."
# Replace with your generated hash (escape $ with $$)
```

### Option 2: Direct Port Access

Access at: http://your-server-ip:8081

For production, comment out port 8081 in the traefik service.

## SSL Certificates

Traefik automatically obtains and renews Let's Encrypt certificates.

### Verify Certificates

```bash
# Check Traefik logs for certificate challenges
docker logs traefik | grep -i acme

# Inspect certificate storage
docker exec traefik ls -la /letsencrypt/
```

### Certificate Storage

Certificates are stored in the Docker volume `traefik-certificates`.

### Force Certificate Renewal

```bash
# Remove certificate storage
docker-compose down
docker volume rm task-manager-ui_traefik-certificates

# Restart services (new certificates will be obtained)
docker-compose up -d
```

## Traefik vs Nginx Comparison

| Feature | Traefik | Nginx |
|---------|---------|-------|
| Configuration | Docker labels | Config files |
| SSL Management | Automatic | Manual with Certbot |
| Service Discovery | Automatic | Manual |
| Reload Required | No | Yes |
| Dashboard | Built-in | Separate tools |
| Learning Curve | Moderate | Steeper |
| Resource Usage | Light | Very Light |
| Maturity | Modern | Mature |

## Common Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f

# Restart specific service
docker-compose restart task-manager-api

# Rebuild service
docker-compose up -d --build task-manager-ui

# Check status
docker-compose ps
```

## Troubleshooting

### Certificates Not Working

1. **Check DNS**: Ensure domain points to your server
   ```bash
   dig your-domain.com
   nslookup your-domain.com
   ```

2. **Check Firewall**: Ports 80 and 443 must be open
   ```bash
   sudo ufw status
   sudo netstat -tlnp | grep -E '80|443'
   ```

3. **Check Traefik Logs**:
   ```bash
   docker logs traefik | grep -i error
   docker logs traefik | grep -i acme
   ```

4. **Verify Email**: ACME_EMAIL must be valid in `.env`

### Service Not Accessible

1. **Check container is running**:
   ```bash
   docker ps | grep task-manager
   ```

2. **Check Traefik routing**:
   ```bash
   # Visit Traefik dashboard
   # Check HTTP -> Routers section
   ```

3. **Test internal connectivity**:
   ```bash
   docker exec traefik ping task-manager-ui
   docker exec traefik wget -O- http://task-manager-ui:80/health
   ```

### Dashboard Not Accessible

1. **For subdomain access**: Ensure DNS is configured
2. **For port access**: Check port 8081 is not blocked
3. **Check auth credentials**: Verify basicauth.users is correct

### Database Connection Issues

Same as standard setup - see [EXTERNAL_DB_SETUP.md](EXTERNAL_DB_SETUP.md)

## Advanced Configuration

### Add Additional Domain/Subdomain

Add to the service labels:
```yaml
labels:
  - "traefik.http.routers.frontend.rule=Host(`app.example.com`) || Host(`example.com`)"
```

### Add IP Whitelist

```yaml
labels:
  - "traefik.http.middlewares.ip-whitelist.ipwhitelist.sourcerange=1.2.3.4/32,5.6.7.0/24"
  - "traefik.http.routers.api.middlewares=ip-whitelist"
```

### Add Rate Limiting

```yaml
labels:
  - "traefik.http.middlewares.rate-limit.ratelimit.average=100"
  - "traefik.http.middlewares.rate-limit.ratelimit.burst=50"
  - "traefik.http.routers.api.middlewares=rate-limit"
```

### Add Request Logging

Update docker-compose.yml:
```yaml
command:
  - "--log.level=DEBUG"
  - "--accesslog=true"
  - "--accesslog.filepath=/var/log/traefik/access.log"
```

## Security Best Practices

1. **Change Dashboard Password**: Use strong password with htpasswd
2. **Disable Dashboard Port**: Comment out port 8081 in production
3. **Use Strong Database Passwords**: Update `.env` with strong passwords
4. **Enable Firewall**: Only allow 80, 443, and SSH
5. **Keep Traefik Updated**: Regularly update Traefik image
6. **Monitor Logs**: Regularly check for suspicious activity
7. **Backup Certificates**: Backup `traefik-certificates` volume

## Firewall Configuration

```bash
sudo ufw enable
sudo ufw allow 22/tcp     # SSH
sudo ufw allow 80/tcp     # HTTP
sudo ufw allow 443/tcp    # HTTPS
sudo ufw deny 5432/tcp    # Block PostgreSQL
sudo ufw deny 8080/tcp    # Block API direct access
sudo ufw deny 8081/tcp    # Block Traefik dashboard port (optional)
sudo ufw status
```

## Monitoring

### View Metrics

Access Traefik dashboard at https://traefik.your-domain.com

Check:
- HTTP Routers: See all configured routes
- HTTP Services: See backend services status
- HTTP Middlewares: See applied middlewares

### Prometheus Integration

Traefik can export metrics for Prometheus. Add to Traefik command:
```yaml
command:
  - "--metrics.prometheus=true"
  - "--metrics.prometheus.entrypoint=metrics"
  - "--entrypoints.metrics.address=:8082"
```

## Migration from Nginx

If switching from Nginx setup:

1. Stop Nginx:
   ```bash
   sudo systemctl stop nginx
   sudo systemctl disable nginx
   ```

2. Update `.env` with domain and email

3. Start Traefik setup:
   ```bash
   docker-compose up -d
   ```

4. Verify all services work

5. Remove Nginx configuration:
   ```bash
   sudo rm /etc/nginx/sites-enabled/task-manager
   ```

## Production Checklist

- [ ] PostgreSQL databases created and secured
- [ ] `.env` file configured with strong passwords
- [ ] Domain DNS pointing to server
- [ ] Ports 80 and 443 open in firewall
- [ ] Valid email configured for Let's Encrypt
- [ ] SSL certificates obtained successfully
- [ ] Dashboard password changed
- [ ] Dashboard port 8081 disabled/protected
- [ ] All services healthy and accessible
- [ ] Keycloak realm and clients configured
- [ ] Regular backups configured
- [ ] Monitoring enabled

## Support

For additional help:
- [Nginx Alternative](EXTERNAL_DB_SETUP.md)
- [Keycloak Setup](KEYCLOAK_SETUP.md)
- [Quick Start](QUICKSTART.md)

## Resources

- [Traefik Documentation](https://doc.traefik.io/traefik/)
- [Let's Encrypt](https://letsencrypt.org/)
- [Docker Labels Reference](https://doc.traefik.io/traefik/routing/providers/docker/)
