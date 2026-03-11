# Batch Scripts Guide

## Overview
This directory contains Windows batch scripts for simplified management of the Selenium Grid. These scripts provide an easy way to start, stop, scale, and manage Docker containers without needing to type complex Docker commands.

## Available Scripts

### 1. **start-grid.bat** - Basic Grid Startup
**Purpose**: Start the Selenium Grid infrastructure with default configuration

**Features**:
- ✅ Docker availability check
- ✅ Compose file validation
- ✅ Automatic container startup
- ✅ Error handling and reporting
- ✅ Health check notifications

**Usage**:
```powershell
# Double-click in Windows Explorer
start-grid.bat

# Or from command line
.\start-grid.bat
```

**What it does**:
1. Checks if Docker is running
2. Verifies docker-compose-v3.yml exists
3. Starts all containers (1 Chrome, 1 Edge, 1 Firefox)
4. Provides access links

**Output**:
```
[SUCCESS] Grid Started Successfully!

Services:
  - Selenium Grid: http://localhost:4444
  - File Browser: http://localhost:8081 (when video enabled)
```

---

### 2. **stop-grid.bat** - Grid Shutdown
**Purpose**: Stop all Selenium Grid containers and cleanup

**Features**:
- ✅ Docker status verification
- ✅ Graceful container shutdown
- ✅ Error handling
- ✅ Cleanup suggestions

**Usage**:
```powershell
.\stop-grid.bat
```

**What it does**:
1. Verifies Docker is running
2. Stops all containers
3. Removes containers and networks
4. Provides cleanup tips

**Cleanup Tip**:
To also remove volumes:
```powershell
docker compose -f docker-compose-v3.yml down -v
```

---

### 3. **start-grid-video.bat** - Grid with Video Recording
**Purpose**: Start grid with video recording capabilities enabled

**Features**:
- ✅ Video recording services enabled
- ✅ Auto-creates `testcaseVideos` directory
- ✅ File browser automatically started
- ✅ Full error handling

**Usage**:
```powershell
.\start-grid-video.bat
```

**What it does**:
1. Performs Docker validation
2. Creates `testcaseVideos` directory
3. Starts grid WITH video profile
4. Enables file browser service
5. Provides access links

**Output**:
```
[SUCCESS] Grid Started with Video Recording!

Services:
  - Selenium Grid: http://localhost:4444
  - File Browser: http://localhost:8081

Video Recording:
  - Videos will be saved to: testcaseVideos/
  - Access via File Browser at http://localhost:8081
```

**Access Recordings**:
- Open browser: `http://localhost:8081`
- Browse recorded test videos
- Download for analysis

---

### 4. **start-grid-scaled.bat** - Scaled Grid Startup
**Purpose**: Start Selenium Grid with multiple browser instances (scaling)

**Features**:
- ✅ Interactive configuration menu
- ✅ Pre-configured scaling presets
- ✅ Custom scaling option
- ✅ Real-time feedback

**Usage**:
```powershell
.\start-grid-scaled.bat
```

**Interactive Menu**:
```
Scaling Configuration:
===================================
1. 3 Chrome instances (default)
2. 2 Chrome + 2 Edge instances
3. 2 Chrome + 2 Edge + 2 Firefox (custom)
4. Enter custom configuration

Select configuration (1-4): 
```

**Configuration Options**:

**Option 1: 3 Chrome Instances**
- Starts with 3 Chrome browser instances
- Best for Chrome-only testing
- Efficient resource usage

**Option 2: 2 Chrome + 2 Edge**
- 2 Chrome instances
- 2 Edge instances
- Cross-browser parallelization

**Option 3: 2 Chrome + 2 Edge + 2 Firefox**
- Balanced multi-browser setup
- High parallelization
- Maximum coverage

**Option 4: Custom Configuration**
- Enter custom numbers for each browser:
  ```
  Number of Chrome instances: 3
  Number of Edge instances: 2
  Number of Firefox instances: 1
  ```

**Maximum Limits**:
- Hub max sessions: 5 (total across all browsers)
- Per node max: 3 sessions
- Scale strategically based on available system resources

**Verify Scaling**:
```powershell
docker compose -f docker-compose-v3.yml ps
```

---

### 5. **grid-menu.bat** - Interactive Management Console
**Purpose**: Comprehensive menu-driven grid management interface

**Features**:
- ✅ Full grid lifecycle management
- ✅ Interactive menu system
- ✅ Direct console access
- ✅ Video browser access
- ✅ Container inspection
- ✅ Safe cleanup options

**Usage**:
```powershell
.\grid-menu.bat
```

**Main Menu**:
```
======================================
  SELENIUM GRID MANAGEMENT CONSOLE
======================================

Select an option:

  1. Start Grid (Basic)
  2. Start Grid with Video Recording
  3. Start Grid (Scaled)
  4. Stop Grid
  5. View Running Containers
  6. View Grid Console
  7. View Test Videos
  8. Clean Up (Remove all containers)
  9. Exit
```

**Option Details**:

