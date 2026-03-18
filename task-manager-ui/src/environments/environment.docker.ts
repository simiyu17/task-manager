import { getEnvironmentConfig } from '../app/config/environment.config';

// Compile-time environment (Docker internal network)
const compileTimeEnvironment = {
  production: true,
  apiBaseUrl: 'http://task-manager-api:8082/task-manager/api/v1',
  authBaseUrl: 'http://keycloak:8080',
  authRealmName: 'task-manager',
  authClientId: 'task-manager-ui-client',
  apiTimeout: 30000
};

// Export environment with runtime override support
export const environment = getEnvironmentConfig(compileTimeEnvironment);
