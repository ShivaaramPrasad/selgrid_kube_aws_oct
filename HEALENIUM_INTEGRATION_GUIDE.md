# Healenium Self-Healing Integration Guide

## Overview

This framework integrates **Healenium** — an open-source self-healing library for Selenium — alongside
our custom self-healing layer. Together, they provide **multi-layer resilience** against test failures
caused by locator changes, timing issues, and infrastructure problems.

## Architecture: Multi-Layer Self-Healing

```
┌─────────────────────────────────────────────────┐
│               Test Layer                        │
│  RetryAnalyzer (2 automatic retries)            │
│  TestListener (screenshot on failure)           │
├─────────────────────────────────────────────────┤
│            Page Object Layer                    │
│  CustomSelfHealingDriver                        │
│  ├── FluentWait with retry                      │
│  ├── Alternate locator strategies               │
│  ├── JavaScript click/sendKeys fallback         │
│  └── Stale element auto-recovery                │
├─────────────────────────────────────────────────┤
│            Driver Layer (Healenium)             │
│  SelfHealingDriver.create(delegate)             │
│  ├── Stores DOM snapshots in PostgreSQL         │
│  ├── Detects broken locators automatically      │
│  ├── Finds best-matching element via ML         │
│  └── Updates locator mapping for future runs    │
├─────────────────────────────────────────────────┤
│            WebDriver (Chrome)                   │
│  Local ChromeDriver / Remote Grid               │
└─────────────────────────────────────────────────┘
```

### How Each Layer Works

| Layer | Handles | Mechanism |
|-------|---------|-----------|
| **Healenium** | Broken locators (DOM changes) | ML-based element matching using stored DOM snapshots |
| **CustomSelfHealingDriver** | Stale elements, timing, intercepted clicks | Retry loops, alt locators, JS fallback |
| **RetryAnalyzer** | Any remaining failures | Re-runs entire test method (up to 2 retries) |
| **TestListener** | Failure diagnostics | Auto-captures screenshot on test failure |

## Prerequisites

- **Docker Desktop** installed and running
- **Java 17+**
- **Maven 3.8+**

## Quick Start

### 1. Start Healenium Backend Services

```batch
cd docker
start-healenium.bat
```

Or manually:
```bash
cd docker
docker-compose -f docker-compose-healenium.yml up -d
```

This starts 3 services:
| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL | 5432 | Stores locator snapshots & healing history |
| Healenium Backend | 7878 | REST API for healing decisions |
| Selector Imitator | 8000 | ML engine for element matching |

### 2. Run Tests

```bash
mvn clean test
```

On first run, Healenium stores DOM snapshots for every element interaction.
On subsequent runs, if a locator breaks, Healenium automatically finds the best match.

### 3. Stop Healenium Backend

```batch
cd docker
stop-healenium.bat
```

## Configuration

### healenium.properties
Located in `src/main/resources/healenium.properties`:

```properties
# Number of recovery attempts
recovery-tries=1

# Minimum similarity score (0.0-1.0)
# Higher = stricter, Lower = more flexible
score-cap=0.5

# Enable/disable globally
heal-enabled=true

# Backend connection
serverHost=localhost
serverPort=7878
```

### Key Settings

| Property | Default | Description |
|----------|---------|-------------|
| `recovery-tries` | 1 | Number of healing attempts per locator failure |
| `score-cap` | 0.5 | Minimum match confidence (0.0=any match, 1.0=exact only) |
| `heal-enabled` | true | Master switch for Healenium |
| `serverHost` | localhost | Healenium backend hostname |
| `serverPort` | 7878 | Healenium backend port |

## How It Works in This Framework

### DriverManager Integration

```java
// In DriverManager.initDriver():
WebDriver delegate = new ChromeDriver(getChromeOptions());
delegate.manage().window().maximize();

// Wrap with Healenium (falls back to plain driver if backend unavailable)
WebDriver healeniumDriver = wrapWithHealenium(delegate);
```

The `wrapWithHealenium()` method:
1. Tries to create a Healenium `SelfHealingDriver`
2. If backend is running → returns Healenium-wrapped driver
3. If backend is unavailable → returns original driver (custom self-healing still works)

### Graceful Fallback

Tests work in **3 modes** without any code changes:

| Mode | Healenium Backend | Custom Self-Healing | Result |
|------|-------------------|---------------------|--------|
| **Full** | Running ✅ | Active ✅ | Both layers heal |
| **Custom Only** | Not running ❌ | Active ✅ | Custom healing only |
| **Basic** | Off | Off | Standard Selenium |

## Docker Services Detail

### docker-compose-healenium.yml

```yaml
services:
  healenium-db:          # PostgreSQL 15
    ports: 5432:5432
    
  healenium-backend:     # REST API
    ports: 7878:7878
    depends_on: healenium-db
    
  healenium-selector-imitator:  # ML matching
    ports: 8000:8000
```

### Verifying Services

```bash
# Check all services are up
docker-compose -f docker/docker-compose-healenium.yml ps

# Check backend health
curl http://localhost:7878/healenium/report

# Check database
docker exec healenium-db psql -U healenium_user -d healenium -c "\dt healenium.*"
```

## Running with Selenium Grid + Healenium

To use both Selenium Grid (Kubernetes) and Healenium together:

1. Start Healenium backend: `docker\start-healenium.bat`
2. Deploy Selenium Grid: `kubectl\start-kubectl-grid.bat`
3. Set `selenium.gridurl=localhost` in `configuration.properties`
4. Run tests: `mvn clean test`

## Troubleshooting

### Healenium backend not connecting
```
[HEALENIUM] Failed to wrap with Healenium (backend may not be running)
[HEALENIUM] Falling back to standard WebDriver
```
**Solution**: Run `docker\start-healenium.bat` and wait 15 seconds for services to initialize.

### Low healing accuracy
If Healenium heals to wrong elements, increase `score-cap` in `healenium.properties`:
```properties
score-cap=0.7  # Stricter matching
```

### Port conflicts
Default ports: 5432 (PostgreSQL), 7878 (Backend), 8000 (Imitator).
If these conflict, update both `docker-compose-healenium.yml` and `healenium.properties`.

### View healing reports
After running tests with Healenium backend active:
```bash
curl http://localhost:7878/healenium/report
```

## File Reference

| File | Purpose |
|------|---------|
| `pom.xml` | Healenium Maven dependency |
| `src/main/resources/healenium.properties` | Healenium configuration |
| `src/main/java/Base/DriverManager.java` | Healenium driver wrapping |
| `src/main/java/utilities/CustomSelfHealingDriver.java` | Custom retry/fallback logic |
| `docker/docker-compose-healenium.yml` | Backend services |
| `docker/start-healenium.bat` | Start backend |
| `docker/stop-healenium.bat` | Stop backend |
