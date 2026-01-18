#!/usr/bin/env bash

APP_PORT=${APP_PORT:-8080}
SWAGGER_URL="http://localhost:${APP_PORT}/api/swagger-ui/index.html"

echo ">>> Building project (skip tests by default)..."
./mvnw clean package -DskipTests || mvn clean package -DskipTests

echo ">>> Starting application on port ${APP_PORT}..."
# Run in background so the script can continue and open the browser
JAVA_CMD="./mvnw spring-boot:run"
if [ -x "./mvnw" ]; then
  ./mvnw spring-boot:run &
else
  mvn spring-boot:run &
fi

APP_PID=$!
echo ">>> Spring Boot started with PID ${APP_PID} (waiting for it to be ready)..."

# Wait a bit for the app to start
sleep 10

echo ">>> Opening Swagger UI at: ${SWAGGER_URL}"

# Try to open browser in a cross-platform way.
if command -v xdg-open > /dev/null; then
  xdg-open "${SWAGGER_URL}" >/dev/null 2>&1 &
elif command -v open > /dev/null; then
  open "${SWAGGER_URL}" >/dev/null 2>&1 &
elif command -v start > /dev/null; then
  start "${SWAGGER_URL}" >/dev/null 2>&1 &
else
  echo "Could not detect a browser command (xdg-open/open/start). Please open manually:"
  echo "  ${SWAGGER_URL}"
fi

echo ">>> Logs are streaming below. Press Ctrl+C to stop the application."
wait ${APP_PID}
