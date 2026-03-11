# Docker Selenium Grid Setup Documentation

## Overview
This project uses Docker Compose to orchestrate a Selenium Grid infrastructure for distributed automated testing. The setup includes a Selenium Hub, browser nodes (Chrome, Edge, Firefox), video recording services, and a file browser for test artifacts.

## Architecture

### Selenium Hub
- **Image**: `selenium/hub:latest`
- **Container Name**: `selenium-hub`
- **Ports**: 
  - 4442: Event Bus Publish
  - 4443: Event Bus Subscribe
  - 4444: Grid API
- **Configuration**: 
  - Max Sessions: 3 (total across all browsers)
  - Coordinates test distribution across all nodes

### Browser Nodes

#### Chrome Node
- **Image**: `selenium/node-chrome:latest`
- **Platform**: linux/amd64
- **Shared Memory**: 2GB
- **Max Sessions per Node**: 3
- **Dependencies**: Requires selenium-hub to be running first

#### Edge Node
- **Image**: `selenium/node-edge:latest`
- **Platform**: linux/amd64
- **Shared Memory**: 2GB
- **Max Sessions per Node**: 3
- **Dependencies**: Requires selenium-hub to be running first

#### Firefox Node
- **Image**: `selenium/node-firefox:latest`
- **Platform**: linux/amd64
- **Shared Memory**: 2GB
- **Max Sessions per Node**: 3
- **Dependencies**: Requires selenium-hub to be running first

### Video Recording Services
Each browser has an accompanying video service that records test execution:
- **Chrome Video** - Records Chrome browser tests
- **Edge Video** - Records Edge browser tests
- **Firefox Video** - Records Firefox browser tests
- **Image**: `selenium/video:latest`
- **Output**: Videos saved to `./testcaseVideos` directory
- **Format**: Auto-detected by FFmpeg

### File Browser
- **Image**: `filebrowser/filebrowser:latest`
- **Container Name**: `file_browser`
- **Port**: 8081
- **Volume**: `./testcaseVideos:/srv`
- **Access**: http://localhost:8081
- **Features**: Browse and download test videos without authentication

## Getting Started

### Prerequisites
- Docker and Docker Compose installed
- Project directory: `E:\AutoScale\selgrid_kube_aws_oct`
- Minimum 8GB free disk space (for container images and video storage)

### Starting the Grid

**Option 1: Using Batch Script (Windows)**
Double-click or run:
```powershell
cd docker
start-grid.bat
```

**Option 2: Using Docker Compose Directly**
Located in the **docker** folder:
```powershell
cd docker
docker compose -f docker-compose-v3.yml up -d
```

**Expected Output:**
```
[+] up 7/7
 ✔ Container selenium-hub           Created
 ✔ Container docker-chrome-1        Created
 ✔ Container docker-firefox-1       Created
 ✔ Container docker-edge-1          Created
 ✔ Container docker-edge_video-1    Created
 ✔ Container docker-chrome_video-1  Created
 ✔ Container docker-firefox_video-1 Created
```

### Stopping the Grid

**Option 1: Using Batch Script (Windows)**
Double-click or run:
```powershell
cd docker
stop-grid.bat
```

**Option 2: Using Docker Compose Directly**
```powershell
cd docker
docker compose -f docker-compose-v3.yml down
```

**Expected Output:**
```
[+] down 9/9
 ✔ Container docker-edge_video-1    Removed
 ✔ Container file_browser           Removed
 ✔ Container docker-chrome_video-1  Removed
 ✔ Container docker-firefox_video-1 Removed
 ✔ Container docker-chrome-1        Removed
 ✔ Container docker-firefox-1       Removed
 ✔ Container docker-edge-1          Removed
 ✔ Container selenium-hub           Removed
 ✔ Container docker_default         Removed
```

## Running Tests

Navigate to the project root and execute:

```powershell
mvn test
```