| Option | Function | Description |
|--------|----------|-------------|
| 1 | Start Grid | Basic startup (1 of each browser) |
| 2 | Start Video | Grid with video recording enabled |
| 3 | Scale Grid | Interactive scaling configuration |
| 4 | Stop Grid | Shutdown all services |
| 5 | View Containers | List running containers |
| 6 | Grid Console | Opens http://localhost:4444 in browser |
| 7 | View Videos | Opens file browser (http://localhost:8081) |
| 8 | Cleanup | Remove all containers and networks |
| 9 | Exit | Close menu and exit |

**Workflow Example**:
```
1. Start Grid Menu
2. Select Option 2 (Start with Video)
3. Run tests
4. Select Option 7 (View Videos)
5. Review test recordings
6. Select Option 4 (Stop Grid)
7. Select Option 9 (Exit)
```

---

## Common Workflows

### Workflow 1: Basic Testing
```powershell
# Start grid
.\start-grid.bat

# Run tests (in another terminal)
cd ..
mvn test

# Stop grid
.\stop-grid.bat
```

### Workflow 2: Testing with Video Recording
```powershell
# Start with video
.\start-grid-video.bat

# Run tests
cd ..
mvn test

# View videos
# Open: http://localhost:8081

# Stop grid
.\stop-grid.bat
```

### Workflow 3: Parallel Test Execution
```powershell
# Start scaled grid
.\start-grid-scaled.bat
# Select: Option 1 (3 Chrome instances)

# Run tests (will distribute across instances)
cd ..
mvn test

# Stop grid
.\stop-grid.bat
```

### Workflow 4: Full Management Experience
```powershell
# Use interactive menu
.\grid-menu.bat

# Navigate through options:
# 1. Start Grid with Video
# 2. View containers (Option 5)
# 3. Run tests
# 4. View Grid Console (Option 6)
# 5. View test videos (Option 7)
# 6. Cleanup (Option 8)
```

---

## Troubleshooting

### Issue: "Docker is not running"
**Solution**:
- Start Docker Desktop
- Wait for it to fully initialize
- Try again

### Issue: "docker-compose-v3.yml not found"
**Solution**:
- Ensure you're in the `docker` directory
- Verify the file exists: `dir docker-compose-v3.yml`

### Issue: "Port 4444 already in use"
**Solution**:
```powershell
# Stop any existing containers
.\stop-grid.bat

# Or manually:
docker ps
docker stop <container_id>
```

### Issue: "Permission denied" errors
**Solution**:
- Run Command Prompt as Administrator
- Or use PowerShell with admin rights

### Issue: Video recording not working
**Solution**:
- Use `start-grid-video.bat` instead of `start-grid.bat`
- Ensure `testcaseVideos` directory exists
- Check disk space availability

---

## Advanced Usage

### Manual Scaling Commands
If you prefer command line over batch scripts:

```powershell
# Start and scale Chrome to 3 instances
docker compose -f docker-compose-v3.yml up --scale chrome=3 -d

# Scale multiple browsers
docker compose -f docker-compose-v3.yml up --scale chrome=3 --scale edge=2 --scale firefox=2 -d

# View running instances
docker compose -f docker-compose-v3.yml ps

# Check logs
docker compose -f docker-compose-v3.yml logs -f
```

### Environment Variables
Configure grid behavior by setting environment variables before startup:

```powershell
# Set max sessions
set SE_GRID_MAX_SESSION=10
.\start-grid.bat

# Set timeout (in seconds)
set SE_GRID_BROWSER_TIMEOUT=300
.\start-grid.bat
```

### Docker Compose Profiles
Access grid services with or without video:

```powershell
# Basic (no video)
docker compose -f docker-compose-v3.yml up -d

# With video
docker compose --profile video -f docker-compose-v3.yml up -d

# List containers
docker compose -f docker-compose-v3.yml ps
```

---

## Best Practices

### Resource Management
- ✅ Monitor system resources when scaling
- ✅ Each browser instance requires ~500MB RAM
- ✅ Video recording adds ~10-15% overhead
- ✅ Allocate 2GB shared memory per browser node

### Performance Tips
- ✅ Use basic grid for simple tests
- ✅ Enable video only when needed
- ✅ Scale gradually based on test requirements
- ✅ Monitor with `docker stats` during runs

### Safety
- ✅ Always use `stop-grid.bat` before shutting down
- ✅ Use cleanup option cautiously (removes volumes)
- ✅ Run interactive script (`grid-menu.bat`) for guidance
- ✅ Keep backups of test artifacts before cleanup

---

## Docker Commands Reference

Common Docker commands used by these scripts:

```powershell
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# View container logs
docker logs <container_name>

# Stop specific container
docker stop <container_name>

# Remove specific container
docker rm <container_name>

# Check resource usage
docker stats

# Clean up resources
docker system prune
```

---

## Need Help?

### Quick Start
```powershell
# New to this? Start here:
.\grid-menu.bat
```

### File Reference
- **docker-compose-v3.yml** - Main configuration file
- **DOCKER_SETUP.md** - Detailed Docker setup guide
- **SCALING_GUIDE.md** - Scaling strategies documentation
- **Batch Scripts Guide** - This file

### Support Resources
- Grid Console: http://localhost:4444
- File Browser: http://localhost:8081
- Docker Documentation: https://docs.docker.com
- Selenium Grid Documentation: https://www.selenium.dev/documentation/grid/

---

## Script Compatibility

| Feature | Windows 10+ | Windows 7/8 |
|---------|-----------|-----------|
| Basic scripts | ✅ | ✅ |
| Interactive menus | ✅ | ⚠️ Limited |
| Error handling | ✅ | ✅ |
| Features | Full | Basic |

---

## Version History

**v1.0 (March 11, 2026)**
- ✅ Added basic start/stop scripts
- ✅ Added video recording script
- ✅ Added scaled grid script
- ✅ Added interactive menu console
- ✅ Added comprehensive documentation
