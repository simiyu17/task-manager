# Environment Configuration Guide

This Angular application supports multiple environment configurations for different deployment scenarios.

## Available Environments

### 1. Development (default)
- **File**: `src/environments/environment.ts`
- **API Base URL**: `http://localhost:8080/api`
- **Usage**: Default environment for local development
- **Command**: `npm start` or `ng serve`

### 2. Production
- **File**: `src/environments/environment.prod.ts`
- **API Base URL**: `https://api.production.com/api`
- **Usage**: Production deployment
- **Commands**:
  ```bash
  npm run build                           # Build with production config
  ng build --configuration=production     # Alternative command
  ng serve --configuration=production     # Serve with production config
  ```

### 3. Staging
- **File**: `src/environments/environment.staging.ts`
- **API Base URL**: `https://api.staging.com/api`
- **Usage**: Staging/QA environment
- **Commands**:
  ```bash
  ng build --configuration=staging
  ng serve --configuration=staging
  ```

### 4. Docker
- **File**: `src/environments/environment.docker.ts`
- **API Base URL**: `http://task-manager-api:8080/api`
- **Usage**: Docker containerized deployment
- **Commands**:
  ```bash
  ng build --configuration=docker
  ng serve --configuration=docker
  ```

## Environment Configuration

Each environment file contains the following configuration:

```typescript
export const environment = {
  production: boolean,      // Production mode flag
  apiBaseUrl: string,       // API base URL
  apiTimeout: number        // API request timeout in milliseconds
};
```

## Using Environment Variables

### In Components/Services

```typescript
import { environment } from '../environments/environment';

// Access the API base URL
console.log('API URL:', environment.apiBaseUrl);

// Check if running in production
if (environment.production) {
  // Production-specific logic
}
```

### API Service Usage

The application includes an `ApiService` that automatically prepends the API base URL:

```typescript
import { TaskService } from './services/task/task.service';

constructor(private taskService: TaskService) {}

// This will call: http://localhost:8080/api/tasks
this.taskService.getAllTasks().subscribe(tasks => {
  console.log(tasks);
});
```

### HTTP Interceptor

The `ApiInterceptor` automatically adds the base URL to all HTTP requests:

```typescript
// Your request
this.http.get('tasks/123')

// Actual request sent
// http://localhost:8080/api/tasks/123
```

## Docker Configuration

### Building for Docker

```bash
# Build the application with Docker configuration
ng build --configuration=docker

# Or use npm script (you can add this to package.json)
npm run build:docker
```

### Dockerfile Example

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=docker

FROM nginx:alpine
COPY --from=build /app/dist/task-manager-ui /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose Example

```yaml
version: '3.8'
services:
  task-manager-ui:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "4200:80"
    environment:
      - NODE_ENV=production
    depends_on:
      - task-manager-api

  task-manager-api:
    image: task-manager-api:latest
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=postgresql://db:5432/taskmanager
```

## Runtime Environment Variables (Advanced)

For cases where you need to change the API URL at runtime without rebuilding:

### Create `assets/config.json`

```json
{
  "apiBaseUrl": "${API_BASE_URL}",
  "apiTimeout": 30000
}
```

### Update main.ts

```typescript
fetch('/assets/config.json')
  .then(response => response.json())
  .then(config => {
    // Override environment with runtime config
    Object.assign(environment, config);
    
    // Bootstrap the application
    bootstrapApplication(AppComponent, appConfig);
  });
```

### Docker Entrypoint Script

Create `docker-entrypoint.sh`:

```bash
#!/bin/sh
# Replace environment variables in config.json
envsubst < /usr/share/nginx/html/assets/config.json.template > /usr/share/nginx/html/assets/config.json
exec nginx -g 'daemon off;'
```

## Adding New Environments

1. Create a new environment file: `src/environments/environment.{name}.ts`
2. Update `angular.json` configurations:

```json
"configurations": {
  "your-env": {
    "fileReplacements": [
      {
        "replace": "src/environments/environment.ts",
        "with": "src/environments/environment.your-env.ts"
      }
    ]
  }
}
```

3. Add serve configuration:

```json
"serve": {
  "configurations": {
    "your-env": {
      "buildTarget": "task-manager-ui:build:your-env"
    }
  }
}
```

## Package.json Scripts

Add these scripts to your `package.json`:

```json
"scripts": {
  "start": "ng serve",
  "build": "ng build",
  "build:prod": "ng build --configuration=production",
  "build:staging": "ng build --configuration=staging",
  "build:docker": "ng build --configuration=docker",
  "serve:prod": "ng serve --configuration=production",
  "serve:staging": "ng serve --configuration=staging",
  "serve:docker": "ng serve --configuration=docker"
}
```

## Troubleshooting

### CORS Issues
If you encounter CORS errors during development, configure a proxy:

Create `proxy.conf.json`:
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

Update `angular.json`:
```json
"serve": {
  "options": {
    "proxyConfig": "proxy.conf.json"
  }
}
```

### Environment not loading
- Ensure the environment file exists
- Check `angular.json` for correct file replacement configuration
- Clear build cache: `rm -rf dist .angular`
- Rebuild: `ng build --configuration=your-env`

## Best Practices

1. **Never commit sensitive data**: Use environment variables for secrets
2. **Use .env files for local overrides**: Add `.env.local` to `.gitignore`
3. **Validate API URLs**: Check URLs don't have trailing slashes
4. **Use meaningful names**: Name environments based on deployment target
5. **Document changes**: Update this file when adding new environments
