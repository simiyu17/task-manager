import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) {}

  /**
   * Performs a GET request
   * @param endpoint - API endpoint (relative to base URL)
   * @param options - HTTP options
   */
  get<T>(endpoint: string, options: any = {}): Observable<T> {
    return (this.http.get(endpoint, { ...options, observe: 'body' }) as Observable<T>).pipe(
      timeout(environment.apiTimeout),
      catchError(this.handleError)
    );
  }

  /**
   * Performs a POST request
   * @param endpoint - API endpoint (relative to base URL)
   * @param body - Request body
   * @param options - HTTP options
   */
  post<T>(endpoint: string, body: any, options: any = {}): Observable<T> {
    return (this.http.post(endpoint, body, { ...options, observe: 'body' }) as Observable<T>).pipe(
      timeout(environment.apiTimeout),
      catchError(this.handleError)
    );
  }

  /**
   * Performs a PUT request
   * @param endpoint - API endpoint (relative to base URL)
   * @param body - Request body
   * @param options - HTTP options
   */
  put<T>(endpoint: string, body: any, options: any = {}): Observable<T> {
    return (this.http.put(endpoint, body, { ...options, observe: 'body' }) as Observable<T>).pipe(
      timeout(environment.apiTimeout),
      catchError(this.handleError)
    );
  }

  /**
   * Performs a PATCH request
   * @param endpoint - API endpoint (relative to base URL)
   * @param body - Request body
   * @param options - HTTP options
   */
  patch<T>(endpoint: string, body: any, options: any = {}): Observable<T> {
    return (this.http.patch(endpoint, body, { ...options, observe: 'body' }) as Observable<T>).pipe(
      timeout(environment.apiTimeout),
      catchError(this.handleError)
    );
  }

  /**
   * Performs a DELETE request
   * @param endpoint - API endpoint (relative to base URL)
   * @param options - HTTP options
   */
  delete<T>(endpoint: string, options: any = {}): Observable<T> {
    return (this.http.delete(endpoint, { ...options, observe: 'body' }) as Observable<T>).pipe(
      timeout(environment.apiTimeout),
      catchError(this.handleError)
    );
  }

  /**
   * Error handler for HTTP requests
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error(errorMessage);
    return throwError(() => error);
  }
}
