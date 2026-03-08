# External Database & Nginx Reverse Proxy Setup Guide

This guide covers the setup for deploying the Task Manager application with external PostgreSQL databases and Nginx reverse proxy with Let's Encrypt SSL.

## Architecture Overview

```
Internet → Nginx (SSL/Let's Encrypt) → Docker Containers
                ├─→ Angular App (port 3000)
                ├─→ Spring Boot API (port 8082, context: /task-manager)
                └─→ Keycloak (port 8180)
                
Host PostgreSQL (port 5432)
  ├─→ taskmanager database (for Spring Boot)
  └─→ keycloak database (for Keycloak)
```

## Prerequisites

- Ubuntu/Debian Linux host
- Docker & Docker Compose installed
- PostgreSQL 14+ installed on host
- Root/sudo access
- Domain name pointing to your server

## Step 1: PostgreSQL Database Setup

### 1.1 Install PostgreSQL (if not already installed)

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

### 1.2 Create Databases and Users

```bash
# Switch to postgres user
sudo -u postgres psql

# Create Keycloak database and user
CREATE DATABASE keycloak;
CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak_password';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
\c keycloak
GRANT ALL ON SCHEMA public TO keycloak;

# Create Application database and user
CREATE DATABASE taskmanager;
CREATE USER taskmanager WITH ENCRYPTED PASSWORD 'taskmanager_password';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskmanager;
\c taskmanager
GRANT ALL ON SCHEMA public TO taskmanager;

# Exit psql
\q
```

### 1.3 Configure PostgreSQL for Docker Access

Edit PostgreSQL configuration to allow Docker containers to connect:

```bash
# Edit pg_hba.conf
sudo nano /etc/postgresql/[version]/main/pg_hba.conf
```

Add this line after the local connections:

```
# Docker containers access
host    keycloak        keycloak        172.16.0.0/12           md5
host    taskmanager     taskmanager     172.16.0.0/12           md5
```

Edit postgresql.conf to listen on all interfaces:

```bash
sudo nano /etc/postgresql/[version]/main/postgresql.conf
```

Change:
```
listen_addresses = '*'
```

Restart PostgreSQL:

```bash
sudo systemctl restart postgresql
```

### 1.4 Verify PostgreSQL Listening

```bash
sudo netstat -plnt | grep 5432
# Should show: 0.0.0.0:5432
```

## Step 2: Configure Environment Variables

Create `.env` file from the example:

```bash
cd /path/to/task-manager-ui
cp .env.example .env
nano .env
```

Update with your values:

```env
# Keycloak Configuration
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=YourStrongPassword123!
KC_HOSTNAME=your-domain.com
KC_DB_USERNAME=keycloak
KC_DB_PASSWORD=keycloak_password

# Application Database
DB_NAME=taskmanager
DB_USERNAME=taskmanager
DB_PASSWORD=taskmanager_password

# Spring Boot Application
SPRING_PROFILES_ACTIVE=docker
KEYCLOAK_REALM=task-manager
KEYCLOAK_RESOURCE_API=task-manager-api-client

# Ports
UI_INTERNAL_PORT=3000
API_PORT=8080
KEYCLOAK_PORT=8180
```

## Step 3: Nginx Installation and Configuration

### 3.1 Install Nginx

```bash
sudo apt update
sudo apt install nginx
```

### 3.2 Install Certbot for Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx
```

### 3.3 Configure Nginx

Copy the reverse proxy configuration:

```bash
sudo cp nginx-reverse-proxy.conf /etc/nginx/sites-available/task-manager
```

Edit the configuration:

```bash
sudo nano /etc/nginx/sites-available/task-manager
```

Replace `your-domain.com` with your actual domain throughout the file.

### 3.4 Enable the Site

```bash
# Create symlink
sudo ln -s /etc/nginx/sites-available/task-manager /etc/nginx/sites-enabled/

# Test nginx configuration
sudo nginx -t

# If test passes, reload nginx
sudo systemctl reload nginx
```

### 3.5 Obtain SSL Certificate

```bash
# Stop nginx temporarily for initial certificate
sudo systemctl stop nginx

# Obtain certificate
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# Start nginx
sudo systemctl start nginx
```

Or use the nginx plugin:

```bash
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

### 3.6 Setup Auto-Renewal

```bash
# Test renewal
sudo certbot renew --dry-run

# Certbot automatically sets up a cron job or systemd timer
# Verify it's active:
sudo systemctl status certbot.timer
```

## Step 4: Start Docker Services

### 4.1 Start the Services

```bash
cd /path/to/task-manager-ui

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### 4.2 Verify Services

```bash
# Check if services are running
docker ps

# Test Angular app internally
curl http://localhost:3000

# Test API
curl http://localhost:8080/actuator/health

# Test Keycloak
curl http://localhost:8180/health/ready
```

## Step 5: Keycloak Configuration

### 5.1 Access Keycloak Admin Console

Navigate to: `https://your-domain.com/auth/admin`

