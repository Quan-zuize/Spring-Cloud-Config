# E-Commerce Microservices Project

Dự án E-Commerce với kiến trúc microservices sử dụng Spring Boot, Spring Cloud, Eureka, và Config Server.

## Công nghệ sử dụng
- **Java**: 21
- **Spring Boot**: 4.0.0
- **Spring Cloud**: 2025.0.0
- **PostgreSQL**: 15
- **Redis**: 8
- **RabbitMQ**: 4

## Architecture

```
+-------------------+         +-------------------+
|                   |         |                   |
|   API Gateway     +<------->+  Eureka Server    |
| (api-gateway)     |  (Discovery, Routing)      |
+---------+---------+         +---------+---------+
          |                             |
          v                             v
+---------+---------+         +---------+---------+
|                   |         |                   |
|   Demo Client     +<------->+  Config Server    |
| (demo-client)     |  (Config Centralization)    |
+---------+---------+         +---------+---------+
          |                             |
          v                             v
+---------+---------+   +---------------+---------------+
|                   |   |               |               |
|   PostgreSQL      |   |    Redis      |   RabbitMQ    |
| (Database)        |   | (Cache)       | (Message Bus) |
+-------------------+   +---------------+---------------+
```

### Mô tả các thành phần:
- **API Gateway (api-gateway)**: Định tuyến, bảo mật, mock service, load balancing.
- **Eureka Server (eureka-server)**: Service Discovery, quản lý danh sách các service đăng ký.
- **Config Server (config-service)**: Quản lý cấu hình tập trung cho toàn bộ hệ thống.
- **Demo Client (demo-client)**: Service mẫu, đăng ký với Eureka, lấy config từ Config Server, ví dụ cho các service khác.
- **Common Services (common-services)**: Module chứa các service dùng chung (có thể mở rộng thêm redis-service, monitor-service, rabbit-service,...)
- **PostgreSQL**: Lưu trữ dữ liệu chính.
- **Redis**: Cache dữ liệu, hỗ trợ session, rate limit.
- **RabbitMQ**: Message broker cho giao tiếp bất đồng bộ.

## Các Services
| Service           | Mô tả                                      | Port   |
|-------------------|--------------------------------------------|--------|
| config-service    | Spring Cloud Config Server                 | 8888   |
| eureka-server     | Eureka Service Discovery                   | 8761   |
| demo-client       | Service mẫu, đăng ký với Eureka            | 8001+  |
| api-gateway       | Định tuyến request (mock)                  | 8080   |
| common-services   | Module chứa các service dùng chung         | -      |
| PostgreSQL        | Database                                  | 5432   |
| Redis            | Cache                                      | 6379   |
| RabbitMQ         | Message broker                             | 5672/15672 |

## Cấu trúc dự án
```
e_commerce/
├── pom.xml                # Parent POM
├── docker-compose.yml     # Chạy các service phụ trợ
├── config-service/        # Config Server
├── eureka-server/         # Eureka Service Discovery
├── demo-client/           # Service mẫu
├── api-gateway/           # API Gateway (mock)
├── common-services/       # Common service module
```

## Khởi động nhanh
1. Khởi động các service phụ trợ:
   ```sh
   docker-compose up -d
   ```
2. Build project:
   ```sh
   ./mvnw clean install -DskipTests
   ```
3. Chạy lần lượt:
   - config-service (8888)
   - eureka-server (8761)
   - demo-client (8001 hoặc random)
   - api-gateway (nếu có)

## Cấu hình Config Server
- Lưu trữ native: `./config/config-repository/`
- Pattern search-locations:
  ```yaml
  spring:
    cloud:
      config:
        server:
          native:
            search-locations:
              - file:./config/config-repository/common/{label}
              - file:./config/config-repository/{profile}/common/
              - file:./config/config-repository/{profile}/{label}/
  ```
- Cấu hình dùng chung để trong `common/`, cấu hình riêng từng service để trong `{label}/`

## Một số lệnh Maven hữu ích
```sh
./mvnw clean install -DskipTests
./mvnw clean install -pl config-service
```

## Truy cập nhanh
- Eureka Dashboard: http://localhost:8761
- Config Server: http://localhost:8888
- Demo Client Actuator: http://localhost:8001/actuator

## License
MIT License
