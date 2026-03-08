// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import { getEnvironmentConfig } from '../app/config/environment.config';

// Compile-time environment (development)
const compileTimeEnvironment = {
  production: false,
  apiBaseUrl: 'http://localhost:8082/task-manager/api/v1',
  authBaseUrl: 'http://localhost:8080',
  authRealmName: 'task-manager',
  authClientId: 'task-manager-ui-client',
  apiTimeout: 30000
};

// Export environment with runtime override support
export const environment = getEnvironmentConfig(compileTimeEnvironment);

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
