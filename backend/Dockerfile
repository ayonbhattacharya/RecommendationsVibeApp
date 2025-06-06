# Use Maven with OpenJDK 17 (single stage for simplicity)
FROM maven:3.8-openjdk-17

# Set working directory
WORKDIR /app

# Copy pom.xml first (for better caching)
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Expose port 8080
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production

# Run the application
CMD ["java", "-jar", "target/VibeCodingTryouts-1.0-SNAPSHOT.jar"]
