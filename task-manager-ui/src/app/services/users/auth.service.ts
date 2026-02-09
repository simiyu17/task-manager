import { Injectable, inject, computed } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private keycloak = inject(Keycloak);

  // Expose a signal for the authentication status
  public isAuthenticated = computed(() => this.keycloak.authenticated ?? false);

  hasPermission(role: string): boolean {
    return this.keycloak.hasRealmRole(role);
  }
}