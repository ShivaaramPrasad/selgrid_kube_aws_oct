# Kubernetes Batch Files Guide

## Overview
This guide explains the batch files created for managing the Kubernetes Selenium Grid infrastructure. These scripts provide easy-to-use commands for starting, stopping, and monitoring the grid.

## Batch Files

### 1. `kubectl-menu.bat` - Interactive Menu
**Purpose:** Main menu for managing all Kubernetes grid operations

**Usage:**
```batch
kubectl-menu.bat
```
(Run from the `kubectl` folder)

**Features:**
- Start/Stop Grid
- Check Status
- View Logs (Hub & Nodes)
- Port Forward
- Access Grid UI
- Run Tests
- Cleanup Resources

---

### 2. `start-kubectl-grid.bat` - Start Grid
**Purpose:** Deploy Selenium Hub and Chrome Nodes to Kubernetes

**Usage:**
```batch
start-kubectl-grid.bat
```
(Run from the `kubectl` folder)

**What it does:**
1. Applies Selenium Hub Service and Deployment
2. Applies Selenium Node Chrome Deployment
3. Waits for all pods to be ready
4. Displays pod status
5. Shows next steps

**Next Steps after starting:**
```bash
# Terminal 1: Port Forward (keep this open)
kubectl port-forward svc/selenium-hub-service 4444:4444

# Terminal 2: Run Tests
mvn clean test

# Access Grid UI
http://localhost:4444
```

---

### 3. `stop-kubectl-grid.bat` - Stop Grid
**Purpose:** Remove Selenium Hub and Chrome Nodes from Kubernetes

**Usage:**
```batch
stop-kubectl-grid.bat
```
(Run from the `kubectl` folder)

**What it does:**
1. Deletes Node deployment
2. Deletes Hub deployment
3. Deletes Hub service
4. Displays final resource status

**Warning:** Stops all running tests and removes containers

---

### 4. `kubectl-status.bat` - Check Status
**Purpose:** Display current status of the Kubernetes Grid

**Usage:**
```batch
kubectl-status.bat
```
(Run from the `kubectl` folder)

**Information displayed:**
- Pod status (Running, Pending, etc.)
- Deployment status
- Service status
- Recent logs from Hub and Nodes
- Port 4444 forwarding status
- Quick reference commands

---

### 5. `kubectl-port-forward.bat` - Port Forward
**Purpose:** Establish port-forward tunnel from localhost:4444 to Hub

**Usage:**
```batch
kubectl-port-forward.bat
```
(Run from the `kubectl` folder)

**Notes:**
- Keep this terminal open while running tests
- Run in a separate terminal from test execution
- Press Ctrl+C to stop

**Why needed:**
- Kubernetes services are internal to the cluster
- Port-forward creates a local tunnel
- Allows tests to connect to http://localhost:4444

---

## Quick Start Workflow

### Option 1: Using Menu (Easiest)
From the `kubectl` folder:
```batch
kubectl-menu.bat
```
Then select options from the interactive menu.

### Option 2: Using Individual Scripts (Manual)

**Terminal 1 - Start Grid:**
```batch
start-kubectl-grid.bat
```

**Terminal 2 - Port Forward:**
```batch
kubectl-port-forward.bat
```

**Terminal 3 - Run Tests:**
```batch
mvn clean test
```

**Stop Grid (Any Terminal):**
```batch
stop-kubectl-grid.bat
```

---

## Common Tasks

### Check if Grid is Running
```batch
kubectl-status.bat
```

### View Hub Logs in Real-time
```batch
kubectl logs -l app=selenium-hub -f
```

### View Node Logs in Real-time
```batch
kubectl logs -l app=selenium-node-chrome -f
```

### Access Grid UI
```
http://localhost:4444
```

### Run Tests Against Grid
```batch
mvn clean test
```

### Setup Port Forward Manually
```batch
kubectl port-forward svc/selenium-hub-service 4444:4444
```

Or use the batch file:
```batch
kubectl-port-forward.bat
```

### Check Node Registration
```batch
kubectl logs -l app=selenium-hub | findstr "Added node"
```

---

## Troubleshooting

### Port 4444 Already in Use
```batch
REM Kill existing port-forward
taskkill /F /IM kubectl.exe

REM Wait and try again
timeout /t 2
kubectl-port-forward.bat
```

### Pods Not Starting
```batch
REM Check pod status
kubectl get pods

REM View pod details
kubectl describe pods

REM View pod logs
kubectl logs <pod-name>
```

### Grid Unresponsive
```batch
REM Restart grid
stop-kubectl-grid.bat
timeout /t 5
start-kubectl-grid.bat
```

### Service Not Found
```batch
REM Ensure service exists
kubectl get svc

REM If not, recreate from YAML
kubectl apply -f kubectl/selenium-hub-service.yaml
```

---

## File Locations

All batch files and Kubernetes manifests are located in the `kubectl/` directory:
```
kubectl/
├── kubectl-menu.bat                    (Main interactive menu)
├── start-kubectl-grid.bat              (Start deployments)
├── stop-kubectl-grid.bat               (Stop deployments)
├── kubectl-status.bat                  (Check status)
├── kubectl-port-forward.bat            (Setup port forwarding)
├── KUBECTL_BATCH_FILES_GUIDE.md        (This guide)
├── selenium-hub-deployment.yaml        (Hub configuration)
├── selenium-hub-service.yaml           (Service configuration)
└── selenium-node-chrome-deployment.yaml (Node configuration)
```

---

## Environment Requirements

- Windows PowerShell or CMD
- Docker Desktop with Kubernetes enabled
- kubectl CLI installed
- Maven (for running tests)
- Java 17+ installed

---

## Additional Notes

- **Grid is network-isolated:** All pods run in Kubernetes cluster network
- **Port-forward required:** Must be active to access from localhost
- **One service only:** Only one port-forward per terminal
- **Pod restart:** Deleting/recreating deployments generates new pods
- **Logs persist:** Check logs before deleting for debugging

---

## See Also

- [KUBERNETES_SELENIUM_SETUP_GUIDE.md](../KUBERNETES_SELENIUM_SETUP_GUIDE.md) - Detailed setup guide
- [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) - Quick reference commands
- Kubernetes Manifests: `selenium-hub-service.yaml`, `selenium-hub-deployment.yaml`, `selenium-node-chrome-deployment.yaml`
