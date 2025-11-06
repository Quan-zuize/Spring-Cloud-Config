# Spring Cloud Config with Spring Cloud Bus Example

This project demonstrates Spring Cloud Config with:
- **config-service** (demo service) as the centralized Config Server
- **demo-client** service as the Config Client
- **Spring Cloud Bus** with RabbitMQ for distributed configuration refresh

## Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────────────┐
│                 │         │                  │         │                         │
│  Config Client  │────────▶│  Config Server   │────────▶│  Native File System     │
│  (demo-client)  │         │  (demo service)  │         │  (config-repository)    │
│   Port: 8001    │         │   Port: 8888     │         │  (./config/...)         │
│                 │         │                  │         │                         │
└────────┬────────┘         └────────┬─────────┘         └─────────────────────────┘
         │                           │
         │      ┌────────────────────┘
         │      │
         └──────┼──────────────────┐
                │                  │
         ┌──────▼──────────────────▼───┐
         │                              │
         │       RabbitMQ Message Bus   │
         │   (Spring Cloud Bus AMQP)    │
         │                              │
         └──────────────────────────────┘
         
When /actuator/busrefresh is called:
1. Config Server broadcasts RefreshRemoteApplicationEvent via RabbitMQ
2. All connected clients receive the event through Spring Cloud Bus
3. Clients automatically refresh their configuration without restart
```

## Configuration Structure

### Config Server (demo service)
- **Port**: 8888
- **Storage Type**: Native (File System)
- **Repository Location**: `./config/config-repository/`
- **Security**: Basic Auth (admin/admin123)
- **Annotation**: `@EnableConfigServer`
- **Spring Boot**: 3.5.7
- **Spring Cloud**: 2025.0.0
- **Spring Cloud Bus**: Enabled with RabbitMQ (AMQP)
- **Dependencies**:
  - `spring-cloud-config-server`
  - `spring-cloud-starter-bus-amqp`
- **RabbitMQ**: localhost:5672 (default)
- **Bus Refresh**: Broadcast configuration updates to all clients via `/actuator/busrefresh`

### Dynamic Search Locations Pattern
The Config Server uses a flexible search pattern to resolve configurations:
```yaml
search-locations:
  # Common configs based on service name (label): dev, dc, dr, etc.
  - file:./config/config-repository/common/{label}
  
  # Shared configs: redis, rabbit, kafka, db, cache
  - file:./config/config-repository/{profile}/common/
  
  # Service-specific configs
  - file:./config/config-repository/{profile}/{label}/
```

**Where**:
- `{profile}`: Environment (dev, prod, staging, etc.)
- `{label}`: Service name (demo-client, order-service, etc.)

### Config Client (demo-client)
- **Port**: 8001
- **Active Profile**: dev (configurable)
- **Label**: demo-client (service name)
- **Dependencies**:
  - `spring-cloud-starter-config`
  - `spring-cloud-starter-bus-amqp`
- **RabbitMQ**: localhost:5672 (default)
- **Features**:
  - Fetches configuration from Config Server on startup
  - `@RefreshScope` for dynamic configuration updates
  - REST endpoints to view current configuration
  - Can include common configurations (application-common.yml)
  - Automatically receives refresh events via Spring Cloud Bus
  - No restart required when configuration changes

### Config Repository Structure
Located in `config-service/config/config-repository/`:

```
config-repository/
├── dev/                              # Development profile
│   ├── common/                       # Shared configs for all dev services
│   │   └── (redis, db, kafka...)    # Future: Shared infrastructure configs
│   └── demo-client/                  # demo-client service configs
│       ├── application.yml           # Service-specific application config
│       └── demo-client.yml           # Service-specific custom config
│
└── prod/                             # Production profile
    ├── common/                       # Shared configs for all prod services
    │   └── demo-client.yml           # Common production config
    ├── demo-client/                  # demo-client service configs
    │   └── demo-client.yml           # Production-specific config
    └── master/                       # Master branch configs
        └── demo-client.yml           # Master-specific config
