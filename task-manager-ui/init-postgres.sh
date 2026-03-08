#!/bin/bash

# PostgreSQL Database Initialization Script for Task Manager
# This script creates the required databases and users for the application

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${NC}ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Check if running as root or with sudo
if [ "$EUID" -ne 0 ]; then
    print_error "Please run this script with sudo"
    exit 1
fi

print_info "Task Manager PostgreSQL Database Setup"
echo ""

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    print_error "PostgreSQL is not installed"
    print_info "Install it with: sudo apt install postgresql postgresql-contrib"
    exit 1
fi

print_success "PostgreSQL is installed"

# Prompt for passwords
read -sp "Enter password for Keycloak database user: " KC_PASSWORD
echo ""
read -sp "Confirm password: " KC_PASSWORD_CONFIRM
echo ""

if [ "$KC_PASSWORD" != "$KC_PASSWORD_CONFIRM" ]; then
    print_error "Passwords do not match"
    exit 1
fi

read -sp "Enter password for TaskManager database user: " TM_PASSWORD
echo ""
read -sp "Confirm password: " TM_PASSWORD_CONFIRM
echo ""

if [ "$TM_PASSWORD" != "$TM_PASSWORD_CONFIRM" ]; then
    print_error "Passwords do not match"
    exit 1
fi

print_info "Creating databases and users..."
echo ""

# Create SQL commands
SQL_COMMANDS=$(cat <<EOF
-- Create Keycloak database and user
CREATE DATABASE keycloak;
CREATE USER keycloak WITH ENCRYPTED PASSWORD '$KC_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
\c keycloak
GRANT ALL ON SCHEMA public TO keycloak;

-- Create TaskManager database and user
\c postgres
CREATE DATABASE taskmanager;
CREATE USER taskmanager WITH ENCRYPTED PASSWORD '$TM_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskmanager;
\c taskmanager
GRANT ALL ON SCHEMA public TO taskmanager;
EOF
)

# Execute SQL commands as postgres user
if sudo -u postgres psql <<< "$SQL_COMMANDS" 2>&1 | grep -q "ERROR"; then
    print_error "Failed to create databases. They may already exist."
    print_info "To recreate, first drop existing databases:"
    print_info "  sudo -u postgres psql -c 'DROP DATABASE IF EXISTS keycloak; DROP USER IF EXISTS keycloak;'"
    print_info "  sudo -u postgres psql -c 'DROP DATABASE IF EXISTS taskmanager; DROP USER IF EXISTS taskmanager;'"
    exit 1
fi

print_success "Databases created successfully"
echo ""

# Configure pg_hba.conf for Docker access
PG_VERSION=$(sudo -u postgres psql -t -c "SELECT version();" | grep -oP '\d+\.\d+' | head -1 | cut -d'.' -f1)
PG_HBA_FILE="/etc/postgresql/$PG_VERSION/main/pg_hba.conf"

if [ -f "$PG_HBA_FILE" ]; then
    print_info "Configuring PostgreSQL for Docker access..."
    
    # Backup original file
    cp "$PG_HBA_FILE" "$PG_HBA_FILE.backup.$(date +%Y%m%d_%H%M%S)"
    
    # Check if Docker access rules already exist
    if ! grep -q "# Docker containers access" "$PG_HBA_FILE"; then
        # Add Docker access rules
        cat >> "$PG_HBA_FILE" <<EOF

# Docker containers access
host    keycloak        keycloak        172.16.0.0/12           md5
host    taskmanager     taskmanager     172.16.0.0/12           md5
EOF
        print_success "Docker access rules added to pg_hba.conf"
    else
        print_warning "Docker access rules already exist in pg_hba.conf"
    fi
else
    print_error "Could not find pg_hba.conf at $PG_HBA_FILE"
    print_info "Please manually add these lines to your pg_hba.conf:"
    print_info "  host    keycloak        keycloak        172.16.0.0/12           md5"
    print_info "  host    taskmanager     taskmanager     172.16.0.0/12           md5"
fi

# Configure postgresql.conf to listen on all interfaces
PG_CONF_FILE="/etc/postgresql/$PG_VERSION/main/postgresql.conf"

if [ -f "$PG_CONF_FILE" ]; then
    print_info "Configuring PostgreSQL to listen on all interfaces..."
    
    # Backup original file
    cp "$PG_CONF_FILE" "$PG_CONF_FILE.backup.$(date +%Y%m%d_%H%M%S)"
    
    # Update listen_addresses
    if grep -q "^listen_addresses" "$PG_CONF_FILE"; then
        sed -i "s/^listen_addresses.*/listen_addresses = '*'/" "$PG_CONF_FILE"
    else
        echo "listen_addresses = '*'" >> "$PG_CONF_FILE"
    fi
    print_success "PostgreSQL configured to listen on all interfaces"
else
    print_error "Could not find postgresql.conf at $PG_CONF_FILE"
fi

# Restart PostgreSQL
print_info "Restarting PostgreSQL..."
if systemctl restart postgresql; then
    print_success "PostgreSQL restarted successfully"
else
    print_error "Failed to restart PostgreSQL"
    exit 1
fi

# Verify PostgreSQL is listening
if netstat -plnt 2>/dev/null | grep -q ":5432.*0.0.0.0"; then
    print_success "PostgreSQL is listening on all interfaces"
else
    print_warning "PostgreSQL might not be listening on all interfaces"
    print_info "Check with: sudo netstat -plnt | grep 5432"
fi

echo ""
print_success "Database setup complete!"
echo ""
print_info "Database Details:"
echo "  Keycloak Database: keycloak"
echo "  Keycloak User: keycloak"
echo "  TaskManager Database: taskmanager"
echo "  TaskManager User: taskmanager"
echo ""
print_info "Next steps:"
echo "  1. Update your .env file with the database passwords"
echo "  2. Start Docker services: ./docker-manage.sh start"
echo "  3. Configure Keycloak and your application"
echo ""
print_warning "Remember to keep your database passwords secure!"

