# Movie Theater Booking Application

This is a movie theater booking application built with Spring Boot and containerized using Docker.

## Prerequisites

- Docker installed on your machine
- Docker Compose (optional, for running with dependent services)

## Building and Running with Docker

### 1. Build the Docker Image

```bash
docker build -t movie-theater-app .
```

### 2. Run the Container

```bash
docker run -d -p 8080:8080 --name movie-theater movie-theater-app
```

The application will be available at `http://localhost:8080`

### Environment Variables

The following environment variables can be configured when running the container:

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/moviedb \
  --name movie-theater \
  movie-theater-app
```

## Docker Compose (Recommended)

Create a `docker-compose.yml` file to run the application with all its dependencies (MongoDB, Kafka, etc.).

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/moviedb
    depends_on:
      - mongodb
      - kafka

  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

volumes:
  mongodb_data:
```

### Running with Docker Compose

1. Start all services:
```bash
docker-compose up -d
```

2. Stop all services:
```bash
docker-compose down
```

## Container Management

### View Logs
```bash
docker logs movie-theater
```

### Stop Container
```bash
docker stop movie-theater
```

### Remove Container
```bash
docker rm movie-theater
```

### Remove Image
```bash
docker rmi movie-theater-app
```

## Troubleshooting

1. If the application fails to start, check the logs:
```bash
docker logs movie-theater
```

2. To check if the container is running:
```bash
docker ps
```

3. To check resource usage:
```bash
docker stats movie-theater
```
