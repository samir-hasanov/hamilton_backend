# Build mərhələsi
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Run mərhələsi
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# build/libs-də yaranan jar-ı kopyalayırıq
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
