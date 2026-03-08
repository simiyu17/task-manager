# Keycloak Configuration Guide

This guide provides the necessary Keycloak configuration for Firefox browser compatibility.

## Browser Compatibility Issue - Firefox

Firefox's Enhanced Tracking Protection blocks third-party cookies and iframes by default, which can prevent Keycloak's SSO mechanisms from working correctly. This application has been configured to work around these limitations.

## Application Configuration

The following Keycloak init options are configured for Firefox compatibility:

```typescript
{
  onLoad: 'check-sso',
  checkLoginIframe: false,  // Critical for Firefox
  pkceMethod: 'S256',       // Improved security and compatibility
  flow: 'standard',         // Explicit OAuth2 standard flow
  responseMode: 'query'     // Query mode for path-based routing
}
```

**Note**: This application uses PathLocationStrategy (standard URL paths without hash `#`), not HashLocationStrategy.

## Keycloak Server Configuration Requirements

### 1. Client Configuration

Configure your Keycloak client (`task-manager-ui-client`) with these settings:

#### Access Settings:
- **Client Protocol**: `openid-connect`
- **Access Type**: `public`
- **Standard Flow Enabled**: `ON`
- **Implicit Flow Enabled**: `OFF` (recommended)
- **Direct Access Grants Enabled**: `OFF` (recommended)

#### Valid Redirect URIs:
Configure these patterns for path-based routing:
```
http://localhost:4200/*
https://your-domain.com/*
```

**Important**: Since the application uses PathLocationStrategy, no hash (`#`) is needed in redirect URIs.

#### Web Origins:
Add your application origins:
```
http://localhost:4200
https://your-domain.com
```

### 2. PKCE Configuration

PKCE (Proof Key for Code Exchange) is enabled in the application for improved security.

In Keycloak Admin Console:
1. Go to your Realm → Clients → `task-manager-ui-client`
2. Under **Advanced Settings**:
   - **Proof Key for Code Exchange Code Challenge Method**: `S256`

### 3. CORS Configuration

Ensure CORS is properly configured:
1. In your client settings, add `Web Origins` (see above)
2. In Realm Settings → Security Defenses → Headers:
   - Verify that CORS is not blocking your domain

### 4. Session Configuration (Optional)

For better Firefox compatibility, you may want to adjust:
1. Realm Settings → Sessions
   - **SSO Session Idle**: Adjust based on your needs
   - **SSO Session Max**: Adjust based on your needs

## Testing the Configuration

### In Chrome:
1. Navigate to `http://localhost:4200`
2. You should be redirected to Keycloak login
3. After login, redirected back to dashboard

### In Firefox:
1. Open Developer Console (F12)
2. Navigate to `http://localhost:4200`
3. Check for any CORS or cookie errors
4. If you see cookie warnings, they should not prevent login
5. You should be redirected to Keycloak login
6. After login, redirected back to dashboard

## Troubleshooting

### Issue: Redirect loop in Firefox
**Solution**: 
- Verify `checkLoginIframe: false` in app.config.ts
- Check that Valid Redirect URIs use wildcard patterns: `http://localhost:4200/*`
- Ensure no hash (`#`) patterns are in redirect URIs since the app uses PathLocationStrategy

### Issue: CORS errors
**Solution**:
- Add your domain to Web Origins in Keycloak client
- Ensure Keycloak server allows your origin

### Issue: "Invalid redirect_uri"
**Solution**:
- Add wildcard redirect URI patterns to Keycloak client
- Pattern: `http://localhost:4200/*` (no hash since we use PathLocationStrategy)
- Ensure the pattern matches your actual domain

### Issue: Still not working in Firefox
**Solution**:
1. Disable Enhanced Tracking Protection for your Keycloak domain:
   - Click the shield icon in Firefox address bar
   - Toggle "Enhanced Tracking Protection" off for Keycloak
2. Clear browser cookies and localStorage
3. Restart browser

### Issue: Working in incognito/private mode but not regular mode
**Solution**:
- Clear cookies and site data for both your app and Keycloak domains
- Firefox: Settings → Privacy & Security → Cookies and Site Data → Clear Data

## Environment Configuration

Update your environment files with correct Keycloak settings:

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8082/task-manager/api/v1',
  authBaseUrl: 'http://localhost:8080',
  authRealmName: 'task-manager',
  authClientId: 'task-manager-ui-client',
  apiTimeout: 30000
};
```

## Additional Notes

### Server Configuration for PathLocationStrategy

Since the application uses PathLocationStrategy (standard URLs without `#`), your web server must be configured to serve `index.html` for all routes. This ensures that deep linking and page refreshes work correctly.

#### Development Server
The Angular development server (`ng serve`) handles this automatically.

#### Production Deployment

**Apache (.htaccess):**
```apache
<IfModule mod_rewrite.c>
  RewriteEngine On
  RewriteBase /
  RewriteRule ^index\.html$ - [L]
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteRule . /index.html [L]
</IfModule>
```

**Nginx:**
```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

**Node.js/Express:**
```javascript
app.get('/*', function(req, res) {
  res.sendFile(path.join(__dirname, 'index.html'));
});
```

### Why checkLoginIframe is disabled:
Firefox's Enhanced Tracking Protection considers the iframe method for checking SSO status as tracking and blocks it. Disabling this feature allows proper authentication flow.

### PKCE (S256):
PKCE adds an additional security layer and is recommended for public clients (like SPAs). It works by:
1. Generating a code_verifier
2. Creating a code_challenge from the verifier
3. Sending the challenge during authorization
4. Sending the verifier during token exchange
5. Keycloak verifies they match

This prevents authorization code interception attacks and is especially important for public clients.

## References

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Firefox Enhanced Tracking Protection](https://support.mozilla.org/en-US/kb/enhanced-tracking-protection-firefox-desktop)
- [OAuth 2.0 PKCE](https://oauth.net/2/pkce/)
