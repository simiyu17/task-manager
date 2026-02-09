import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  provideRouter,
  withEnabledBlockingInitialNavigation,
  withHashLocation,
  withInMemoryScrolling,
  withRouterConfig,
  withViewTransitions
} from '@angular/router';
import { IconSetService } from '@coreui/icons-angular';
import { routes } from './app.routes';
import { apiInterceptor } from './interceptors/api.interceptor';
import { 
  provideKeycloak, 
  createInterceptorCondition, 
  IncludeBearerTokenCondition, 
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  UserActivityService,
  withAutoRefreshToken,
  includeBearerTokenInterceptor,
  AutoRefreshTokenService
} from 'keycloak-angular';
import { environment } from '../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes,
      withRouterConfig({
        onSameUrlNavigation: 'reload'
      }),
      withInMemoryScrolling({
        scrollPositionRestoration: 'top',
        anchorScrolling: 'enabled'
      }),
      withEnabledBlockingInitialNavigation(),
      withViewTransitions(),
      withHashLocation(),
    ),
    IconSetService,
    provideAnimationsAsync(),
    // 1. Configure Keycloak
    provideKeycloak({
      config: {
        url: environment.authBaseUrl, // Base URL of Keycloak
        realm: environment.authRealmName, // Keycloak Realm
        clientId: environment.authClientId, // Keycloak Client ID
      },
      initOptions: {
        onLoad: 'check-sso', // Checks if user is logged in without redirecting immediately
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html'
      },
      // Modern feature: Auto refresh token based on user activity
      features: [
        withAutoRefreshToken({
          onInactivityTimeout: 'logout',
          sessionTimeout: 60000 
        })
      ]
    }),
    // 2. Configure Auto-Interceptor to add Token to Requests
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [
        createInterceptorCondition<IncludeBearerTokenCondition>({
          urlPattern: new RegExp(`^(${environment.apiBaseUrl})(?:/.*)?$`), // Match Spring Boot Port
        })
      ]
    },
    // 3. Register HTTP Client with the Interceptor
    provideHttpClient(withInterceptors([
      apiInterceptor,
      includeBearerTokenInterceptor
    ])),
    // 4. Required services for auto-refresh
    UserActivityService,
    AutoRefreshTokenService
  ]
};

