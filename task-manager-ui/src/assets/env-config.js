// Runtime environment configuration for Docker deployment
// This file is generated dynamically by generate-env-config.sh at container startup
// For local development, this placeholder does nothing (compile-time environment is used)

// Check if window.__env is not already defined
if (typeof window !== 'undefined' && !window.__env) {
  // Empty object for local development
  // In Docker, this will be replaced by actual configuration
  window.__env = {};
}
