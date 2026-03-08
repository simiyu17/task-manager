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
  const requiredRoles = _route.data['roles'] as string[];
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
  
  };