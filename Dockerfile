FROM maven:3.9.7-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml /app/pom.xml

RUN mvn dependency:go-offline 

COPY /src ./src

RUN mvn -f pom.xml clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
     
WORKDIR /app

RUN apk update && apk add --no-cache dumb-init && \
    addgroup -S javauser && adduser -S javauser -G javauser

COPY --from=build /app/target/*.jar app.jar

RUN chown -R javauser:javauser /app

USER javauser

EXPOSE 8080

CMD ["dumb-init", "java", "-jar", "app.jar"]