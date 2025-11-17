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
- Native file system storage
- Basic authentication (admin/admin123)

### 2. Eureka Server (Port: 8761)
- Netflix Eureka Server
- Service Discovery và Registry
- Dashboard: http://localhost:8761
- Load balancing cho các microservices

### 3. Demo Client (Port: Random/8001)
- Eureka client service
- Kết nối Config Server để lấy cấu hình
- REST API endpoints
- Load balanced via Eureka

## Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────────────┐
│                 │         │                  │         │                         │
│  Config Client  │────────▶│  Config Server   │────────▶│  Native File System     │
│  (demo-client)  │         │  (config-service)│         │  (config-repository)    │
│   Port: 8001    │         │   Port: 8888     │         │                         │
│                 │         │                  │         │                         │
└────────┬────────┘         └──────────────────┘         └─────────────────────────┘
         │                           
         │                  ┌──────────────────┐
         │                  │                  │
         └─────────────────▶│  Eureka Server   │
                            │   Port: 8761     │
                            │                  │
                            └──────────────────┘

Service Discovery Flow:
1. Demo Client registers with Eureka Server on startup
2. Eureka Server maintains registry of all service instances
3. Services can discover each other via Eureka
4. Load balancing across multiple instances automatically
```

## Configuration Structure

### Config Server (config-service)
- **Port**: 8888
- **Storage Type**: Native (File System)
- **Repository Location**: `./config/config-repository/`
- **Security**: Basic Auth (admin/admin123)
- **Dependencies**: `spring-cloud-config-server`

### Dynamic Search Locations Pattern
```yaml
search-locations:
  - file:./config/config-repository/common/{label}
  - file:./config/config-repository/{profile}/common/
  - file:./config/config-repository/{profile}/{label}/
```
- `{profile}`: Environment (dev, prod, staging)
- `{label}`: Service name (demo-client, order-service)

### Eureka Server (eureka-server)
- **Port**: 8761
- **Dashboard**: http://localhost:8761
- **Features**:
  - Service registration and discovery
  - Health checking
  - Load balancing
  - Client-side service discovery

### Demo Client (demo-client)
- **Port**: 8001 (configurable)
- **Active Profile**: dev
- **Label**: demo-client
- **Dependencies**:
  - `spring-cloud-starter-config`
  - `spring-cloud-starter-netflix-eureka-client`
- **Features**:
  - Fetches configuration from Config Server
  - Registers with Eureka Server
  - REST endpoints for testing
  - Load balanced calls via RestTemplate

### Config Repository Structure
```
config-repository/
├── dev/
│   ├── common/                       # Shared configs
│   └── demo-client/                  # Service-specific configs
│       ├── application.yml
│       └── demo-client.yml
└── prod/
    ├── common/
    └── demo-client/
        └── demo-client.yml
```

## Build và Package
- **Java**: JDK 21 or higher
- **Maven**: 3.6+

### Build và bỏ qua tests
```cmd
mvnw clean install -DskipTests
```

### Build một service cụ thể
```cmd
mvnw clean install -pl config-service
mvnw clean install -pl eureka-server
mvnw clean install -pl demo-client
```

## Chạy các service

### Thứ tự khởi động:
1. **Config Service** (port 8888)
2. **Eureka Server** (port 8761)
3. **Demo Client** (port 8001)

## Kiểm tra hệ thống

- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **Demo Client Actuator**: http://localhost:8001/actuator
- **Load Balance Test**: http://localhost:8761/api/load-balance/DEMO-CLIENT

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

## Eureka Service Discovery

### Đăng ký service với Eureka

**Thêm dependency trong pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**Cấu hình trong application.yml:**
```yaml
spring:
  application:
    name: your-service-name

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**Enable Eureka Client:**
```java
@SpringBootApplication
@EnableDiscoveryClient
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

### Load Balancing với RestTemplate

```java
@Configuration
public class RestConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Gọi service khác qua Eureka

```java
@Autowired
private RestTemplate restTemplate;

public String callOtherService() {
    // Sử dụng tên service thay vì hostname:port
    return restTemplate.getForObject(
        "http://OTHER-SERVICE/api/endpoint", 
        String.class
    );
}
```

## Testing

### Test Eureka Load Balancing

```cmd
# Xem danh sách services đã đăng ký
curl http://localhost:8761/eureka/apps

# Test load balancing qua Eureka Server
curl http://localhost:8761/api/load-balance/DEMO-CLIENT

# Lấy thông tin instance cụ thể
curl http://localhost:8761/api/instance-info/DEMO-CLIENT
```

### Test Config Server

```cmd
# Lấy config cho demo-client với profile dev
curl -u admin:admin123 http://localhost:8888/demo-client/dev/demo-client

# Refresh config (nếu có thay đổi)
curl -X POST http://localhost:8001/actuator/refresh
```

## Troubleshooting

### Service không đăng ký với Eureka
- Đảm bảo Eureka Server đang chạy trên port 8761
- Kiểm tra cấu hình `eureka.client.service-url.defaultZone`
- Xem logs để tìm lỗi kết nối

### Config Server không load được cấu hình
- Kiểm tra đường dẫn `search-locations` trong application.yml
- Đảm bảo các file cấu hình tồn tại trong config-repository
- Kiểm tra username/password (admin/admin123)

### Build errors
- Chạy `mvnw clean` trước khi build lại
- Đảm bảo Java 21: `java -version`
- Kiểm tra Maven: `mvnw -version`

## License

This project is licensed under the MIT License.


