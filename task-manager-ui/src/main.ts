/// <reference types="@angular/localize" />
import 'zone.js';
import { bootstrapApplication } from '@angular/platform-browser';
import { Chart, registerables } from 'chart.js';

import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

// Register Chart.js components and set defaults
Chart.register(...registerables);

// Set global Chart.js defaults to prevent clip errors
// Set clip to 0 (number) which disables clipping - Chart.js 4.x expects number or object, not boolean
Chart.defaults.datasets.doughnut = Chart.defaults.datasets.doughnut || {};
Chart.defaults.datasets.doughnut.clip = 0;
Chart.defaults.datasets.line = Chart.defaults.datasets.line || {};
Chart.defaults.datasets.line.clip = 0;
Chart.defaults.datasets.bar = Chart.defaults.datasets.bar || {};
Chart.defaults.datasets.bar.clip = 0;
Chart.defaults.datasets.pie = Chart.defaults.datasets.pie || {};
Chart.defaults.datasets.pie.clip = 0;
Chart.defaults.datasets.polarArea = Chart.defaults.datasets.polarArea || {};
Chart.defaults.datasets.polarArea.clip = 0;
Chart.defaults.datasets.radar = Chart.defaults.datasets.radar || {};
Chart.defaults.datasets.radar.clip = 0;
Chart.defaults.datasets.pie.clip = 0;

bootstrapApplication(AppComponent, appConfig)
  .catch(err => {
    console.error('Application bootstrap failed:', err);
    // Show user-friendly error message
    const root = document.querySelector('app-root');
    if (root) {
      root.innerHTML = `
        <div style="text-align: center; padding: 50px; font-family: Arial, sans-serif;">
          <h2 style="color: #dc3545;">Application Failed to Start</h2>
          <p>There was an error initializing the application. Please try:</p>
          <ul style="list-style: none; padding: 0;">
            <li>1. Refreshing the page</li>
            <li>2. Clearing your browser cache</li>
            <li>3. Checking your internet connection</li>
          </ul>
          <button onclick="window.location.reload()" 
                  style="padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; margin-top: 20px;">
            Reload Page
          </button>
          <details style="margin-top: 20px; text-align: left; max-width: 600px; margin-left: auto; margin-right: auto;">
            <summary style="cursor: pointer; color: #666;">Technical Details</summary>
            <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto;">${err}</pre>
          </details>
        </div>
      `;
    }
  });


