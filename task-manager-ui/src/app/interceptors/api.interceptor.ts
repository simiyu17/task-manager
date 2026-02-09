import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  // If request already uses the API base URL, pass it through
  if (req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  // Skip truly external URLs (CDN, third-party APIs, etc.)
  if (req.url.startsWith('http://') || req.url.startsWith('https://')) {
    return next(req);
  }

  // For all relative URLs, prepend the API base URL
  const endpoint = req.url.replace(/^\//, ''); // Remove leading slash if present
  const apiReq = req.clone({
    url: `${environment.apiBaseUrl}/${endpoint}`
  });

  return next(apiReq);
};
