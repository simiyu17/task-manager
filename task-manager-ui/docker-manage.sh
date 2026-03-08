#!/bin/bash

# Task Manager Docker Management Script
# This script helps manage the Docker Compose deployment

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${NC}ℹ $1${NC}"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to check if docker-compose is available
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null; then
        print_error "docker-compose is not installed. Please install it and try again."
        exit 1
    fi
    print_success "docker-compose is available"
}

# Function to start all services
start_all() {
    print_info "Starting all services..."
    docker-compose up -d
    print_success "All services started"
    show_status
}

# Function to stop all services
stop_all() {
    print_info "Stopping all services..."
    docker-compose stop
    print_success "All services stopped"
}

# Function to restart all services
restart_all() {
    print_info "Restarting all services..."
    docker-compose restart
    print_success "All services restarted"
}

# Function to rebuild and restart services
rebuild() {
    local service=$1
    if [ -z "$service" ]; then
        print_info "Rebuilding all services..."
        docker-compose up -d --build
        print_success "All services rebuilt and restarted"
    else
        print_info "Rebuilding $service..."
        docker-compose up -d --build "$service"
        print_success "$service rebuilt and restarted"
    fi
}

# Function to show logs
show_logs() {
    local service=$1
    if [ -z "$service" ]; then
        print_info "Showing logs for all services (Ctrl+C to exit)..."
        docker-compose logs -f
    else
        print_info "Showing logs for $service (Ctrl+C to exit)..."
        docker-compose logs -f "$service"
    fi
}

# Function to show status
show_status() {
    print_info "Service Status:"
    docker-compose ps
    echo ""
    print_info "Service Health:"
    for service in task-manager-ui task-manager-api keycloak traefik; do
        if docker ps --format '{{.Names}}' | grep -q "^${service}$"; then
            health=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "no health check")
            if [ "$health" = "healthy" ]; then
                print_success "$service: $health"
            elif [ "$health" = "no health check" ]; then
                echo "  $service: running (no health check)"
            else
                print_warning "$service: $health"
            fi
        else
            print_error "$service: not running"
        fi
    done
}

# Function to clean everything
clean_all() {
    print_warning "This will remove all containers and networks!"
    print_warning "External PostgreSQL databases will NOT be affected."
    read -p "Are you sure? (yes/no): " -r
    if [[ $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        print_info "Cleaning up..."
        docker-compose down
        print_success "Cleanup complete"
    else
        print_info "Cleanup cancelled"
    fi
}

# Function to access a service shell
shell() {
    local service=$1
    if [ -z "$service" ]; then
        print_error "Please specify a service name"
        print_info "Available services: task-manager-ui, task-manager-api, keycloak, traefik"
        exit 1
    fi
    print_info "Accessing shell for $service..."
    docker exec -it "$service" /bin/sh
}

# Function to backup databases
backup() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_dir="./backups"
    
    mkdir -p "$backup_dir"
    
    print_info "Backing up external PostgreSQL databases..."
    print_warning "Note: This requires PostgreSQL client tools and proper credentials"
    
    # Backup application database (requires host PostgreSQL access)
    if command -v pg_dump &> /dev/null; then
        print_info "Backing up taskmanager database..."
        pg_dump -h localhost -U taskmanager -d taskmanager > "$backup_dir/taskmanager_${timestamp}.sql" 2>/dev/null && \
            print_success "Application database backed up to $backup_dir/taskmanager_${timestamp}.sql" || \
            print_error "Failed to backup taskmanager database (check credentials)"
        
        # Backup keycloak database
        print_info "Backing up keycloak database..."
        pg_dump -h localhost -U keycloak -d keycloak > "$backup_dir/keycloak_${timestamp}.sql" 2>/dev/null && \
            print_success "Keycloak database backed up to $backup_dir/keycloak_${timestamp}.sql" || \
            print_error "Failed to backup keycloak database (check credentials)"
    else
        print_error "pg_dump not found. Install PostgreSQL client tools: sudo apt install postgresql-client"
    fi
}

# Function to show help
show_help() {
    cat << EOF
Task Manager Docker Management Script

Usage: ./docker-manage.sh [command] [options]

Commands:
    start           Start all services
    stop            Stop all services
    restart         Restart all services
    rebuild [svc]   Rebuild and restart all services or specific service
    logs [svc]      Show logs for all services or specific service
    status          Show status of all services
    shell <svc>     Access shell of a specific service
    backup          Backup external PostgreSQL databases (requires pg_dump)
    clean           Remove all containers and networks
    help            Show this help message

Service names:
    task-manager-ui     Angular 21 frontend
    task-manager-api    Spring Boot 3.5.10 backend
    keycloak           Keycloak 26.0.5 authentication server
    traefik            Traefik reverse proxy with automatic SSL

Note: This setup uses:
- External PostgreSQL databases on the host (not managed by Docker)
- Traefik for automatic SSL certificate management via Let's Encrypt
- All services accessed through https://your-domain.com

Examples:
    ./docker-manage.sh start
    ./docker-manage.sh logs task-manager-api
    ./docker-manage.sh rebuild task-manager-ui
    ./docker-manage.sh shell keycloak
    ./docker-manage.sh backup

EOF
}

# Main script logic
main() {
    check_docker
    check_docker_compose
    
    case "${1:-help}" in
        start)
            start_all
            ;;
        stop)
            stop_all
            ;;
        restart)
            restart_all
            ;;
        rebuild)
            rebuild "$2"
            ;;
        logs)
            show_logs "$2"
            ;;
        status)
            show_status
            ;;
        shell)
            shell "$2"
            ;;
        backup)
            backup
            ;;
        clean)
            clean_all
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
