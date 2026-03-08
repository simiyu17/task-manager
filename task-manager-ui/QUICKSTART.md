# Quick Start Guide - External PostgreSQL + Reverse Proxy Setup

This is a streamlined guide for deploying the Task Manager application with external PostgreSQL databases and a reverse proxy (Nginx or Traefik).

## Prerequisites Check

- [ ] Docker and Docker Compose installed
- [ ] PostgreSQL 14+ installed on host
- [ ] Nginx installed
- [ ] Domain name configured
- [ ] Ports 80, 443 available
- [ ] Root/sudo access

## Quick Setup - Common Steps

### Step 1: Initialize PostgreSQL Databases

Run the automated setup script:

```bash
sudo ./init-postgres.sh
```

This will:
- Create `keycloak` and `taskmanager` databases
- Create users with your chosen passwords
- Configure Docker access in `pg_hba.conf`
- Configure PostgreSQL to listen on all interfaces
- Restart PostgreSQL service

**Alternative Manual Setup:**
```bash
sudo -u postgres psql

CREATE DATABASE keycloak;
CREATE USER keycloak WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

CREATE DATABASE taskmanager;
CREATE USER taskmanager WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskmanager;
\q
```

Then edit `/etc/postgresql/[version]/main/pg_hba.conf`:
```
host    keycloak        keycloak        172.16.0.0/12           md5
host    taskmanager     taskmanager     172.16.0.0/12           md5
```

And `/etc/postgresql/[version]/main/postgresql.conf`:
```
listen_addresses = '*'
```

Restart: `sudo systemctl restart postgresql`

### Step 2: Configure Environment

```bash
cp .env.example .env
nano .env
```

Update these values:
```env
# Domain and email
DOMAIN=your-domain.com
ACME_EMAIL=your-email@example.com  # For Let's Encrypt

# Passwords
KC_DB_PASSWORD=your_keycloak_password
DB_PASSWORD=your_taskmanager_password
KEYCLOAK_ADMIN_PASSWORD=your_admin_password
```

### Step 3: Start Services

```bash
docker-compose up -d
```

Or use the management script:
```bash
./docker-manage.sh start
```

Verify:
```bash
docker-compose ps
./docker-manage.sh status
```

### Step 4: Verify Deployment

```bash
# Check status
docker-compose ps

# Test endpoints (SSL automatically configured by Traefik!)
curl https://your-domain.com
curl https://your-domain.com/task-manager/api/v1/actuator/health
curl https://your-domain.com/auth/health/ready
```

**That's it!** Traefik handles SSL automatically.

### Step 5: Configure Keycloak

Access: `https://your-domain.com/auth/admin`

1. **Create Realm**: `task-manager`

2. **Create Frontend Client**:
   - Client ID: `task-manager-ui-client`
   - Access Type: `public`
   - Valid Redirect URIs: `https://your-domain.com/*`
   - Web Origins: `https://your-domain.com`

3. **Create Backend Client**:
   - Client ID: `task-manager-api-client`
   - Access Type: `confidential`
   - Service Accounts Enabled: `ON`

4. **Create Users and Roles** as needed

## Verify Deployment

Test each service:

```bash
# Angular App
curl https://your-domain.com

# API (with Traefik or Nginx)
curl https://your-domain.com/task-manager/api/v1/actuator/health

# Keycloak (with Traefik or Nginx)
curl https://your-domain.com/auth/health/ready
```

Access Points:
- **Frontend**: https://your-domain.com
- **API**: https://your-domain.com/task-manager/api/v1/
- **Keycloak Admin**: https://your-domain.com/auth/admin/
- **Traefik Dashboard** (if using Traefik): https://traefik.your-domain.com

Check logs:
```bash
# Nginx setup
./docker-manage.sh logs

# Traefik setup
docker-compose -f docker-compose.traefik.yml logs -f
```

## Access Points

### All Setups
- **Frontend**: https://your-domain.com
- **API**: https://your-domain.com/task-manager/api/v1/
- **Keycloak Admin**: https://your-domain.com/auth/admin/
- **Traefik Dashboard**: https://traefik.your-domain.com (or http://server-ip:8081)

## Common Commands

```bash
# Start services
docker-compose up -d
./docker-manage.sh start

# Stop services
docker-compose down
./docker-manage.sh stop

# View logs
docker-compose logs -f [service-name]
./docker-manage.sh logs [service-name]

# Rebuild a service
docker-compose up -d --build task-manager-ui
./docker-manage.sh rebuild task-manager-ui

# Check status
docker-compose ps
./docker-manage.sh status

# Backup databases
./docker-manage.sh backup

# Access container shell
docker exec -it keycloak /bin/sh
./docker-manage.sh shell keycloak
```

## Troubleshooting

### Services won't start
```bash
# Check logs
docker-compose logs
./docker-manage.sh logs

# Check Docker
docker ps -a
```

### Database connection failed
```bash
# Test PostgreSQL
psql -h localhost -U taskmanager -d taskmanager

# Check if listening
sudo netstat -plnt | grep 5432

# Check pg_hba.conf has Docker network
sudo cat /etc/postgresql/*/main/pg_hba.conf | grep 172.16
```

### SSL certificate issues (Traefik)
```bash
# Check Traefik logs
docker logs traefik | grep -i acme

# Verify DNS is correct
dig your-domain.com

# Ensure ports 80 and 443 are accessible
sudo netstat -tlnp | grep -E '80|443'
```

## Security Checklist

- [ ] Changed all default passwords
- [ ] Firewall configured (UFW/iptables)
- [ ] SSL/HTTPS enabled
- [ ] PostgreSQL not exposed externally
- [ ] Docker services not exposed externally (only via nginx)
- [ ] Regular backups configured

## Firewall Configuration

```bash
sudo ufw enable
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw deny 5432/tcp   # Block PostgreSQL externally
sudo ufw deny 8080/tcp   # Block API externally
sudo ufw deny 8180/tcp   # Block Keycloak externally
sudo ufw status
```

## Backup Strategy

```bash
# Manual backup
./docker-manage.sh backup

# Automated backup (add to crontab)
0 2 * * * cd /path/to/task-manager-ui && ./docker-manage.sh backup
```

## Maintenance

### Update Services
```bash
# Pull latest images
docker-compose pull

# Rebuild and restart
docker-compose up -d --build
```

### Monitor Resources
```bash
# Docker stats
docker stats

# System resources
htop
df -h
```

### Log Rotation
Ensure Docker log rotation is configured in `/etc/docker/daemon.json`:
```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

## Need More Details?

- **Traefik Configuration**: [TRAEFIK_SETUP.md](TRAEFIK_SETUP.md)
- **Alternative Nginx Setup**: [EXTERNAL_DB_SETUP.md](EXTERNAL_DB_SETUP.md)
- **Docker Details**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
- **Keycloak Setup**: [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md)
- **Environments**: [ENVIRONMENT.md](ENVIRONMENT.md)

## Support

If you encounter issues:
1. Check service logs: `./docker-manage.sh logs`
2. Verify PostgreSQL is accessible
3. Check firewall rules
4. Verify nginx configuration
5. Check SSL certificate validity
