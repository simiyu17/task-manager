# Stage 1: Build the Angular application
FROM node:22-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy project files
COPY . .

# Build the application for docker environment
RUN npm run build:docker

# Stage 2: Serve the application with Nginx
FROM nginx:1.27-alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built application from build stage
COPY --from=build /app/dist/task-manager-ui/browser /usr/share/nginx/html

# Create assets directory if it doesn't exist
RUN mkdir -p /usr/share/nginx/html/assets

# Copy environment configuration script
COPY generate-env-config.sh /docker-entrypoint.d/40-generate-env-config.sh

# Make the script executable
RUN chmod +x /docker-entrypoint.d/40-generate-env-config.sh

# Expose port 80
EXPOSE 80

# Nginx will automatically run scripts in /docker-entrypoint.d/
# Our script runs before nginx starts, generating env-config.js from environment variables
CMD ["nginx", "-g", "daemon off;"]
