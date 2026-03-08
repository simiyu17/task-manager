import { getEnvironmentConfig } from '../app/config/environment.config';

// Compile-time environment (production)
const compileTimeEnvironment = {
  production: true,
  apiBaseUrl: 'https://your-domain.com/task-manager/api/v1',
  authBaseUrl: 'https://your-domain.com/auth',
  authRealmName: 'task-manager',
  authClientId: 'task-manager-ui-client',
  apiTimeout: 30000
};

// Export environment with runtime override support (Docker deployment)
export const environment = getEnvironmentConfig(compileTimeEnvironment);
