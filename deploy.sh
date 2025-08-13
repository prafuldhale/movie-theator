#!/bin/bash

# Movie Booking Application Docker Deployment Script
# Author: Movie Booking Team
# Version: 1.0

set -e  # Exit on any error

echo "🎬 Movie Booking Application - Docker Deployment"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    print_status "Checking Docker installation..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running or not installed. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Clean up old containers and images
cleanup() {
    print_status "Cleaning up old containers and images..."
    
    # Stop and remove old containers
    if docker ps -a --filter "name=moviebooking" --format "{{.Names}}" | grep -q .; then
        docker stop $(docker ps -a --filter "name=moviebooking" --format "{{.Names}}") 2>/dev/null || true
        docker rm $(docker ps -a --filter "name=moviebooking" --format "{{.Names}}") 2>/dev/null || true
        print_success "Old containers cleaned up"
    fi
    
    # Remove old images (optional - uncomment if you want to force rebuild)
    # docker rmi moviebooking-backend:latest 2>/dev/null || true
}

# Build the application
build_app() {
    print_status "Building the application..."
    
    # Run tests first
    print_status "Running tests..."
    if ! mvn test; then
        print_error "Tests failed. Please fix the issues before deploying."
        exit 1
    fi
    print_success "Tests passed"
    
    # Build the application
    print_status "Building with Maven..."
    if ! mvn clean package -DskipTests; then
        print_error "Maven build failed."
        exit 1
    fi
    print_success "Maven build completed"
}

# Build Docker image
build_docker() {
    print_status "Building Docker image..."
    
    # Build the image
    if docker build -t moviebooking-backend:latest .; then
        print_success "Docker image built successfully"
    else
        print_error "Docker build failed"
        exit 1
    fi
}

# Deploy with docker-compose
deploy() {
    print_status "Deploying with docker-compose..."
    
    # Create logs directory if it doesn't exist
    mkdir -p logs
    
    # Deploy using docker-compose
    if docker-compose up -d; then
        print_success "Application deployed successfully"
    else
        print_error "Docker-compose deployment failed"
        exit 1
    fi
}

# Check application health
check_health() {
    print_status "Checking application health..."
    
    # Wait for application to start
    print_status "Waiting for application to start (30 seconds)..."
    sleep 30
    
    # Check if application is responding
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_success "Application is healthy and responding"
    else
        print_warning "Application health check failed. Checking logs..."
        docker-compose logs moviebooking-app
        print_warning "Please check the logs above for any issues"
    fi
}

# Show deployment information
show_info() {
    echo ""
    echo "🎉 Deployment Complete!"
    echo "======================"
    echo "Application URL: http://localhost:8080"
    echo "H2 Console: http://localhost:8080/h2-console"
    echo "API Documentation: http://localhost:8080/api/v1.0/moviebooking"
    echo ""
    echo "Useful Commands:"
    echo "  View logs: docker-compose logs -f moviebooking-app"
    echo "  Stop app: docker-compose down"
    echo "  Restart app: docker-compose restart"
    echo "  View containers: docker ps"
    echo ""
}

# Main deployment process
main() {
    echo "Starting deployment process..."
    echo ""
    
    check_docker
    cleanup
    build_app
    build_docker
    deploy
    check_health
    show_info
}

# Handle command line arguments
case "${1:-}" in
    "clean")
        print_status "Cleaning up only..."
        cleanup
        print_success "Cleanup completed"
        ;;
    "build")
        print_status "Building only..."
        build_app
        build_docker
        print_success "Build completed"
        ;;
    "deploy")
        print_status "Deploying only..."
        deploy
        check_health
        show_info
        ;;
    "logs")
        print_status "Showing logs..."
        docker-compose logs -f moviebooking-app
        ;;
    "stop")
        print_status "Stopping application..."
        docker-compose down
        print_success "Application stopped"
        ;;
    "restart")
        print_status "Restarting application..."
        docker-compose restart
        print_success "Application restarted"
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  (no args)  Full deployment process"
        echo "  clean      Clean up old containers and images"
        echo "  build      Build application and Docker image only"
        echo "  deploy     Deploy with docker-compose only"
        echo "  logs       Show application logs"
        echo "  stop       Stop the application"
        echo "  restart    Restart the application"
        echo "  help       Show this help message"
        ;;
    *)
        main
        ;;
esac
