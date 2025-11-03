# Quick Start Guide - Spring Cloud Config Example

## What You Have

✅ **Config Server (demo)** - Centralized configuration server on port 8888
✅ **Config Client (demo-client)** - Client application on port 8081
✅ **Git Config Repository** - Configuration files stored in git at `C:\Users\quanna2\config-repo`

## Quick Start (5 Steps)

### 1. Start the Config Server
```cmd
cd C:\Users\quanna2\Downloads\demo\demo
mvnw.cmd spring-boot:run
```
Wait until you see: "Started ConfigServer"

### 2. Start the Config Client (in a new terminal)
```cmd
cd C:\Users\quanna2\Downloads\demo\demo-client
mvnw.cmd spring-boot:run
```
Wait until you see: "Started DemoClientApplication"

### 3. Test the Configuration
Open browser or use curl:
```
http://localhost:8081/api/config
```

You should see all configuration values fetched from the Config Server!

### 4. Try Different Endpoints
```
http://localhost:8081/api/message          - View message property
http://localhost:8081/api/features         - View feature flags
http://localhost:8081/actuator/health      - Health check
```

### 5. Update Configuration Without Restart

Edit file: `C:\Users\quanna2\config-repo\demo-client-dev.properties`

Change:
```properties
message=My new custom message!
```

Commit:
```cmd
cd C:\Users\quanna2\config-repo
git add .
git commit -m "Update message"
```

Refresh (PowerShell):
```powershell
Invoke-WebRequest -Uri http://localhost:8081/actuator/refresh -Method POST
```

Or (curl):
```cmd
curl -X POST http://localhost:8081/actuator/refresh
```

Check the change:
```
http://localhost:8081/api/message
```

## Configuration Files Explained

### Config Repository (C:\Users\quanna2\config-repo\)

**demo-client.properties** (Default - used when no profile specified)
- Basic configuration that all profiles inherit

**demo-client-dev.properties** (Development - currently active)
- Debug features enabled
- Longer timeouts for development
- Beta features turned on

**demo-client-prod.properties** (Production)
- Optimized settings for production
- Debug features disabled
- Stable features only

## Switching Environments

Edit: `demo-client\src\main\resources\application.properties`

Change from:
```properties
spring.profiles.active=dev
```

To:
```properties
spring.profiles.active=prod
```

Then restart demo-client to see production configuration.

## Architecture Overview

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  1. demo-client starts and connects to config-server    │
│                                                          │
└──────────────────┬───────────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  2. config-server authenticates client (admin/admin123) │
│                                                          │
└──────────────────┬───────────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  3. config-server reads from git repository             │
│     Location: C:\Users\quanna2\config-repo               │
│                                                          │
└──────────────────┬───────────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  4. Returns merged configuration:                        │
│     - demo-client.properties (base)                      │
│     - demo-client-dev.properties (profile override)      │
│                                                          │
└──────────────────┬───────────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  5. demo-client uses configuration via @Value and        │
│     @ConfigurationProperties                             │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

## Key Files

### Config Server Files
- `demo\src\main\java\com\quanna\demo\ConfigServer.java` - Main class with @EnableConfigServer
- `demo\src\main\resources\application.properties` - Server configuration
- `demo\pom.xml` - Has spring-cloud-config-server dependency

### Config Client Files
- `demo-client\src\main\java\com\quanna\democlient\DemoClientApplication.java` - Main class
- `demo-client\src\main\java\com\quanna\democlient\controller\ConfigController.java` - REST API
- `demo-client\src\main\java\com\quanna\democlient\config\AppConfig.java` - Configuration properties
- `demo-client\src\main\resources\application.properties` - Client configuration
- `demo-client\pom.xml` - Has spring-cloud-starter-config dependency

## Troubleshooting

**Problem**: Client can't connect to config server
**Solution**: Make sure config server is running first (port 8888)

**Problem**: Configuration not loading
**Solution**: Check that files are committed to git in config-repo

**Problem**: Refresh doesn't work
**Solution**: Make sure you committed changes to git and called /actuator/refresh

**Problem**: 401 Unauthorized
**Solution**: Check username/password in demo-client application.properties

## Next Steps

- Try modifying other properties in the config files
- Create a new profile (e.g., demo-client-test.properties)
- Add more configuration properties to AppConfig.java
- Create additional REST endpoints in ConfigController.java
- Explore Config Server endpoints: http://localhost:8888/demo-client/dev

Enjoy your Spring Cloud Config setup! 🚀

