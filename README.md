# E-Commerce Microservices Project

Dự án E-Commerce với kiến trúc microservices sử dụng Spring Boot và Spring Cloud.

## Cấu trúc dự án

```
e_commerce/
├── .gitignore                 # Git ignore cho toàn project
├── pom.xml                    # Parent POM quản lý tất cả các module
├── mvnw, mvnw.cmd            # Maven wrapper
├── config-service/            # Config Server - Quản lý cấu hình tập trung
├── eureka-server/             # Eureka Server - Service Discovery
└── demo-client/               # Demo Client Service
```

## Công nghệ sử dụng

- **Spring Boot**: 3.5.7
- **Spring Cloud**: 2025.0.0
- **Java**: 21
- **Maven**: Multi-module project

## Các Service

### 1. Config Service (Port: 8888)
- Spring Cloud Config Server
- Quản lý cấu hình tập trung cho các microservices
- Hỗ trợ Native file system và Git repository
- Spring Cloud Bus với RabbitMQ

### 2. Eureka Server (Port: 8761)
- Netflix Eureka Server
- Service Discovery và Registry
- Dashboard: http://localhost:8761

### 3. Demo Client
- Demo service sử dụng Config Server
- Spring Cloud Bus với RabbitMQ
- Actuator endpoints

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

- **Java**: JDK 21 or higher
- **Maven**: 3.6+ (hoặc sử dụng Maven wrapper có sẵn)
- **RabbitMQ**: Cần thiết cho Spring Cloud Bus

### Cài đặt RabbitMQ

**Cách 1: Docker (Khuyến nghị)**
```cmd
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

**Cách 2: Windows Installer**
1. Tải và cài đặt [Erlang](https://www.erlang.org/downloads)
2. Tải và cài đặt [RabbitMQ](https://www.rabbitmq.com/download.html)
3. Khởi động RabbitMQ service

**Kiểm tra RabbitMQ:**
- Management UI: http://localhost:15672
- Default credentials: guest/guest

## Build và Package

### Build tất cả các service từ thư mục gốc
```cmd
mvnw clean install
```

### Build và bỏ qua tests
```cmd
mvnw clean install -DskipTests
```

### Package tất cả các service thành JAR
```cmd
mvnw clean package
```

### Build một service cụ thể
```cmd
# Chỉ build config-service
mvnw clean install -pl config-service

# Chỉ build eureka-server
mvnw clean install -pl eureka-server

# Chỉ build demo-client
mvnw clean install -pl demo-client
```

### Build song song (nhanh hơn)
```cmd
mvnw clean install -T 4
```

## Chạy các service

### Thứ tự khởi động đề nghị:
1. **RabbitMQ** (nếu chưa chạy)
2. **Config Service** (port 8888)
3. **Eureka Server** (port 8761)
4. **Demo Client** và các service khác

### Cách 1: Chạy từng service với Maven

```cmd
# Terminal 1: Config Service
cd config-service
mvnw spring-boot:run

# Terminal 2: Eureka Server
cd eureka-server
mvnw spring-boot:run

# Terminal 3: Demo Client
cd demo-client
mvnw spring-boot:run
```

### Cách 2: Chạy từ JAR file (sau khi build)

```cmd
# Terminal 1
java -jar config-service/target/config-service-0.0.1-SNAPSHOT.jar

# Terminal 2
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar

# Terminal 3
java -jar demo-client/target/demo-client-0.0.1-SNAPSHOT.jar
```

## Kiểm tra hệ thống

- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **Config Server Actuator**: http://localhost:8888/actuator
- **RabbitMQ Management**: http://localhost:15672
- **Demo Client Actuator**: http://localhost:{port}/actuator

## Lệnh Maven hữu ích

```cmd
# Xem dependency tree
mvnw dependency:tree

# Clean tất cả
mvnw clean

# Compile tất cả
mvnw compile

# Run tests cho tất cả modules
mvnw test

# Skip tests khi build
mvnw clean install -DskipTests

# Build parallel (nhanh hơn)
mvnw clean install -T 4

# Chỉ build modules đã thay đổi
mvnw clean install -amd
```

## Development

### Thêm service mới

1. Tạo thư mục service mới trong thư mục gốc
2. Thêm module vào `pom.xml` gốc:
```xml
<modules>
    <module>config-service</module>
    <module>eureka-server</module>
    <module>demo-client</module>
    <module>your-new-service</module>
</modules>
```

3. Tạo `pom.xml` cho service mới với parent:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.quanna</groupId>
        <artifactId>e-commerce-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <artifactId>your-new-service</artifactId>
    <name>Your New Service</name>
    <description>Description of your service</description>
    
    <dependencies>
        <!-- Add your dependencies here -->
    </dependencies>
</project>
```

4. Tạo cấu trúc thư mục:
```
your-new-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/quanna/yourservice/
│   │   │       └── YourServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
└── pom.xml
```

### Đăng ký service với Eureka

Trong `pom.xml` của service mới:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Trong `application.yml`:
```yaml
spring:
  application:
    name: your-service-name

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Trong Application class:
```java
@SpringBootApplication
@EnableDiscoveryClient  // hoặc @EnableEurekaClient
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

## Troubleshooting

### Lỗi build
- Chạy `mvnw clean` trước khi build lại
- Đảm bảo Java 21 đã được cài đặt: `java -version`
- Kiểm tra Maven version: `mvnw -version`

### RabbitMQ connection refused
- Kiểm tra RabbitMQ đang chạy: `docker ps` hoặc kiểm tra Windows Service
- Truy cập RabbitMQ Management UI: http://localhost:15672
- Kiểm tra cấu hình RabbitMQ trong application.yml

### Service không đăng ký với Eureka
- Đảm bảo Eureka Server đang chạy
- Kiểm tra cấu hình eureka.client.service-url.defaultZone
- Xem logs để tìm lỗi kết nối

### Config Server không load được cấu hình
- Kiểm tra đường dẫn search-locations trong application.yml
- Đảm bảo các file cấu hình tồn tại trong config-repository
- Kiểm tra username/password cho Config Server

## Git

File `.gitignore` đã được thiết lập để ignore:
- Thư mục `target/` (build artifacts)
- IDE files (.idea, *.iml, .vscode, etc.)
- Logs và temporary files
- OS specific files (.DS_Store, Thumbs.db)

## License

This project is licensed under the MIT License.

## Contact

Project maintained by: quanna2


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

