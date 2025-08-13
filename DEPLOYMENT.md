# 🎬 Movie Booking Application - Docker Deployment Guide

This guide will help you deploy the Movie Booking Application using Docker and Docker Compose.

## 📋 Prerequisites

Before deploying, ensure you have the following installed:

- **Docker Desktop** (Windows/Mac) or **Docker Engine** (Linux)
- **Docker Compose** (usually included with Docker Desktop)
- **Java 17** (for local development and testing)
- **Maven 3.9+** (for building the application)
- **Git** (for cloning the repository)

## 🚀 Quick Deployment

### Option 1: Using the Deployment Script (Recommended)

#### For Linux/Mac:
```bash
# Make the script executable (first time only)
chmod +x deploy.sh

# Run the deployment script
./deploy.sh
```

#### For Windows:
```cmd
# Run the deployment script
deploy.bat
```

### Option 2: Manual Deployment

#### Step 1: Build the Application
```bash
# Run tests
mvn test

# Build the application
mvn clean package -DskipTests
```

#### Step 2: Build Docker Image
```bash
# Build the Docker image
docker build -t moviebooking-backend:latest .
```

#### Step 3: Deploy with Docker Compose
```bash
# Create logs directory
mkdir -p logs

# Deploy the application
docker-compose up -d
```

#### Step 4: Verify Deployment
```bash
# Check if the application is running
docker ps

# Check application logs
docker-compose logs -f moviebooking-app

# Test the application
curl http://localhost:8080/actuator/health
```

## 🌐 Accessing the Application

Once deployed, you can access the application at:

- **Application URL**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
- **API Base URL**: http://localhost:8080/api/v1.0/moviebooking

### H2 Console Configuration
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (leave empty)

## 📊 Monitoring and Logs

### View Application Logs
```bash
# View real-time logs
docker-compose logs -f moviebooking-app

# View logs with timestamps
docker-compose logs -t moviebooking-app

# View last 100 lines
docker-compose logs --tail=100 moviebooking-app
```

### Check Application Health
```bash
# Health check endpoint
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info
```

### Monitor Container Status
```bash
# List running containers
docker ps

# View container resource usage
docker stats

# View container details
docker inspect moviebooking-backend
```

## 🔧 Configuration

### Environment Variables

The application can be configured using environment variables in the `docker-compose.yml` file:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
  - SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
  - SPRING_DATASOURCE_USERNAME=sa
  - SPRING_DATASOURCE_PASSWORD=
  - SPRING_H2_CONSOLE_ENABLED=true
  - SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
  - SPRING_JPA_SHOW_SQL=true
  - SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Logging Configuration

Logs are stored in the `./logs` directory and include:
- **Console logs**: Human-readable format
- **File logs**: `moviebooking-app.log` with rotation
- **JSON logs**: Structured format for analysis
- **Kafka logs**: For centralized logging (if Kafka is enabled)

## 🛠️ Management Commands

### Using Docker Compose
```bash
# Start the application
docker-compose up -d

# Stop the application
docker-compose down

# Restart the application
docker-compose restart

# View logs
docker-compose logs -f moviebooking-app

# Scale the application (if needed)
docker-compose up -d --scale moviebooking-app=2
```

### Using the Deployment Script
```bash
# Full deployment
./deploy.sh

# Clean up only
./deploy.sh clean

# Build only
./deploy.sh build

# Deploy only
./deploy.sh deploy

# View logs
./deploy.sh logs

# Stop application
./deploy.sh stop

# Restart application
./deploy.sh restart

# Show help
./deploy.sh help
```

## 🔍 Troubleshooting

### Common Issues

#### 1. Port Already in Use
```bash
# Check what's using port 8080
netstat -tulpn | grep :8080

# Kill the process or change the port in docker-compose.yml
```

#### 2. Docker Build Fails
```bash
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker build --no-cache -t moviebooking-backend:latest .
```

#### 3. Application Won't Start
```bash
# Check container logs
docker-compose logs moviebooking-app

# Check container status
docker ps -a

# Restart the container
docker-compose restart moviebooking-app
```

#### 4. Database Connection Issues
```bash
# Check if H2 console is accessible
curl http://localhost:8080/h2-console

# Verify database configuration
docker-compose exec moviebooking-app env | grep SPRING_DATASOURCE
```

### Debug Mode

To run the application in debug mode:

```bash
# Stop the current deployment
docker-compose down

# Run with debug logging
docker-compose up -d --build
docker-compose logs -f moviebooking-app
```

## 🔄 Updating the Application

### Option 1: Using the Deployment Script
```bash
# This will rebuild and redeploy automatically
./deploy.sh
```

### Option 2: Manual Update
```bash
# Pull latest changes
git pull

# Rebuild and redeploy
mvn clean package -DskipTests
docker build -t moviebooking-backend:latest .
docker-compose down
docker-compose up -d
```

## 🧪 Testing the Deployment

### API Testing
```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Test movie listing
curl http://localhost:8080/api/v1.0/moviebooking/all

# Test user registration
curl -X POST http://localhost:8080/api/v1.0/moviebooking/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "loginId": "johndoe",
    "password": "Password123@",
    "confirmPassword": "Password123@",
    "contactNumber": "1234567890"
  }'
```

### Using the HAR File
Import the `endpoints.har` file into Postman or similar API testing tools to test all endpoints.

## 📈 Production Deployment

For production deployment, consider:

1. **Database**: Use a production database (PostgreSQL, MySQL) instead of H2
2. **Security**: Configure proper security settings
3. **Monitoring**: Set up application monitoring and alerting
4. **Load Balancing**: Use a reverse proxy (Nginx, HAProxy)
5. **SSL/TLS**: Configure HTTPS
6. **Backup**: Set up database backups
7. **Scaling**: Configure horizontal scaling

### Production Docker Compose Example
```yaml
version: '3.8'

services:
  moviebooking-app:
    build: .
    container_name: moviebooking-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/moviebooking
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    depends_on:
      - db
    restart: unless-stopped

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=moviebooking
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

## 📞 Support

If you encounter any issues:

1. Check the application logs: `docker-compose logs moviebooking-app`
2. Verify Docker is running: `docker info`
3. Check system resources: `docker stats`
4. Review the troubleshooting section above

For additional help, please refer to the application documentation or create an issue in the project repository.
