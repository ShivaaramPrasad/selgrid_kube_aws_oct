# Kubernetes Selenium Grid - Quick Reference

## 🚀 Quick Start (5 minutes)

### 1. Deploy Everything
```powershell
cd kubectl
kubectl apply -f selenium-hub-deployment.yaml
kubectl apply -f selenium-hub-service.yaml
kubectl apply -f selenium-node-chrome-deployment.yaml
```

### 2. Wait for Nodes to Register
```powershell
Start-Sleep -Seconds 30
kubectl get pods
```

### 3. Start Port-Forward (in separate terminal)
```powershell
kubectl port-forward svc/selenium-hub-service 4444:4444
```

### 4. Run Tests (in another terminal)
```powershell
mvn clean test
```

---

## 📋 Daily Operations

### Check Grid Status
```powershell
kubectl get pods
kubectl get svc
kubectl logs -l app=selenium-hub | Select-String "Added node"
```

### View Grid UI
```
http://localhost:4444
```

### Run Tests
```powershell
# All tests
mvn clean test

# Specific test
mvn clean test -Dtest=HomePageTests

# Specific suite
mvn test -Dsuites=src/test/java/Base/suites/homepagetests.xml
```

### Monitor Logs
```powershell
# Hub logs
kubectl logs -f -l app=selenium-hub

# Node logs
kubectl logs -f -l app=selenium-node-chrome

# Specific pod
kubectl logs -f selenium-hub-deployment-xxxxx
```

---

## 🔧 Common Fixes

### Port-Forward Not Working
```powershell
Get-Process kubectl | Stop-Process -Force
Start-Sleep 2
kubectl port-forward svc/selenium-hub-service 4444:4444
```

### Nodes Not Registering
```powershell
kubectl delete pods -l app=selenium-node-chrome
Start-Sleep 30
kubectl logs -l app=selenium-hub | Select-String "Added node"
```

### Tests Timeout
```powershell
# 1. Verify port-forward is running
# 2. Check pods: kubectl get pods (should all show READY 1/1)
# 3. Check hub: kubectl logs -l app=selenium-hub | tail -50
# 4. Restart everything:
kubectl delete all -l app=selenium-hub
kubectl delete all -l app=selenium-node-chrome
# Then redeploy step 1
```

### Connection Refused
```powershell
# Verify Grid is accessible
curl http://localhost:4444/wd/hub/status

# If not, disable port-forward and restart
Get-Process kubectl -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep 2
kubectl port-forward svc/selenium-hub-service 4444:4444
```

---

## 📊 Status Commands

```powershell
# All pods
kubectl get pods

# Specific app
kubectl get pods -l app=selenium-hub
kubectl get pods -l app=selenium-node-chrome

# Services
kubectl get svc

# Events (last 10)
kubectl get events --sort-by='.lastTimestamp' -n default | tail -10

# Pod details
kubectl describe pod <pod-name>

# Node registration
kubectl logs -l app=selenium-hub | grep "Added node"

# Test execution
kubectl logs -l app=selenium-hub | grep "Session \|session "
```

---

## 🧹 Cleanup Commands

```powershell
# Delete specific deployment
kubectl delete deployment selenium-hub-deployment

# Delete all selenium components
kubectl delete all -l app=selenium-hub
kubectl delete all -l app=selenium-node-chrome

# Delete specific service
kubectl delete svc selenium-hub-service

# Delete everything (nuclear option)
kubectl delete all --all
```

---

## ⚙️ Configuration Files

### Hub Configuration
**File:** `kubectl/selenium-hub-deployment.yaml`
- Image: `selenium/hub:4.17.0-20240123`
- Replicas: 1
- Port: 4444

### Node Configuration
**File:** `kubectl/selenium-node-chrome-deployment.yaml`
- Image: `selenium/node-chrome:latest`
- Replicas: 3
- Port: 5555

### Test Configuration
**File:** `src/main/resources/configuration.properties`
- `selenium.gridurl=localhost` → Enable Grid
- `selenium.gridurl=` (empty) → Use local browser

---

## 📈 Expected Behavior

### Pod Lifecycle
1. **Deploying** (0-30s) - Pod starting
2. **Running** (30s+) - Container running
3. **Ready** (60s+) - Pod ready for traffic

