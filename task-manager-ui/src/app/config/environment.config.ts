// TypeScript interface for runtime environment configuration
// This allows Angular to read configuration from window.__env (loaded from env-config.js)

export interface EnvironmentConfig {
  production: boolean;
  apiBaseUrl: string;
  authBaseUrl: string;
  authRealmName: string;
  authClientId: string;
  apiTimeout: number;
  enableDebug?: boolean;
}

// Declare global window interface extension
declare global {
  interface Window {
    __env: EnvironmentConfig;
  }
}

// Helper function to get environment configuration
// Falls back to compile-time environment if window.__env is not available
export function getEnvironmentConfig(compileTimeEnv: EnvironmentConfig): EnvironmentConfig {
  // Check if runtime config is available (Docker deployment)
  if (typeof window !== 'undefined' && window.__env) {
    return {
      production: window.__env.production ?? compileTimeEnv.production,
      apiBaseUrl: window.__env.apiBaseUrl ?? compileTimeEnv.apiBaseUrl,
      authBaseUrl: window.__env.authBaseUrl ?? compileTimeEnv.authBaseUrl,
      authRealmName: window.__env.authRealmName ?? compileTimeEnv.authRealmName,
      authClientId: window.__env.authClientId ?? compileTimeEnv.authClientId,
      apiTimeout: window.__env.apiTimeout ?? compileTimeEnv.apiTimeout,
      enableDebug: window.__env.enableDebug ?? false
    };
  }
  
  // Fall back to compile-time environment (local development)
  return compileTimeEnv;
}
