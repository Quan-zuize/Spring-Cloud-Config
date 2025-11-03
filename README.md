# Spring Cloud Config Example

This project demonstrates Spring Cloud Config with:
- **demo** service as the Config Server
- **demo-client** service as the Config Client

## Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│                 │         │                  │         │                 │
│  Config Client  │────────▶│  Config Server   │────────▶│  Git Repository │
│  (demo-client)  │         │     (demo)       │         │  (config-repo)  │
│   Port: 8081    │         │   Port: 8888     │         │                 │
└─────────────────┘         └──────────────────┘         └─────────────────┘
```

## Configuration Structure

### Config Server (demo)
- **Port**: 8888
- **Repository**: `C:\Users\quanna2\config-repo`
- **Security**: Basic Auth (admin/admin123)
- **Annotation**: `@EnableConfigServer`

### Config Repository Files
Located in `C:\Users\quanna2\config-repo`:
- `demo-client.properties` - Default configuration
- `demo-client-dev.properties` - Development environment
- `demo-client-prod.properties` - Production environment

### Config Client (demo-client)
- **Port**: 8081
- **Active Profile**: dev (configurable)
- **Features**:
  - Fetches configuration from Config Server
  - `@RefreshScope` for dynamic configuration updates
  - REST endpoints to view current configuration

## How to Run

### Step 1: Start Config Server
```cmd
cd C:\Users\quanna2\Downloads\demo\demo
mvnw spring-boot:run
```

The Config Server will start on http://localhost:8888

### Step 2: Start Config Client
```cmd
cd C:\Users\quanna2\Downloads\demo\demo-client
mvnw spring-boot:run
```

The Config Client will start on http://localhost:8081

## Testing the Configuration

### 1. View Current Configuration
```cmd
curl http://localhost:8081/api/config
```

Response (with dev profile):
```json
{
  "message": "Hello from Config Server - DEV Environment!",
  "app.description": "Demo-client DEV environment",
  "app.version": "1.0.0-DEV",
  "db.connection.timeout": 60,
  "db.pool.size": 5,
  "feature.newUI": true,
  "feature.betaFeatures": true
}
```

### 2. View Message Only
```cmd
curl http://localhost:8081/api/message
```

Response:
```
Hello from Config Server - DEV Environment!
```

### 3. View Feature Flags
```cmd
curl http://localhost:8081/api/features
```

Response:
```json
{
  "newUI": true,
  "betaFeatures": true
}
```

### 4. Access Config Server Directly
View configuration for demo-client (default profile):
```cmd
curl -u admin:admin123 http://localhost:8888/demo-client/default
```

View configuration for demo-client (dev profile):
```cmd
curl -u admin:admin123 http://localhost:8888/demo-client/dev
```

View configuration for demo-client (prod profile):
```cmd
curl -u admin:admin123 http://localhost:8888/demo-client/prod
```

## Dynamic Configuration Refresh

### Update Configuration Without Restart

1. **Modify config file**:
   Edit `C:\Users\quanna2\config-repo\demo-client-dev.properties`
   ```properties
   message=Updated message from Config Server!
   ```

2. **Commit changes**:
   ```cmd
   cd C:\Users\quanna2\config-repo
   git add .
   git commit -m "Update message"
   ```

3. **Refresh client configuration**:
   ```cmd
   curl -X POST http://localhost:8081/actuator/refresh
   ```

4. **Verify the change**:
   ```cmd
   curl http://localhost:8081/api/message
   ```

## Switching Profiles

To switch between environments, edit `demo-client/src/main/resources/application.properties`:

For Development:
```properties
spring.profiles.active=dev
```

For Production:
```properties
spring.profiles.active=prod
```

Then restart the demo-client application.

## Key Spring Cloud Config Concepts

### 1. Config Server (`@EnableConfigServer`)
- Centralized configuration management
- Git-backed configuration storage
- Supports multiple environments (profiles)

### 2. Config Client
- Fetches configuration from Config Server on startup
- Uses `spring.config.import` to connect to Config Server
- Supports authentication for secure access

### 3. Refresh Scope (`@RefreshScope`)
- Allows dynamic configuration updates
- No need to restart the application
- Triggered via `/actuator/refresh` endpoint

### 4. Configuration Files
- `{application}.properties` - Default configuration
- `{application}-{profile}.properties` - Profile-specific configuration
- Properties are merged (profile-specific overrides default)

## Troubleshooting

### Config Client can't connect to Config Server
- Ensure Config Server is running on port 8888
- Check credentials (admin/admin123)
- Verify `spring.config.import` in application.properties

### Configuration not updating
- Make sure changes are committed to git repository
- Call `/actuator/refresh` endpoint after updating config
- Verify `@RefreshScope` annotation is present

### Git repository errors
- Ensure git is initialized in config-repo directory
- Check file paths in Config Server application.properties
- Verify files are committed to git

## Project Structure

```
demo/
├── demo/                           # Config Server
│   ├── src/main/java/
│   │   └── com/quanna/demo/
│   │       └── ConfigServer.java   # @EnableConfigServer
│   ├── src/main/resources/
│   │   └── application.properties  # Server configuration
│   └── pom.xml                     # spring-cloud-config-server
│
└── demo-client/                    # Config Client
    ├── src/main/java/
    │   └── com/quanna/democlient/
    │       ├── DemoClientApplication.java
    │       ├── config/
    │       │   └── AppConfig.java  # @ConfigurationProperties
    │       └── controller/
    │           └── ConfigController.java  # REST endpoints
    ├── src/main/resources/
    │   └── application.properties  # Client configuration
    └── pom.xml                     # spring-cloud-starter-config

config-repo/                        # Git repository for configs
├── demo-client.properties          # Default configuration
├── demo-client-dev.properties      # Dev environment
└── demo-client-prod.properties     # Prod environment
```

## Additional Resources

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [Config Server Reference](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [Refresh Scope](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#refresh-scope)