Login with credentials from your `.env` file (default: admin/admin)

### 5.2 Create Realm

1. Click "Add realm" (or "Create realm")
2. Name: `task-manager`
3. Enabled: ON
4. Click "Create"

### 5.3 Configure Frontend Client

**Clients → Create Client**

- Client ID: `task-manager-ui-client`
- Client Protocol: `openid-connect`
- Access Type: `public`

**Settings:**
- Standard Flow Enabled: ON
- Valid Redirect URIs:
  ```
  https://your-domain.com/*
  http://localhost:4200/*
  ```
- Web Origins:
  ```
  https://your-domain.com
  http://localhost:4200
  ```
- PKCE Code Challenge Method: `S256`

### 5.4 Configure Backend Client

**Clients → Create Client**

- Client ID: `task-manager-api-client`
- Client Protocol: `openid-connect`
- Access Type: `confidential`

**Settings:**
- Service Accounts Enabled: ON
- Authorization Enabled: ON
- Valid Redirect URIs: `https://your-domain.com/*`

Save and get the client secret from the "Credentials" tab.

### 5.5 Create Roles and Users

Create necessary roles and users as per your application requirements.

## Step 6: Update Frontend Configuration

If using production environment, update the Angular environment file:

```typescript
// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiBaseUrl: 'https://your-domain.com/api',
  keycloak: {
    url: 'https://your-domain.com/auth',
    realm: 'task-manager',
    clientId: 'task-manager-ui-client'
  }
};
```

Rebuild and restart the frontend:

```bash
docker-compose up -d --build task-manager-ui
```

## Step 7: Firewall Configuration

Configure firewall to allow only necessary ports:

```bash
# Enable UFW if not already enabled
sudo ufw enable

# Allow SSH
sudo ufw allow 22/tcp

# Allow HTTP and HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# PostgreSQL should NOT be exposed externally
# Only allow from localhost
sudo ufw deny 5432/tcp

# API and Keycloak should NOT be exposed externally
# They're accessed through nginx reverse proxy
sudo ufw deny 8080/tcp
sudo ufw deny 8180/tcp

# Check status
sudo ufw status
```

## Verification Checklist

- [ ] PostgreSQL databases created and accessible
- [ ] Docker containers running and healthy
- [ ] Nginx serving HTTPS traffic
- [ ] SSL certificate valid and auto-renewing
- [ ] Frontend accessible at https://your-domain.com
- [ ] API accessible at https://your-domain.com/api/*
- [ ] Keycloak accessible at https://your-domain.com/auth/*
- [ ] Keycloak realm and clients configured
- [ ] Application login working

## Maintenance Tasks

### Database Backups

```bash
# Backup taskmanager database
sudo -u postgres pg_dump taskmanager > taskmanager_backup_$(date +%Y%m%d).sql

# Backup keycloak database
sudo -u postgres pg_dump keycloak > keycloak_backup_$(date +%Y%m%d).sql
```

### Docker Container Updates

```bash
# Pull latest images
docker-compose pull

# Rebuild and restart
docker-compose up -d --build
```

### View Logs

```bash
# Docker logs
docker-compose logs -f

# Nginx logs
sudo tail -f /var/log/nginx/task-manager-access.log
sudo tail -f /var/log/nginx/task-manager-error.log

# PostgreSQL logs
sudo tail -f /var/log/postgresql/postgresql-[version]-main.log
```

## Troubleshooting

### Database Connection Issues

```bash
# Test PostgreSQL connection from host
psql -h localhost -U taskmanager -d taskmanager

# Test from Docker container
docker exec -it task-manager-api bash
curl http://host.docker.internal:5432
```

If connection fails, check:
1. PostgreSQL is listening: `sudo netstat -plnt | grep 5432`
2. pg_hba.conf has Docker network range
3. Firewall allows local connections

### SSL Certificate Issues

```bash
# Check certificate expiry
sudo certbot certificates

# Force renewal
sudo certbot renew --force-renewal

# Reload nginx after renewal
sudo systemctl reload nginx
```

### Container Communication Issues

```bash
# Check Docker network
docker network inspect task-manager-ui_task-manager-network

# Test internal connectivity
docker exec task-manager-ui ping task-manager-api
docker exec task-manager-api ping keycloak
```

## Security Best Practices

1. **Change Default Passwords**: Update all default passwords in `.env`
2. **Regular Updates**: Keep Docker images and system packages updated
3. **Monitor Logs**: Regularly check application and system logs
4. **Backup Strategy**: Implement automated database backups
5. **Firewall Rules**: Ensure only necessary ports are exposed
6. **SSL/TLS**: Keep certificates updated and use strong ciphers
7. **Database Access**: Restrict PostgreSQL access to localhost only
8. **Secrets Management**: Use Docker secrets or vault for sensitive data

## Support

For additional help, refer to:
- [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) - Docker deployment guide
- [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md) - Keycloak configuration details
- [ENVIRONMENT.md](ENVIRONMENT.md) - Environment configuration guide