Example test output:
```
[INFO] Running TestSuite
Properties:
url=https://practicesoftwaretesting.com/
selenium.gridurl=localhost
Grid Execution
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Troubleshooting

### Issue: Container Name Already in Use

**Error:**
```
Error response from daemon: Conflict. The container name "/file_browser" is already in use by container "..."
```

**Solution:**
```powershell
# Remove the conflicting container
docker rm -f file_browser

# Or remove all containers from the compose setup
docker compose -f docker-compose-v3.yml down
docker compose -f docker-compose-v3.yml up -d
```

### Issue: Multiple Containers Still Running

**Solution:**
```powershell
# Stop all containers
docker compose -f docker-compose-v3.yml down

# Clean up any remaining containers
docker ps -a
docker rm -f <container_id>

# Restart fresh
docker compose -f docker-compose-v3.yml up -d
```

### Issue: Maven Test Fails with POM Error

**Error:**
```
The goal you specified requires a project to execute but there is no POM in this directory
```

**Solution:**
Ensure you're in the project root directory, not in the `docker` folder:
```powershell
cd ..  # Navigate to project root
mvn test
```

## Accessing Services

| Service | URL | Purpose |
|---------|-----|---------|
| Selenium Grid | `http://localhost:4444` | Grid console and API |
| File Browser | `http://localhost:8081` | View test videos |
| Selenium Hub Event Bus | `localhost:4442/4443` | Internal communication |

## Directory Structure

```
project-root/
├── docker/
│   ├── docker-compose-v3.yml      # Main compose configuration
│   ├── start-grid.bat             # Windows batch script to start grid
│   ├── stop-grid.bat              # Windows batch script to stop grid
│   └── testcaseVideos/            # Video recordings directory
├── DOCKER_SETUP.md                # This documentation file
├── src/
│   ├── main/java/
│   │   ├── Base/                  # Base test classes
│   │   ├── pages/                 # Page Object Models
│   │   └── utilities/             # Helper utilities
│   └── test/java/
│       └── tests/                 # Test classes
├── pom.xml                         # Maven configuration
└── configuration.properties        # Test configuration
```

## Environment Variables

The services use the following environment variables:

- `SE_GRID_MAX_SESSION`: Maximum concurrent sessions on hub (set to 3)
- `SE_EVENT_BUS_HOST`: Hub hostname for event communication
- `SE_EVENT_BUS_PUBLISH_PORT`: 4442 (publish events)
- `SE_EVENT_BUS_SUBSCRIBE_PORT`: 4443 (subscribe to events)
- `SE_NODE_MAX_SESSIONS`: Max sessions per node (set to 3)
- `SE_NODE_OVERRIDE_MAX_SESSIONS`: Allow node override (true)
- `DISPLAY_CONTAINER_NAME`: Browser container to record
- `SE_VIDEO_FILE_NAME`: Video filename (auto)
- `FB_NOAUTH`: File browser authentication disabled

## Performance Notes

- Each browser node has 2GB shared memory for memory-intensive operations
- Maximum 3 concurrent sessions total across all browsers
- Video recording may add 10-15% overhead to test execution time
- Ensure sufficient disk space for video files (approximately 50-100MB per test)

## Common Commands Reference

### View running containers
```powershell
docker ps
docker compose -f docker-compose-v3.yml ps
```

### View logs
```powershell
docker logs <container_name>
docker compose -f docker-compose-v3.yml logs
```

### Execute command in container
```powershell
docker exec -it selenium-hub bash
```

### Remove all containers and volumes
```powershell
docker compose -f docker-compose-v3.yml down -v
```

## Notes

- All containers are configured to run on Linux/AMD64 architecture
- The file browser has authentication disabled for convenience (not for production)
- Video recording is automatic when tests run on any browser node
- Shared memory allocation (2GB) is important for browser stability

## Support

For issues or errors:
1. Check container logs: `docker logs <container_name>`
2. Verify all containers are running: `docker compose -f docker-compose-v3.yml ps`
3. Review test output in the console
4. Check video recordings in the file browser for visual debugging
