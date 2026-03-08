# Reverse Proxy Setup - Default: Traefik

This application uses **Traefik** as the default reverse proxy with automatic SSL certificate management. You can also use Nginx if you prefer traditional configuration.

## Current Setup

The default `docker-compose.yml` includes:
- **Traefik v2** - Automatic SSL with Let's Encrypt
- **Service Discovery** - Automatically routes to Docker containers
- **Built-in Dashboard** - Monitor services at https://traefik.your-domain.com

## Why Traefik is Default

| Feature | Nginx | Traefik |
|---------|-------|---------|
| **Configuration** | Config files | Docker labels |
| **SSL Management** | Manual (Certbot) | Automatic (Let's Encrypt) |
| **Service Discovery** | Manual | Automatic |
| **Reload on Changes** | Required | Not required |
| **Dashboard** | Third-party tools | Built-in |
| **Learning Curve** | Moderate to Steep | Moderate |
| **Resource Usage** | Very Light | Light |
| **Maturity** | Very Mature | Modern |
| **Best For** | Traditional setups | Docker-native setups |

## When to Choose Nginx

### Advantages
✅ **Battle-tested**: Used by millions of websites worldwide  
✅ **Extremely lightweight**: Minimal resource usage  
✅ **Fine-grained control**: Extensive configuration options  
✅ **Non-Docker workloads**: Works with any application  
✅ **Familiar**: Many sysadmins know Nginx well  
✅ **Advanced features**: Rate limiting, caching, etc.  

### Disadvantages
❌ Requires manual SSL certificate management  
❌ Requires configuration file editing  
❌ Requires reload on configuration changes  
❌ No service discovery  

### Choose Nginx if:
- You're familiar with traditional server setups
- You need fine-grained control over every aspect
- You're running mixed (Docker + non-Docker) workloads
- You prefer manual control over automation
- Your team is already experienced with Nginx

### Setup Files
- Configuration: [nginx-reverse-proxy.conf](nginx-reverse-proxy.conf)
- Docker Compose: [docker-compose.yml](docker-compose.yml)
- Guide: [EXTERNAL_DB_SETUP.md](EXTERNAL_DB_SETUP.md)

---

## When to Choose Traefik

### Advantages
✅ **Automatic SSL**: Let's Encrypt certificates managed automatically  
✅ **Service Discovery**: Automatically detects Docker services  
✅ **Zero downtime updates**: Dynamic configuration  
✅ **Built-in dashboard**: Monitor routes and services  
✅ **Docker-native**: Designed for containers  
✅ **Less maintenance**: Fewer manual steps  

### Disadvantages
❌ Docker-dependent for auto-discovery  
❌ Less mature than Nginx  
❌ Different mental model (labels vs files)  
❌ Slightly higher resource usage  

### Choose Traefik if:
- You're running a Docker-based infrastructure
- You want automatic SSL certificate management
- You prefer configuration through Docker labels
- You want a built-in monitoring dashboard
- You value automation over manual control
- You're building a microservices architecture

### Setup Files
- Docker Compose: [docker-compose.traefik.yml](docker-compose.traefik.yml)
- Guide: [TRAEFIK_SETUP.md](TRAEFIK_SETUP.md)

---

## Architecture Diagrams

### Nginx Architecture
```
Internet (Port 443)
    ↓
Nginx (Host)
├── SSL Termination (Let's Encrypt + Certbot)
├── /          → Angular App (Docker container port 3000)
├── /api/      → Spring Boot API (Docker container port 8080)
└── /auth/     → Keycloak (Docker container port 8080)

PostgreSQL (Host)
├── keycloak database
└── taskmanager database
```

### Traefik Architecture
```
Internet (Port 443)
    ↓
Traefik (Docker container)
├── SSL Termination (Let's Encrypt automatic)
├── /          → Angular App (Docker container)
├── /api/      → Spring Boot API (Docker container)
└── /auth/     → Keycloak (Docker container)
    └── Dashboard: traefik.domain.com

PostgreSQL (Host)
├── keycloak database
└── taskmanager database
```

---

## Feature Comparison Details

### SSL Certificate Management

**Nginx:**
- Requires Certbot installation on host
- Manual certificate request: `certbot --nginx`
- Auto-renewal via cron/systemd timer
- Certificates stored in `/etc/letsencrypt/`

**Traefik:**
- Built-in Let's Encrypt support
- Automatic certificate request on first HTTPS access
- Automatic renewal
- Certificates stored in Docker volume

### Configuration Changes

**Nginx:**
```bash
# Edit configuration file
sudo nano /etc/nginx/sites-available/task-manager

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

**Traefik:**
```yaml
# Edit docker-compose.traefik.yml labels
# or add labels to service

# Restart service
docker-compose -f docker-compose.traefik.yml up -d
```
No Traefik restart needed! Changes detected automatically.

### Adding a New Service

**Nginx:**
1. Edit nginx-reverse-proxy.conf
2. Add location block for new service
3. Test and reload Nginx
4. Update docker-compose.yml

**Traefik:**
1. Add service to docker-compose.traefik.yml
2. Add Traefik labels to service
3. Start service

Traefik automatically discovers the service!

### Monitoring

**Nginx:**
- Access logs: `/var/log/nginx/access.log`
- Error logs: `/var/log/nginx/error.log`
- Status module (if compiled)
- Third-party tools: Prometheus exporter, etc.

**Traefik:**
- Built-in dashboard: https://traefik.domain.com
- Real-time service status
- Router and middleware configuration
- Built-in metrics endpoint
- Prometheus integration

---

## Migration Between Options

### From Nginx to Traefik

1. Stop Nginx:
   ```bash
   sudo systemctl stop nginx
   ```

2. Update `.env` file:
   ```env
   DOMAIN=your-domain.com
   ACME_EMAIL=your-email@example.com
   ```

3. Start Traefik:
   ```bash
   docker-compose -f docker-compose.traefik.yml up -d
   ```

4. Verify services work

5. Optionally disable Nginx:
   ```bash
   sudo systemctl disable nginx
   ```

### From Traefik to Nginx

1. Stop Traefik services:
   ```bash
   docker-compose -f docker-compose.traefik.yml down
   ```

2. Setup Nginx following [EXTERNAL_DB_SETUP.md](EXTERNAL_DB_SETUP.md)

3. Update services to expose ports in docker-compose.yml (uncomment ports)

4. Start services:
   ```bash
   docker-compose up -d
   ```

---

## Cost Considerations

Both solutions are **free and open-source**.

### Nginx
- No licensing costs
- Lower hosting costs (slightly less memory)
- Potential higher operational costs (more manual work)

### Traefik
- No licensing costs
- Slightly higher memory usage (still very light)
- Lower operational costs (automation reduces manual work)

---

## Recommendations by Use Case

### Small Business Website
**Recommendation**: Traefik  
**Reason**: Automatic SSL and lower maintenance burden

### Enterprise Application
**Recommendation**: Either  
**Reason**: Choose based on existing infrastructure and team expertise

### Multi-Application Server
**Recommendation**: Nginx  
**Reason**: Better for mixed Docker/non-Docker workloads

### Microservices Architecture
**Recommendation**: Traefik  
**Reason**: Designed for dynamic service discovery

### Development Environment
**Recommendation**: Traefik  
**Reason**: Faster to set up, no manual certificate management

### High-Traffic Production
**Recommendation**: Either  
**Reason**: Both perform excellently; choose based on operational preferences

---

## Can I Use Both?

**Not simultaneously** on the same server for the same application, but:

- You can run Nginx on port 443 and Traefik on a different port
- You can use Nginx as the main proxy and Traefik for specific services
- You can run them on different servers

However, this adds complexity without significant benefits for most use cases.

---

## Getting Started

### Default Setup (Traefik)
```bash
sudo ./init-postgres.sh
cp .env.example .env
# Edit .env with DOMAIN and ACME_EMAIL
docker-compose up -d
# Done! SSL is automatic
```

### Alternative Setup (Nginx)
If you prefer Nginx, see [EXTERNAL_DB_SETUP.md](EXTERNAL_DB_SETUP.md) for manual setup:
```bash
sudo ./init-postgres.sh
cp .env.example .env
# Edit .env
docker-compose up -d
# Then configure Nginx on host with Certbot
```

---

## Need Help Deciding?

The default setup uses **Traefik** because:
- ✅ Automatic SSL certificate management
- ✅ Zero configuration after initial setup
- ✅ Built-in dashboard for monitoring
- ✅ Perfect for Docker-based deployments

**Use Nginx instead if:**
- You have existing Nginx expertise
- You need to proxy non-Docker services
- You prefer file-based configuration
- You have specific advanced Nginx requirements

**Recommendation**: Stick with the default Traefik setup unless you have specific reasons to use Nginx.

---

## Documentation Links

- **Quick Start**: [QUICKSTART.md](QUICKSTART.md)
- **Nginx Setup**: [EXTERNAL_DB_SETUP.md](EXTERNAL_DB_SETUP.md)
- **Traefik Setup**: [TRAEFIK_SETUP.md](TRAEFIK_SETUP.md)
- **Docker Basics**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
- **Keycloak Config**: [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md)
