# How to Install Docker on Jenkins Agent

## Determine Your Jenkins Setup First

### Option 1: Jenkins Running on Docker
Your Jenkins itself is a Docker container.

### Option 2: Jenkins Running on VPS/Direct
Your Jenkins is installed directly on your VPS.

### Option 3: Jenkins Cloud (Jenkins.io, AWS, etc.)
Your Jenkins is hosted service.

---

## How to Check Which One You Have

Run this on your VPS:

```bash
# Check if Jenkins is running in Docker
docker ps | grep jenkins

# OR check if Jenkins is running as service
systemctl status jenkins  # on Ubuntu/Debian
service jenkins status     # on other systems
```

---

## Solution Based on Your Setup

### Scenario A: Jenkins Running on Docker

If `docker ps | grep jenkins` shows output:

```bash
# Stop Jenkins container
docker stop <jenkins-container-name>

# Remove container (keep data!)
docker rm <jenkins-container-name>

# Start Jenkins WITH Docker mounted
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker \
  jenkins/jenkins:lts
```

### Scenario B: Jenkins Running on VPS (Your Case)

Since you're on VPS (`srv575756`), Jenkins is likely:

1. Running as service, OR
2. Running inside Docker

#### Check 1: Is Jenkins a Service?

```bash
systemctl status jenkins
```

If this shows Jenkins is running:
```bash
# Install Docker directly on your VPS
curl -fsSL https://get.docker.com | sh

# Add Jenkins user to docker group
sudo usermod -aG docker jenkins

# Verify
docker --version
```

#### Check 2: Is Jenkins in Docker?

```bash
docker ps
```

If you see jenkins container:
```bash
# Get Jenkins container name
docker ps | grep jenkins

# Stop and recreate with Docker socket mounted
docker stop <container-name>
docker rm <container-name>

# Recreate with Docker access
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /usr/bin/docker:/usr/bin/docker:ro \
  jenkins/jenkins:lts
```

---

## Quick Test

After installing Docker, create a test job in Jenkins:

**Job Name:** `test-docker`
**Type:** Pipeline
**Script:**
```groovy
pipeline {
    agent any
    stages {
        stage('Test Docker') {
            steps {
                sh 'docker --version'
                sh 'docker run hello-world'
            }
        }
    }
}
```

If this succeeds → Docker is ready! ✅

---

## Still Having Issues?

### Option 1: Use DinD (Docker-in-Docker)

If you cannot mount Docker socket, use DinD:

```yaml
# Pod template for Kubernetes
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
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

### Option 2: Use Docker-in-Docker Sidecar

Run DinD as sidecar container:

```bash
docker run -d --privileged --name dind \
  -e DOCKER_TLS_CERTDIR="" \
  docker:stable-dind

docker run -d --name jenkins \
  --link dind:docker \
  -p 8080:8080 \
  -e DOCKER_HOST=tcp://docker:2375 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts
```

### Option 3: Use Pre-built Jenkins Image with Docker

Use image that already has Docker:

```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  tekuology/jenkins-docker:latest
```

---

## For Your Specific Case (VPS: srv575756)

### Step 1: Check how Jenkins runs

```bash
# On your VPS
docker ps | grep jenkins
```

### Step 2A: If Jenkins in Docker

Recreate Jenkins container with Docker mounted:

```bash
# Stop current
docker stop <jenkins-container>
docker rm <jenkins-container>

# Start new with Docker
docker run -d \
  --name jenkins \
  --restart unless-stopped \
  -p 8080:8080 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

### Step 2B: If Jenkins as Service

Install Docker on VPS:

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh

# Add Jenkins user to docker group
sudo usermod -aG docker jenkins

# Restart Jenkins
sudo systemctl restart jenkins
```

### Step 3: Verify

Create test pipeline job and run.

---

## Need More Help?

Provide output of these commands:

```bash
# 1. Check Jenkins process
ps aux | grep jenkins

# 2. Check Docker
docker --version

# 3. Check Jenkins containers
docker ps | grep jenkins

# 4. Check Jenkins service
systemctl status jenkins
```

Share these outputs and I can give you exact commands!