### Node Registration Timeline
```
0s:   Pod starts
5s:   Container runs
10s:  Event bus connects
15s:  Registration event sent
20s:  Hub receives registration
25s:  Node appears in grid
```

### Test Execution Flow
```
1. Test connects to localhost:4444
2. Hub receives connection request
3. Hub finds available node
4. Node starts browser
5. Test runs in browser
6. Node stops browser
7. Session ends
```

---

## 🚨 Red Flags

| Symptom | Cause | Fix |
|---------|-------|-----|
| Pods stuck in Pending | Insufficient resources | Scale down replicas or increase Docker memory |
| ImagePullBackOff | Image not found | Use correct image version |
| No "Added node" logs | Service/networking issue | Restart nodes, check DNS |
| Connection refused | Port-forward not active | Restart port-forward |
| Session timeout | Long running test | Increase timeout in test code |
| 0% ready nodes | Hub/node version mismatch | Use compatible versions |

---

## 💡 Tips & Tricks

### Useful Aliases
```powershell
# Add to PowerShell profile
Set-Alias -Name kgp -Value 'kubectl get pods'
Set-Alias -Name kgs -Value 'kubectl get svc'
Set-Alias -Name kl -Value 'kubectl logs -l app=selenium-hub'
Set-Alias -Name kpf -Value 'kubectl port-forward svc/selenium-hub-service 4444:4444'
```

### Watch Pod Updates
```powershell
kubectl get pods -w  # Real-time pod status updates
```

### Get Pod Names Quickly
```powershell
# Hub pod
$HUB = kubectl get pods -l app=selenium-hub -o jsonpath='{.items[0].metadata.name}'
echo $HUB

# All nodes
kubectl get pods -l app=selenium-node-chrome -o name
```

### Copy Pods Between Environments
```powershell
# Get pod logs
kubectl logs <pod-name> > pod-logs.txt

# Describe pod
kubectl describe pod <pod-name> > pod-info.txt
```

---

## 📞 Getting Help

### Check Logs for Errors
```powershell
kubectl logs -l app=selenium-hub | Select-String "error|ERROR|Error|Exception"
kubectl logs -l app=selenium-node-chrome | Select-String "error|ERROR|Error|Exception"
```

### Describe Pod for Events
```powershell
kubectl describe pod <pod-name>
# Shows: Status, Events, Containers, Mounts
```

### Get Full Event Timeline
```powershell
kubectl get events --sort-by='.lastTimestamp'
```

### Test Grid Directly
```powershell
curl http://localhost:4444/wd/hub/status
# Expected: {"value":{"ready":true,"message":"Selenium Grid ready.","nodes":3}}
```

---

## 📝 Important URLs

| Component | URL | Purpose |
|-----------|-----|---------|
| Hub Console | http://localhost:4444 | View grid status |
| Hub Web UI | http://localhost:4444/ui | Dashboard with nodes |
| Health Check | http://localhost:4444/wd/hub/status | Check grid status |
| New Session | http://localhost:4444/wd/hub/session | Create new session |

---

## Version Matrix

| Component | Version | Status |
|-----------|---------|--------|
| Selenium Hub | 4.17.0-20240123 | ✅ Verified |
| Chrome Nodes | latest | ✅ Current |
| Kubernetes | 1.25+ | ✅ Tested |
| Java | 11+ | ✅ Required |
| Maven | 3.6+ | ✅ Required |

---

## 🔐 Best Practices

1. **Always use port-forward** for local Kubernetes
2. **Keep replicas at 3** for parallel testing
3. **Use specific hub image versions** (avoid latest for hub)
4. **Set proper timeouts** in test code (300s default)
5. **Monitor logs regularly** during test execution
6. **Clean up resources** after testing is done

---

**Quick Access:** 
- 📖 Full Guide: [KUBERNETES_SELENIUM_SETUP_GUIDE.md](KUBERNETES_SELENIUM_SETUP_GUIDE.md)
- 📦 Repository: https://github.com/ShivaaramPrasad/selgrid_kube_aws_oct
- 🎓 Wiki: https://github.com/Raneesh02/selgrid_kube_aws_oct/wiki
