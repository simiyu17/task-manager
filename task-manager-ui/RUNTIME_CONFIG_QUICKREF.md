# Angular Runtime Configuration - Quick Reference

## Overview
Angular app reads configuration at runtime from `window.__env` object in Docker deployments, falling back to compile-time environment for local development.

## Key Files

| File | Purpose |
|------|---------|
| `src/index.html` | Loads `env-config.js` before Angular bootstraps |
| `src/env-config.js` | Placeholder for local dev (replaced in Docker) |
| `src/app/config/environment.config.ts` | TypeScript helper for runtime config |
| `src/environments/*.ts` | Environment files using `getEnvironmentConfig()` |
| `generate-env-config.sh` | Generates runtime config in Docker |
| `docker-compose.yml` | Sets `ANGULAR_*` environment variables |

## How It Works

```
┌─────────────────────────────────────────────────────────────┐
│  1. Docker Container Starts                                  │
│     └─> Nginx entrypoint executes generate-env-config.sh    │
│         └─> Reads ANGULAR_* environment variables           │
│             └─> Generates env-config.js with window.__env   │
└─────────────────────────────────────────────────────────────┘
                            ⬇
┌─────────────────────────────────────────────────────────────┐
│  2. Browser Loads Application                                │
│     └─> index.html loads env-config.js first                │
│         └─> Sets window.__env object                        │
│             └─> Angular app bootstraps                      │
│                 └─> getEnvironmentConfig() reads __env      │
└─────────────────────────────────────────────────────────────┘
```

## Environment Variables (docker-compose.yml)

```yaml
task-manager-ui:
  environment:
    - ANGULAR_PRODUCTION=false
    - ANGULAR_API_BASE_URL=https://${DOMAIN}/task-manager/api/v1
    - ANGULAR_AUTH_BASE_URL=https://${DOMAIN}/auth
    - ANGULAR_AUTH_REALM_NAME=${KEYCLOAK_REALM:-task-manager}
    - ANGULAR_AUTH_CLIENT_ID=${KEYCLOAK_RESOURCE_UI:-task-manager-ui-client}
    - ANGULAR_API_TIMEOUT=30000
```

## TypeScript Usage

### Environment File Structure
```typescript
import { getEnvironmentConfig } from '../app/config/environment.config';

const compileTimeEnvironment = {
  production: false,
  apiBaseUrl: 'http://localhost:8082/task-manager/api/v1',
  authBaseUrl: 'http://localhost:8080',
  authRealmName: 'task-manager',
  authClientId: 'task-manager-ui-client',
  apiTimeout: 30000
};

// Merges window.__env (Docker) with compile-time config (local dev)
export const environment = getEnvironmentConfig(compileTimeEnvironment);
```

### Using Environment in Services
```typescript
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiBaseUrl;
  private timeout = environment.apiTimeout;
  
  constructor(private http: HttpClient) {}
  
  getTasks() {
    return this.http.get(`${this.apiUrl}/tasks`, { 
      timeout: this.timeout 
    });
  }
}
```

## Commands

### Local Development
```bash
npm start
# Uses compile-time environment.ts
# env-config.js is ignored (empty placeholder)
```

### Docker Build & Run
```bash
# Build image
docker-compose build task-manager-ui

# Start container
docker-compose up -d task-manager-ui

# Verify generated config
docker-compose exec task-manager-ui cat /usr/share/nginx/html/env-config.js

# View logs
docker-compose logs -f task-manager-ui
```

### Testing Configuration
```bash
# Browser console
console.log(window.__env);

# Expected output in Docker:
{
  production: false,
  apiBaseUrl: "https://your-domain.com/task-manager/api/v1",
  authBaseUrl: "https://your-domain.com/auth",
  authRealmName: "task-manager",
  authClientId: "task-manager-ui-client",
  apiTimeout: 30000
}

# Expected output locally:
{} // Empty object, uses compile-time config
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `window.__env` is undefined | Check if `env-config.js` is loaded in `index.html` |
| Wrong configuration in Docker | Verify `ANGULAR_*` variables in `docker-compose.yml` |
| TypeScript errors | Check import paths in environment files |
| Script not executing | Verify permissions: `chmod +x generate-env-config.sh` |
| Config not updating | Restart container: `docker-compose restart task-manager-ui` |

## Benefits

✅ **Single Docker image** - build once, configure at runtime  
✅ **No rebuild needed** - change config by updating docker-compose.yml  
✅ **Type-safe** - TypeScript interfaces ensure correctness  
✅ **Dev-friendly** - local development works without Docker  
✅ **Production-ready** - follows Angular best practices  

## Related Documentation

- **Full Guide**: [RUNTIME_CONFIG.md](RUNTIME_CONFIG.md)
- **Quick Start**: [QUICKSTART.md](QUICKSTART.md)
- **Environment Variables**: [ENV_VARIABLES.md](ENV_VARIABLES.md)
- **Docker Deployment**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
