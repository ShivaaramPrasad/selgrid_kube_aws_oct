# Kubernetes Selenium Grid - Complete Setup Guide

**Last Updated:** March 11, 2026  
**Status:** ✅ Production Ready  
**Repository:** https://github.com/ShivaaramPrasad/selgrid_kube_aws_oct

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Step-by-Step Setup](#step-by-step-setup)
3. [Verification Checklist](#verification-checklist)
4. [Running Tests](#running-tests)
5. [Troubleshooting Guide](#troubleshooting-guide)
6. [Useful Commands](#useful-commands)

---

## Prerequisites

### System Requirements
- **OS:** Windows 10/11 or macOS or Linux
- **RAM:** 8GB minimum (16GB recommended)
- **Disk Space:** 20GB free
- **Docker:** Desktop version with Kubernetes enabled
- **Git:** For version control
- **Java:** JDK 11+ for Maven tests
- **Maven:** 3.6+
- **kubectl:** Command-line tool for Kubernetes

### Software Verification

**Check Docker & Kubernetes:**
```powershell
docker --version
docker ps
kubectl cluster-info
kubectl get nodes
```

**Expected Output:**
```
NAME             STATUS   ROLES           AGE   VERSION
docker-desktop   Ready    control-plane   10m   v1.25.4
```

**Check Java & Maven:**
```powershell
java -version
mvn --version
```

---

## Step-by-Step Setup

### Step 1: Clone the Repository

```powershell
git clone https://github.com/ShivaaramPrasad/selgrid_kube_aws_oct.git
cd selgrid_kube_aws_oct
```

### Step 2: Create or Switch to Exercise3 Branch

```powershell
git checkout -b exercise3
# Or if branch exists:
git checkout exercise3
git pull origin exercise3
```

### Step 3: Review Kubernetes Manifests

Ensure these files exist in `kubectl/` directory:

```powershell
ls kubectl/
# Should show:
# - selenium-hub-deployment.yaml
# - selenium-hub-service.yaml
# - selenium-node-chrome-deployment.yaml
```

#### **File 1: selenium-hub-deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: selenium-hub-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: selenium-hub
  template:
    metadata:
      labels:
        app: selenium-hub
    spec:
      containers:
        - name: selenium-hub
          image: selenium/hub:4.17.0-20240123
          ports:
            - containerPort: 4444
          env:
            - name: SE_EVENT_BUS_PUBLISH_PORT
              value: "4442"
            - name: SE_EVENT_BUS_SUBSCRIBE_PORT
              value: "4443"
```

#### **File 2: selenium-hub-service.yaml**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: selenium-hub-service
  labels:
    app: selenium-hub
spec:
  selector:
    app: selenium-hub
  ports:
    - protocol: TCP
      port: 4444
      targetPort: 4444
      name: port0
    - protocol: TCP
      port: 4443
      targetPort: 4443
      name: port1
    - protocol: TCP
      port: 4442
      targetPort: 4442
      name: port2
  type: LoadBalancer
```

#### **File 3: selenium-node-chrome-deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: selenium-node-chrome-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: selenium-node-chrome
  template:
    metadata:
      labels:
        app: selenium-node-chrome
    spec:
      containers:
        - name: selenium-node-chrome
          image: selenium/node-chrome:latest
          env:
            - name: SE_EVENT_BUS_HOST
              value: selenium-hub-service.default.svc.cluster.local
            - name: SE_EVENT_BUS_PUBLISH_PORT
              value: "4442"
            - name: SE_EVENT_BUS_SUBSCRIBE_PORT
              value: "4443"
```

### Step 4: Deploy Selenium Hub

```powershell
cd kubectl
kubectl apply -f selenium-hub-deployment.yaml
```

**Expected Output:**
```
deployment.apps/selenium-hub-deployment created
```

**Verify:**
```powershell
kubectl get pods -l app=selenium-hub
# Wait for READY column to show 1/1
```

### Step 5: Deploy Selenium Hub Service

```powershell
kubectl apply -f selenium-hub-service.yaml
```

**Expected Output:**
```
service/selenium-hub-service created
```

**Verify:**
```powershell
kubectl get svc selenium-hub-service
# Should show LoadBalancer type with EXTERNAL-IP
```

### Step 6: Deploy Chrome Nodes

```powershell
kubectl apply -f selenium-node-chrome-deployment.yaml
```

**Expected Output:**
```
deployment.apps/selenium-node-chrome-deployment created
```

**Verify (wait 20-30 seconds for nodes to start):**
```powershell
kubectl get pods -l app=selenium-node-chrome
# Should show 3/3 with READY 1/1
```

### Step 7: Verify Node Registration

```powershell
kubectl logs -l app=selenium-hub | Select-String "Added node"
```

**Expected Output:**
```
Added node 123e4567-e89b-12d3-a456-426614174000 at http://10.244.1.80:5555
Added node 789a1234-b567-89c1-d234-890123456789 at http://10.244.1.81:5555
Added node abc12345-def6-4789-a123-456789abcdef at http://10.244.1.82:5555
```

### Step 8: Setup Port Forwarding

```powershell
# Kill any existing port-forward processes
Get-Process -Name kubectl -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# Start new port-forward
kubectl port-forward svc/selenium-hub-service 4444:4444
```

**Expected Output:**
```
Forwarding from [::1]:4444 -> 4444
Forwarding from 127.0.0.1:4444 -> 4444
```

**Keep this terminal open!**

### Step 9: Verify Grid UI Access

In a **NEW terminal**, open browser and navigate to:
```
http://localhost:4444
```

**Expected:** Grid console showing 3 Chrome nodes registered and ready

---

## Verification Checklist

Run the following commands to verify complete setup:

```powershell
# 1. Check all pods are running
kubectl get pods
# Expected: 4 pods total - 1 Hub + 3 Nodes, all with READY 1/1

# 2. Check all services
kubectl get svc
# Expected: selenium-hub-service with LoadBalancer type

# 3. Check Hub is running
kubectl get pods -l app=selenium-hub
# Expected: 1/1 Running

# 4. Check Node pods
kubectl get pods -l app=selenium-node-chrome
# Expected: 3/3 with 1/1 Running

# 5. Check node registration
kubectl logs -l app=selenium-hub | Select-String "Added node"
# Expected: 3 nodes added messages

# 6. Test Grid connectivity
curl http://localhost:4444/wd/hub/status
# Expected: JSON response with status: "ok"
```

---

## Running Tests

### Prerequisites for Tests
1. Port-forward is **ACTIVE** on terminal
2. All pods are in **READY 1/1** state
3. Browser can access **http://localhost:4444**

### Configuration File Check

Edit: `src/main/resources/configuration.properties`

```properties
url=https://practicesoftwaretesting.com/
selenium.gridurl=localhost
```

**Important:** 
- `selenium.gridurl=localhost` → Enables Grid execution
- Leave empty to use local ChromeDriver

### Run All Tests

In a **NEW terminal** (port-forward must be active in another terminal):

```powershell
cd e:\AutoScale\selgrid_kube_aws_oct
mvn clean test
```

### Run Specific Test Suite

```powershell
# Run only homepage tests
mvn clean test -Dtest=HomePageTests

# Run only registration tests
mvn clean test -Dtest=RegistrationTests

# Run only filter tests
mvn clean test -Dtest=FilterTests
```

### Run with Specific Configuration

```powershell
# Run with grid mode explicitly
mvn clean test -DgridMode=true

# Run with local browser
mvn clean test -DgridMode=false
```

### Expected Test Output

```
[INFO] Running Home Page Tests
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 45.234 s

[INFO] Running Registration Tests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 30.145 s

[INFO] BUILD SUCCESS
```

---

## Troubleshooting Guide

### Issue 1: Pods Not Starting (Status: Pending)

**Symptom:**
```
NAME                                    READY   STATUS    RESTARTS   AGE
selenium-hub-deployment-xxxxx           0/1     Pending   0          5m
```

**Solution:**

```powershell
# Check resource availability
kubectl describe pod selenium-hub-deployment-xxxxx

# Check node resources
kubectl describe node docker-desktop

# Check for ImagePullBackOff
kubectl get events --sort-by='.lastTimestamp'

# Solution: Delete and redeploy
kubectl delete deployment selenium-hub-deployment
kubectl apply -f selenium-hub-deployment.yaml
```

---

### Issue 2: Nodes Not Registering

**Symptom:** Hub logs show NO "Added node" messages

**Solution:**

```powershell
# 1. Check node logs for errors
kubectl logs -l app=selenium-node-chrome --tail=50

# 2. Verify event bus connectivity
kubectl exec -it <node-pod-name> -- ps aux | grep java

# 3. Check if nodes can reach hub service
kubectl exec -it <node-pod-name> -- nc -zv selenium-hub-service.default.svc.cluster.local 4442

# 4. Restart nodes
kubectl delete pods -l app=selenium-node-chrome
kubectl apply -f selenium-node-chrome-deployment.yaml

# 5. Wait 20-30 seconds for registration
Start-Sleep -Seconds 30
kubectl logs -l app=selenium-hub | Select-String "Added node"
```

---

### Issue 3: Port-Forward Not Working

**Symptom:** Tests can't connect, ERROR `Connection refused`

**Solution:**

```powershell
# 1. Check if port 4444 is in use
netstat -ano | Select-String ":4444"

# 2. Kill existing port-forward
Get-Process kubectl | Stop-Process -Force
Start-Sleep -Seconds 2

# 3. Restart port-forward
kubectl port-forward svc/selenium-hub-service 4444:4444

# 4. Verify in new terminal
curl http://localhost:4444/wd/hub/status
```

---

### Issue 4: Tests Timeout (Session TIMEOUT)

**Symptom:** Hub logs show `reason=TIMEOUT`

**Solution:**

```powershell
# Checklist:
# 1. Port-forward is ACTIVE in separate terminal ✓
# 2. All 3 nodes are READY 1/1 ✓
# 3. configuration.properties has: selenium.gridurl=localhost ✓
# 4. Network connectivity:
kubectl logs -l app=selenium-hub | Select-String "timeout|Timeout" -Context 2

# If issue persists, restart everything:
kubectl delete deployment selenium-node-chrome-deployment
kubectl apply -f selenium-node-chrome-deployment.yaml
Start-Sleep -Seconds 30
mvn clean test
```

---

### Issue 5: ImagePullBackOff (Image Not Found)

**Symptom:** Pod shows `ImagePullBackOff` status

**Solution:**

```powershell
# Check pod events
kubectl describe pod <pod-name>

# For Hub image issue, update version:
# Edit selenium-hub-deployment.yaml
# Change: image: selenium/hub:4.17.0-20240123
# To: image: selenium/hub:4.17.0-20240123 (verified version)

# For Node image, use latest:
# image: selenium/node-chrome:latest

# Redeploy
kubectl apply -f selenium-hub-deployment.yaml
kubectl apply -f selenium-node-chrome-deployment.yaml
```

---

### Issue 6: Test Failures - Connection Refused

**Symptom:**
```
java.net.ConnectException: Connection refused: localhost:4444
```

**Solution:**

```powershell
# 1. Verify Grid UI is accessible
curl http://localhost:4444/wd/hub/status

# 2. Check grid configuration in test code
# File: src/main/java/Base/DriverManager.java
# Verify getGridUrl() method returns: http://localhost:4444

# 3. Check configuration property
type src\main\resources\configuration.properties
# Should show: selenium.gridurl=localhost

# 4. If not accessible, verify port-forward
Get-Process kubectl | Select-Object ProcessName, CommandLine | Where-Object {$_.CommandLine -match "4444"}

# 5. Restart port-forward if needed
Get-Process kubectl -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2
kubectl port-forward svc/selenium-hub-service 4444:4444
```

---

### Issue 7: Maven Build Failures

**Symptom:**
```
[ERROR] BUILD FAILURE - Compilation Errors
```

**Solution:**

```powershell
# 1. Clean Maven cache
mvn clean

# 2. Update dependencies
mvn dependency:resolve

# 3. Rebuild
mvn clean install

# 4. Run tests
mvn test
```

---

### Issue 8: Kubernetes Cluster Issues

**Symptom:** `The connection to the server was refused`

**Solution:**

```powershell
# 1. Check if Kubernetes is running
kubectl cluster-info

# 2. Restart Docker Desktop
# Stop Docker Desktop from system tray > Settings > Kubernetes > Enable Kubernetes
# Or restart Docker Desktop completely

# 3. Verify cluster status
kubectl get nodes
kubectl get namespaces

# 4. Check kubectl context
kubectl config current-context
# Should show: docker-desktop

# 5. Reset if needed
kubectl config use-context docker-desktop
```

---

## Useful Commands

### Monitoring Commands

```powershell
# Watch pod status in real-time
kubectl get pods -w

# Watch all events
kubectl get events --sort-by='.lastTimestamp'

# Monitor resources
kubectl top nodes
kubectl top pods
```

### Logging Commands

```powershell
# Hub logs
kubectl logs -l app=selenium-hub

# Node logs
kubectl logs -l app=selenium-node-chrome

# Logs with filtering
kubectl logs -l app=selenium-hub | Select-String "error|Error|ERROR"

# Follow logs in real-time
kubectl logs -f -l app=selenium-hub

# Get logs from specific pod
kubectl logs <pod-name> --tail=50
```

### Debugging Commands

```powershell
# Describe pod for events and errors
kubectl describe pod <pod-name>

# Exec into pod
kubectl exec -it <pod-name> -- bash

# Check port connectivity
kubectl exec -it <pod-name> -- nc -zv hostname port

# Check DNS resolution
kubectl exec -it <pod-name> -- nslookup selenium-hub-service
```

### Cleanup Commands

```powershell
# Delete specific deployment
kubectl delete deployment selenium-hub-deployment

# Delete all Selenium deployments
kubectl delete deployment -l app=selenium-hub
kubectl delete deployment -l app=selenium-node-chrome

# Delete services
kubectl delete service selenium-hub-service

# Delete all
kubectl delete all -l app=selenium-hub
kubectl delete all -l app=selenium-node-chrome

# Verify cleanup
kubectl get all
```

---

## Quick Start Checklist

Use this checklist for quick setup:

- [ ] Docker Desktop running with Kubernetes enabled
- [ ] Repository cloned: `git clone ...`
- [ ] Branch created/checked out: `git checkout exercise3`
- [ ] YAML files verified in `kubectl/` directory
- [ ] Hub deployment created: `kubectl apply -f selenium-hub-deployment.yaml`
- [ ] Hub service created: `kubectl apply -f selenium-hub-service.yaml`
- [ ] Node deployment created: `kubectl apply -f selenium-node-chrome-deployment.yaml`
- [ ] All pods in READY 1/1 state: `kubectl get pods`
- [ ] Hub has 3 registered nodes: `kubectl logs -l app=selenium-hub | grep "Added node"`
- [ ] Port-forward active: `kubectl port-forward svc/selenium-hub-service 4444:4444`
- [ ] Grid UI accessible: `http://localhost:4444` (shows 3 nodes)
- [ ] Configuration verified: `selenium.gridurl=localhost` in properties
- [ ] Tests running: `mvn clean test`

---

## Emergency Commands

If everything breaks:

```powershell
# Nuclear option - delete everything
kubectl delete all --all

# Redeploy from scratch
kubectl apply -f kubectl/

# Check status
kubectl get pods
kubectl get svc

# Restart port-forward
kubectl port-forward svc/selenium-hub-service 4444:4444
```

---

## Support & Resources

- **Grid Documentation:** https://www.selenium.dev/documentation/grid/
- **Kubernetes Docs:** https://kubernetes.io/docs/
- **Repository:** https://github.com/ShivaaramPrasad/selgrid_kube_aws_oct
- **Wiki:** https://github.com/Raneesh02/selgrid_kube_aws_oct/wiki

---

## Version Information

```
Selenium Hub: 4.17.0-20240123
Chrome Nodes: latest
Kubernetes: Docker Desktop (v1.25+)
Java: 11+
Maven: 3.6+
```

---

**Last Updated:** March 11, 2026  
**Status:** ✅ Tested and Working  
**Author:** Shivaaram Prasad