```

**Configuration Priority** (highest to lowest):
1. `/{profile}/{label}/{application}.yml` - Service-specific config for profile
2. `/{profile}/common/{application}.yml` - Common config for profile
3. `/common/{label}/{application}.yml` - Service-specific common config

## Prerequisites

### RabbitMQ Installation
Spring Cloud Bus requires RabbitMQ to be running. 

**Option 1: Docker (Recommended)**
```cmd
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

**Option 2: Windows Installer**
1. Download and install [Erlang](https://www.erlang.org/downloads)
2. Download and install [RabbitMQ](https://www.rabbitmq.com/download.html)
3. Start RabbitMQ service

**Verify RabbitMQ:**
- Management UI: http://localhost:15672
- Default credentials: guest/guest

## How to Run

### Step 1: Start RabbitMQ
Make sure RabbitMQ is running before starting the services:
```cmd
docker start rabbitmq
```
Or if using Windows service, ensure the RabbitMQ service is running.

### Step 2: Start Config Server
```cmd
cd C:\Users\quanna2\Downloads\demo\config-service
mvnw spring-boot:run
```

The Config Server will start on http://localhost:8888

### Step 2: Start Config Client
In your demo-client application, add the following configuration:

**application.yml** or **application.properties**:
```yaml
spring:
  application:
    name: demo-client
  profiles:
    active: dev
  config:
    import: "configserver:http://localhost:8888"
  cloud:
    config:
      username: admin
      password: admin123
      label: demo-client  # Service name as label
      profile: dev        # Environment
```

Then start the client:
```cmd
cd C:\Users\quanna2\Downloads\demo\demo-client
mvnw spring-boot:run
```

The Config Client will start on http://localhost:8001

## Testing the Configuration

### 1. Access Config Server Directly

**View dev profile with demo-client label:**
```cmd
curl -u admin:admin123 http://localhost:8888/demo-client/dev/demo-client
```

Response:
```json
{
  "name": "demo-client",
  "profiles": ["dev"],
  "label": "demo-client",
  "propertySources": [
    {
      "name": "file:./config/config-repository/dev/demo-client/demo-client.yml",
      "source": {
        "server.port": 8001,
        "management.endpoints.web.exposure.include": "health,info,env,refresh"
      }
    },
    {
      "name": "file:./config/config-repository/dev/demo-client/application.yml",
      "source": {
        "message": "Hello from Config Server - DEV/demo-client!",
        "app.description": "Demo Client - Development Environment",
        "app.version": "1.0.0-DEV",
        "db.connection.timeout": 60,
        "db.pool.size": 5,
        "feature.newUI": true,
        "feature.betaFeatures": true
      }
    }
  ]
}
```

**View prod profile with demo-client label:**
```cmd
curl -u admin:admin123 http://localhost:8888/demo-client/prod/demo-client
```

**View specific config file:**
```cmd
curl -u admin:admin123 http://localhost:8888/demo-client-dev.yml
```

### 2. Test Config Client Endpoints
If your demo-client has REST endpoints:

```cmd
curl http://localhost:8001/api/config
curl http://localhost:8001/api/message
curl http://localhost:8001/actuator/env
```

### 3. Check Actuator Endpoints
```cmd
curl http://localhost:8001/actuator/health
curl http://localhost:8001/actuator/info
curl http://localhost:8001/actuator/env
```

## Dynamic Configuration Refresh

### Update Configuration Without Restart

1. **Modify config file**:
   Edit `config-service/config/config-repository/dev/demo-client/application.yml`
   ```yaml
   message: "Updated message from Config Server!"
   ```

2. **Save the file** (no git commit needed for native backend)

3. **Refresh client configuration**:
   ```cmd
   curl -X POST http://localhost:8001/actuator/refresh
   ```

4. **Verify the change**:
   ```cmd
   curl http://localhost:8001/api/message
   ```

**Note**: Since we're using native file system backend, changes take effect immediately without git commits.

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

