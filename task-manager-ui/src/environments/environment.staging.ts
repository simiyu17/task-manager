import { getEnvironmentConfig } from '../app/config/environment.config';

// Compile-time environment (staging)
const compileTimeEnvironment = {
  production: false,
  apiBaseUrl: 'https://staging.your-domain.com/task-manager/api/v1',
  authBaseUrl: 'https://staging.your-domain.com/auth',
  authRealmName: 'task-manager',
  authClientId: 'task-manager-ui-client',
  apiTimeout: 30000
};

// Export environment with runtime override support
export const environment = getEnvironmentConfig(compileTimeEnvironment);
