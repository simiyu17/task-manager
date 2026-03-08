# Runtime Configuration for Angular in Docker

This document explains how the Angular application reads runtime environment variables when deployed in Docker containers.

## Problem Statement

Angular applications are built into static files during `ng build`. This means environment variables cannot be injected at runtime like traditional server-side applications. When deploying with Docker, we need a way to configure the application without rebuilding the image.

## Solution Architecture

We implement a three-part solution:

### 1. Runtime Configuration Script (`generate-env-config.sh`)

Located in `/docker-entrypoint.d/40-generate-env-config.sh` inside the Docker container, this script:
- Executes automatically when the Nginx container starts
- Reads environment variables from Docker
- Generates `/usr/share/nginx/html/env-config.js` with JavaScript configuration
- Runs before Nginx starts serving requests

**Generated file structure:**
```javascript
window.__env = {
  production: false,
  apiBaseUrl: 'https://domain.com/task-manager/api/v1',
  authBaseUrl: 'https://domain.com/auth',
  authRealmName: 'task-manager',
  authClientId: 'task-manager-ui-client',
  apiTimeout: 30000
}
```

### 2. TypeScript Configuration Helper (`environment.config.ts`)

Located at `src/app/config/environment.config.ts`, this module:
- Defines TypeScript interface for environment configuration
- Extends Window interface to include `__env` property
- Provides `getEnvironmentConfig()` helper function
- Falls back to compile-time config if runtime config unavailable

**Usage pattern:**
```typescript
import { getEnvironmentConfig } from '../app/config/environment.config';

const compileTimeEnvironment = {
  production: false,
  apiBaseUrl: 'http://localhost:8082/task-manager/api/v1',
  // ... other config
};

export const environment = getEnvironmentConfig(compileTimeEnvironment);
```

### 3. HTML Script Injection (`index.html`)

The `env-config.js` script is loaded before Angular bootstraps:
```html
<head>
  <!-- ... other meta tags ... -->
  <script src="env-config.js"></script>
</head>
```

This ensures `window.__env` is available when Angular initializes.

## Configuration Flow

### Development Workflow
1. Developer runs `ng serve`
2. `env-config.js` contains empty placeholder
3. Angular uses compile-time environment from `environment.ts`
4. No Docker container involved

### Docker Deployment Workflow
1. Build Docker image: `docker-compose build task-manager-ui`
2. Start container with environment variables from `docker-compose.yml`
3. Nginx container starts
4. `/docker-entrypoint.d/40-generate-env-config.sh` executes
5. Generates `env-config.js` from environment variables
6. Nginx serves Angular application
7. Browser loads `index.html`
8. `env-config.js` loads, setting `window.__env`
9. Angular application bootstraps
10. `getEnvironmentConfig()` reads from `window.__env`
11. Application uses Docker environment configuration

## Environment Variables

Set these in `docker-compose.yml` for the `task-manager-ui` service:

| Variable | Description | Example |
|----------|-------------|---------|
| `ANGULAR_PRODUCTION` | Production mode flag | `false` |
| `ANGULAR_API_BASE_URL` | Backend API URL | `https://domain.com/task-manager/api/v1` |
| `ANGULAR_AUTH_BASE_URL` | Keycloak authentication URL | `https://domain.com/auth` |
| `ANGULAR_AUTH_REALM_NAME` | Keycloak realm name | `task-manager` |
| `ANGULAR_AUTH_CLIENT_ID` | Keycloak client ID | `task-manager-ui-client` |
| `ANGULAR_API_TIMEOUT` | API request timeout (ms) | `30000` |

## File Structure

```
task-manager-ui/
├── src/
│   ├── index.html                              # Loads env-config.js
│   ├── env-config.js                           # Placeholder (replaced in Docker)
│   ├── app/
│   │   └── config/
│   │       └── environment.config.ts           # Runtime config helper
│   └── environments/
│       ├── environment.ts                      # Development (uses helper)
│       ├── environment.prod.ts                 # Production (uses helper)
│       ├── environment.docker.ts               # Docker (uses helper)
│       └── environment.staging.ts              # Staging (uses helper)
├── Dockerfile                                   # Copies generate-env-config.sh
├── generate-env-config.sh                      # Runtime config generator
└── docker-compose.yml                          # Sets ANGULAR_* env vars
```

## Benefits

✅ **Single Docker Image**: Build once, deploy anywhere with different configurations
✅ **No Rebuild Required**: Change configuration by updating docker-compose.yml
✅ **Development Friendly**: Local development works without Docker
✅ **Type Safety**: TypeScript interfaces ensure type correctness
✅ **Fallback Support**: Gracefully falls back to compile-time config
✅ **Production Ready**: Follows Angular and Docker best practices

## Testing

### Local Development
```bash
npm start
# Uses compile-time environment from environment.ts
```

### Docker Deployment
```bash
# Build image
docker-compose build task-manager-ui

# Start with custom configuration
docker-compose up -d task-manager-ui

# Verify env-config.js was generated
docker-compose exec task-manager-ui cat /usr/share/nginx/html/env-config.js

# Check browser console
# window.__env should contain Docker environment variables
```

### Browser Console Verification
```javascript
// Open browser console and check:
console.log(window.__env);
// Should show:
// {
//   production: false,
//   apiBaseUrl: "https://your-domain.com/task-manager/api/v1",
//   ...
// }
```

## Troubleshooting

### Issue: Angular uses wrong configuration
**Solution**: Check browser console for `window.__env`. If empty, verify:
1. `env-config.js` is loaded in index.html
2. `generate-env-config.sh` has execute permissions in Dockerfile
3. Environment variables are set in docker-compose.yml

### Issue: Script not executing in Docker
**Solution**: Verify script location and permissions:
```bash
docker-compose exec task-manager-ui ls -la /docker-entrypoint.d/
# Should show: -rwxr-xr-x 40-generate-env-config.sh
```

### Issue: TypeScript compilation errors
**Solution**: Ensure `environment.config.ts` import paths are correct:
```typescript
// ✅ Correct
import { getEnvironmentConfig } from '../app/config/environment.config';

// ❌ Incorrect
import { getEnvironmentConfig } from './config/environment.config';
```

## References

- [Angular Environment Configuration](https://angular.io/guide/build#configuring-application-environments)
- [Nginx Docker Official Image](https://hub.docker.com/_/nginx)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Nginx Docker Entrypoint](https://github.com/nginxinc/docker-nginx/blob/master/entrypoint/docker-entrypoint.sh)
