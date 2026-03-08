#!/bin/sh

# Generate env-config.js from environment variables at container startup
# This allows runtime configuration of Angular app in Docker

cat <<EOF > /usr/share/nginx/html/assets/env-config.js
(function(window) {
  window.__env = window.__env || {};
  
  // Environment configuration
  window.__env.production = ${ANGULAR_PRODUCTION:-false};
  window.__env.apiBaseUrl = '${ANGULAR_API_BASE_URL:-http://localhost:8082/task-manager/api/v1}';
  window.__env.authBaseUrl = '${ANGULAR_AUTH_BASE_URL:-http://localhost:8080}';
  window.__env.authRealmName = '${ANGULAR_AUTH_REALM_NAME:-task-manager}';
  window.__env.authClientId = '${ANGULAR_AUTH_CLIENT_ID:-task-manager-ui-client}';
  window.__env.apiTimeout = ${ANGULAR_API_TIMEOUT:-30000};
  
  // Debug mode
  window.__env.enableDebug = ${ANGULAR_ENABLE_DEBUG:-false};
}(this));
EOF

echo "Environment configuration generated at /usr/share/nginx/html/assets/env-config.js"
cat /usr/share/nginx/html/assets/env-config.js

# Start nginx
exec "$@"
