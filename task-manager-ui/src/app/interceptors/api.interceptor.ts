import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpInterceptorFn } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  // If request already uses the API base URL, pass it through
  if (req.url.startsWith(environment.apiBaseUrl)) {
    console.log('[ApiInterceptor] Already using API base URL, passing through:', req.url);
    return next(req);
  }

  // Skip truly external URLs (CDN, third-party APIs, etc.)
  if (req.url.startsWith('http://') || req.url.startsWith('https://')) {
    console.log('[ApiInterceptor] External URL detected, passing through:', req.url);
    return next(req);
  }

  // For all relative URLs, prepend the API base URL
  const endpoint = req.url.replace(/^\//, ''); // Remove leading slash if present
  const apiReq = req.clone({
    url: `${environment.apiBaseUrl}/${endpoint}`
  });

  console.log('[ApiInterceptor] Prepended base URL:', apiReq.url);
  return next(apiReq);
};
