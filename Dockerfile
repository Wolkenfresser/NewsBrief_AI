# ---------- Build backend ----------
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- Runtime backend ----------
FROM eclipse-temurin:21-jre-jammy AS backend-runtime
WORKDIR /app
RUN useradd --system --uid 10001 --create-home appuser
COPY --from=backend-build /workspace/target/newsbrief-ai-1.0.0.jar /app/app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

# ---------- Build frontend ----------
FROM node:22-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json ./package.json
RUN npm install --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# ---------- Runtime frontend / reverse proxy ----------
FROM nginx:1.30.6-alpine AS frontend-runtime
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=frontend-build /workspace/frontend/dist /usr/share/nginx/html
EXPOSE 80
