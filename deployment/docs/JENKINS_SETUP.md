# Jenkins Setup Guide for Order Management API

## 📋 Requirements Checklist

Before running this pipeline, ensure ALL prerequisites are met:

### ✅ Jenkins Agent Requirements

- [ ] **Docker installed** on Jenkins agent
- [ ] **Maven 3.9** configured in Jenkins
- [ ] **Git plugin** installed
- [ ] **Pipeline plugin** installed
- [ ] **Disk space**: At least 10GB free
- [ ] **Memory**: At least 2GB RAM (4GB recommended)

### ✅ Jenkins Credentials Required

Configure in Jenkins → Manage Jenkins → Credentials:

| Credential ID | Type | Description | Example |
|---------------|------|-------------|---------|
| `docker-hub` | Username + Password | Docker Hub credentials | `username:password` |
| `HEROKU_API_KEY` | Secret text | Heroku API key | `your-heroku-api-key` |
| `db-url` | Secret text | Database JDBC URL | `jdbc:mysql://host:3306/db` |
| `db-username` | Secret text | Database username | `admin` |
| `db-password` | Secret text | Database password | `secret123` |
| `db-driver` | Secret text | Database driver | `com.mysql.cj.jdbc.Driver` |
| `discord-notification` | Secret text | Discord webhook (optional) | `https://discord.com/api/webhooks/...` |

---

## 🔧 Step 1: Install Docker on Jenkins Agent

### Option A: If You Have SSH Access

```bash
# SSH to Jenkins agent
ssh jenkins-agent

# Install Docker
curl -fsSL https://get.docker.com | sh

# Verify installation
docker --version

# Test Docker
docker run hello-world
```

### Option B: If Using Kubernetes/Docker

Update Jenkins agent deployment to include Docker:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: jenkins-agent
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  - name: docker
    image: docker:latest
    command:
    - /bin/sh
    - -c
    - sleep 9999999
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
```

### Option C: Use DinD (Docker-in-Docker)

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: jenkins-agent-dind
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    env:
    - name: DOCKER_HOST
      value: tcp://localhost:2375
  - name: dind
    image: docker:stable-dind
    securityContext:
      privileged: true
    volumeMounts:
    - name: docker-storage
      mountPath: /var/lib/docker
  volumes:
  - name: docker-storage
    emptyDir: {}
```

---

## 🔧 Step 2: Configure Jenkins Tools

### Configure Maven

1. Go to **Manage Jenkins → Global Tool Configuration**
2. Scroll to **Maven**
3. Click **Maven Installations...**
4. Add:
   - Name: `Maven 3.9`
   - Version: `3.9.9`
   - Save

---

## 🔧 Step 3: Create Jenkins Pipeline Job

### Option A: Using Jenkinsfile from Repository

1. Go to Jenkins → **New Item**
2. Name: `edts-week8-springboot`
3. Select **Pipeline**
4. Configure:
   - **Pipeline** → **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/mdafaardiansyah/mini-ecommerce-springboot-week8.git`
   - **Script Path**: `Jenkinsfile`
   - **Branches to build**: `**` (all branches)
5. Save

### Option B: Multibranch Pipeline (Recommended)

1. Go to Jenkins → **New Item**
2. Name: `edts-week8-springboot-multipipeline`
3. Select **Multibranch Pipeline**
4. Configure:
   - **Branch Sources** → **Add source** → **Git**
   - **Repository URL**: `https://github.com/mdafaardiansyah/mini-ecommerce-springboot-week8.git`
   - **Build Configuration**: by Jenkinsfile
5. Save

This will automatically create jobs for `master` and `develop` branches.

---

## 🔧 Step 4: Configure Webhooks (Auto-deploy on push)

### In GitHub:

1. Go to **Repository Settings → Webhooks → Add webhook**
2. **Payload URL**: `http://your-jenkins/github-webhook/`
3. **Content type**: `application/json`
4. **Secret**: (optional) webhook secret
5. **Events**: Select "Just the push event"
6. Add webhook

### In Jenkins:

1. Go to **Configure → Build Triggers**
2. ✅ **GitHub hook trigger for GITScm polling**

---

## 🚀 Step 5: Test the Pipeline

### Test Build:

```bash
# In Jenkins UI, click "Build Now"
```

### Expected Flow:

```
1. Checkout → 30 sec
2. Build with Tests → 5-10 min (tests MUST pass)
3. Verify Tests → 10 sec (fails if tests failed)
4. Verify Docker → 5 sec
5. Build Docker Image → 2-3 min
6. Push to Docker Hub → 1-2 min
7. Deploy to Heroku → 3-5 min
8. Health Check → 1-2 min

Total: ~15-25 minutes
```

---

## 🎯 Deployment Rules

### Automatic Deployment:

| Push to Branch | Environment | Spring Profile | Heroku App |
|---------------|-------------|----------------|------------|
| `develop` | Development | `dev` | `week8-practice1-dev` |
| `main` / `master` | Production | `prod` | `week8-practice1-prod` |

### Manual Deployment:

Click **Build with Parameters** → Select `DEPLOY_ENV`:

- `development` → deploys to `week8-practice1-dev`
- `production` → deploys to `week8-practice1-prod`

---

## ⚠️ Troubleshooting

### Docker Not Found

**Error**: `docker: not found`

**Solution**:
```bash
# SSH to Jenkins agent and install Docker
curl -fsSL https://get.docker.com | sh
```

### Tests Timeout

**Error**: `Timeout has been exceeded` during Build stage

**Cause**: Jenkins agent has low memory (< 1GB available)

**Solution**:
- Upgrade Jenkins agent memory
- Or reduce test complexity
- Tests are optimized but need adequate resources

### Docker Permission Denied

**Error**: `permission denied while trying to connect to the Docker daemon`

**Solution**:
```bash
# Add Jenkins user to docker group
sudo usermod -aG docker jenkins

# Or run Jenkins as root (not recommended)
```

### Heroku Deploy Failed

**Error**: `heroku: command not found`

**Solution**: The pipeline auto-installs Heroku CLI. Check network connectivity.

---

## 📊 Pipeline Stages

| Stage | Purpose | Fails If |
|-------|---------|----------|
| **Checkout** | Pull latest code | Git clone fails |
| **Build** | Maven build + tests | Tests fail ❌ |
| **Verify Tests** | Publish test results | No test reports ❌ |
| **Verify Docker** | Check Docker available | Docker not installed ❌ |
| **Build Docker Image** | Create image | Dockerfile error |
| **Push to Docker Hub** | Push image | Auth fails |
| **Deploy to Heroku** | Deploy container | Heroku error |
| **Health Check** | Verify app running | Health endpoint fails |

---

## ✅ Verification Checklist

After first successful build:

- [ ] Docker image created in Docker Hub
- [ ] Heroku app is running
- [ ] Health check returns 200 OK
- [ ] App responds with correct Spring profile
- [ ] Discord notification received (if configured)
- [ ] Console output shows "All tests passed"

---

**Last Updated:** 2026-02-25
**Pipeline Version:** dd8c6de
