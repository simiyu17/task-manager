import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable} from 'rxjs';
import Keycloak from 'keycloak-js';

export const AuthGuard: CanActivateFn = (
    _route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ):
    Observable<boolean | UrlTree> 
    | Promise<boolean | UrlTree> 
    | boolean 
    | UrlTree=> {
    
    const keycloak = inject(Keycloak);
    const router = inject(Router);

    // 0. Wait for Keycloak initialization if still pending
    if (keycloak.authenticated === undefined) {
      // Keycloak not yet initialized, wait a bit
      return new Promise((resolve) => {
        const maxWait = 5000; // 5 seconds max
        const startTime = Date.now();
        const checkInterval = setInterval(() => {
          if (keycloak.authenticated !== undefined) {
            clearInterval(checkInterval);
            resolve(checkAuth(keycloak, router, state, _route));
          } else if (Date.now() - startTime > maxWait) {
            clearInterval(checkInterval);
            console.error('Keycloak initialization timeout');
            // Force login as fallback
            keycloak.login({ redirectUri: window.location.href });
            resolve(false);
          }
        }, 100);
      });
    }

    return checkAuth(keycloak, router, state, _route);
  };

function checkAuth(
  keycloak: Keycloak,
  router: Router,
  state: RouterStateSnapshot,
  route: ActivatedRouteSnapshot
): boolean | UrlTree {
  // 1. Check if user is authenticated
  if (!keycloak.authenticated) {
    // Use current location for better Firefox compatibility
    const redirectUri = window.location.href;
    keycloak.login({ 
      redirectUri: redirectUri,
      prompt: 'login' // Force fresh login for better cross-browser compatibility
    });
    return false;
  }

  // 2. Check for required roles defined in route data
  const requiredRoles = route.data['roles'] as string[];
  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  // Check if user has at least one of the required roles
  const hasRole = requiredRoles.some((role) => keycloak.hasRealmRole(role));

  if (!hasRole) {
    router.navigate(['/unauthorized']);
    return false;
  }

  return true;
}