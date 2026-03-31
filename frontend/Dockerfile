# Stage 1: Build Angular application
FROM node:20-alpine AS angular-build
WORKDIR /app/angular
COPY angular/package*.json ./
RUN npm install
COPY angular/ .
RUN npx ng build --configuration production

# Stage 2: Setup BFF server with built Angular files
FROM node:20-alpine
WORKDIR /app

# Install BFF dependencies
COPY bff/package*.json ./
RUN npm install --omit=dev

# Copy BFF server code
COPY bff/server.js .

# Copy built Angular files from stage 1 to public directory
COPY --from=angular-build /app/angular/dist/task-manager/browser ./public

# Expose port 3000
EXPOSE 3000

# Set environment variables (can be overridden at runtime)
ENV PORT=3000
ENV BACKEND_URL=http://localhost:8080

# Start the BFF server
CMD ["node", "server.js"]
