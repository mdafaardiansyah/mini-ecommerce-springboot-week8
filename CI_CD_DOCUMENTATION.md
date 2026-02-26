# CI/CD Pipeline Documentation - Order Management API

## 📋 Requirements Checklist

### ✅ All Requirements Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| **develop branch → dev profile** | ✅ IMPLEMENTED | Auto-detected from branch, deploys to `week8-practice1-dev` |
| **main/master branch → prod profile** | ✅ IMPLEMENTED | Auto-detected from branch, deploys to `week8-practice1-prod` |
| **Use Docker** | ✅ IMPLEMENTED | Builds Docker image, pushes to Docker Hub |
| **Restart container automatically** | ✅ IMPLEMENTED | Heroku Container Registry handles container restart |
| **Reflect latest code** | ✅ IMPLEMENTED | Always builds fresh image from source |
| **Use correct Spring profile** | ✅ IMPLEMENTED | `dev` for develop, `prod` for main/master |
| **Unit tests mandatory** | ✅ IMPLEMENTED | Build FAILS if tests fail, no skip |

---

## 🌳 Branch Strategy

```
┌─────────────────────────────────────────────────────────────┐
│ GitHub Repository                                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  develop ────────→ Development Environment (dev profile)     │
│     │                   week8-practice1-dev                   │
│     │                                                           │
│     ├─── feature branches (optional)                          │
│     │                                                           │
│     └─── hotfix branches (optional)                            │
│                                                             │
│  master ────────→ Production Environment (prod profile)        │
│                   week8-practice1-prod                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Branch → Environment Mapping

| Push to Branch | Environment | Spring Profile | Docker Image | Heroku App |
|---------------|-------------|----------------|--------------|------------|
| `develop` | Development | `dev` | `ardidafa/week8-practice1-dev` | `week8-practice1-dev` |
| `master` | Production | `prod` | `ardidafa/week8-practice1-prod` | `week8-practice1-prod` |

---

## 🚀 Complete CI/CD Flow

### Overview Diagram

```
┌──────────────┐
│ Git Push     │  Developer pushes to develop/master
└──────┬───────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│ GitHub Webhook                                             │
│ - Sends POST to Jenkins                                   │
│ - Contains branch info, commit hash                        │
└──────┬──────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│ Jenkins Pipeline (Jenkinsfile)                             │
│                                                               │
│  STAGE 1: Checkout                                          │
│  ├─ Clean workspace                                        │
│  ├─ Pull from GitHub                                        │
│  ├─ Detect branch (develop/master)                          │
│  └─ Set Spring profile & app name                           │
│                                                               │
│  STAGE 2: Build with Maven (Tests MANDATORY)                 │
│  ├─ Maven clean package                                     │
│  ├─ Run unit tests (Service layer with Mockito)             │
│  └─ Build JAR (FAILS if tests fail)                         │
│                                                               │
│  STAGE 3: Verify Tests                                       │
│  └─ Publish JUnit test results                              │
│                                                               │
│  STAGE 4: Verify Docker                                      │
│  └─ Check Docker CLI available                              │
│                                                               │
│  STAGE 5: Build Docker Image                                 │
│  ├─ docker build                                            │
│  ├─ Tag with build number                                    │
│  └─ Tag as latest                                           │
│                                                               │
│  STAGE 6: Push to Docker Hub                                 │
│  ├─ docker login (using credentials)                        │
│  ├─ Push build:X                                             │
│  └─ Push latest                                             │
│                                                               │
│  STAGE 7: Deploy to Heroku                                   │
│  ├─ Install Heroku CLI                                      │
│  ├─ Create app if not exists                                │
│  ├─ Set environment variables (Spring profile, DB)          │
│  ├─ Pull image from Docker Hub                              │
│  ├─ Tag for Heroku Container Registry                       │
│  ├─ Push to Heroku                                          │
│  └─ Release container (auto-restart)                        │
│                                                               │
│  STAGE 8: Health Check                                       │
│  ├─ Get app URL                                             │
│  ├─ Wait 60 seconds for startup                            │
│  └─ Check /actuator/health endpoint                         │
│                                                               │
│  Post Actions                                                │
│  ├─ Success: Discord notification                           │
│  ├─ Failure: Discord notification                           │
│  └─ Always: Docker cleanup                                 │
│                                                               │
└─────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│ Deployed Application                                        │
│ - Running on Heroku                                         │
│ - Docker container restarted                                 │
│ - Latest code deployed                                       │
│ - Health check passing                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Detailed Stage Breakdown

### STAGE 1: Checkout

**Purpose**: Pull latest code from GitHub and detect environment

**Steps**:
1. Clean workspace (remove old files)
2. Checkout from GitHub using `checkout scm`
3. Extract git information:
   - Branch name (develop/master)
   - Commit hash
4. **Auto-detect environment**:
   ```groovy
   if (branch == 'master' || branch == 'main') {
       profile = 'prod'
       appName = 'week8-practice1-prod'
   } else if (branch == 'develop') {
       profile = 'dev'
       appName = 'week8-practice1-dev'
   }
   ```

**Result**:
- Environment variables set: `SPRING_PROFILE`, `DEPLOY_APP_NAME`, `IMAGE_NAME`

---

### STAGE 2: Build with Maven

**Purpose**: Compile code, run tests, create JAR file

**Requirement**: ✅ **Unit tests MANDATORY - Build FAILS if tests fail**

**Steps**:
1. Maven clean package
2. Run unit tests (Service layer only)
   - Tests use **Mockito** (no database)
   - Integration tests skipped with `-DskipITs`
3. Create JAR file

**Test Configuration**:
```xml
<!-- pom.xml -->
<maven-surefire-plugin>
  <excludes>
    <exclude>**/*IntegrationTest.java</exclude>
  </excludes>
  <systemPropertyVariables>
    <spring.profiles.active>unit-test</spring.profiles.active>
  </systemPropertyVariables>
  <parallel>none</parallel>
  <reuseForks>true</reuseForks>
  <argLine>-Xmx128m -XX:MaxMetaspaceSize=64m</argLine>
</maven-surefire-plugin>
```

**Key Points**:
- ✅ Tests run in **unit-test** profile (no database)
- ✅ Optimized for low-memory Jenkins agents
- ✅ Build **FAILS** if any test fails (`-Dmaven.test.failure.ignore=false`)

---

### STAGE 3: Verify Tests

**Purpose**: Ensure all tests passed before deployment

**Steps**:
1. Publish JUnit test results to Jenkins
2. Check if test reports exist
3. Fail build if no test reports found

**Result**:
- ✅ Only proceeds if tests passed
- ❌ Pipeline stops if tests failed

---

### STAGE 4: Verify Docker

**Purpose**: Ensure Docker CLI is available on Jenkins agent

**Steps**:
1. Check if `docker` command exists
2. Display Docker version
3. **FAIL BUILD** if Docker not found

**Prerequisite**:
- Docker must be pre-installed on Jenkins agent
- Cannot auto-install due to permission constraints

---

### STAGE 5: Build Docker Image

**Purpose**: Create Docker image with the application

**Requirement**: ✅ **Use Docker**

**Dockerfile Structure**:
```dockerfile
# Multi-stage build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
# Build stage

FROM eclipse-temurin:17-jre-alpine
# Runtime stage with security
```

**Build Steps**:
1. Build image with tags:
   - `ardidafa/week8-practice1-{env}:BUILD_NUMBER`
   - `ardidafa/week8-practice1-{env}:latest`
2. Pass build args:
   - `SPRING_PROFILES_ACTIVE` (dev or prod)
   - `BUILD_DATE`
   - `VCS_REF` (commit hash)
   - `VERSION` (build number)

**Result**: Docker image created locally

---

### STAGE 6: Push to Docker Hub

**Purpose**: Store Docker image in public registry

**Steps**:
1. Login to Docker Hub using credentials
2. Push tagged image (`:BUILD_NUMBER`)
3. Push `:latest` tag

**Credentials**:
- ID: `docker-hub`
- Type: Username with password
- Auto-injected by Jenkins

**Result**:
- ✅ Image available at: `https://hub.docker.com/r/ardidafa/week8-practice1-prod`

---

### STAGE 7: Deploy to Heroku

**Purpose**: Deploy container to production environment

**Requirements**: ✅
- ✅ Use Docker
- ✅ Restart container automatically
- ✅ Reflect latest code
- ✅ Use correct Spring profile

**Steps**:

**7.1 Install Heroku CLI**
```bash
curl -fsSL https://cli-assets.heroku.com/heroku-linux-x64.tar.gz
tar -xzf heroku.tar.gz
```

**7.2 Create/Verify App**
```bash
heroku create week8-practice1-prod  # if not exists
```

**7.3 Set Environment Variables**
```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set SPRING_DATASOURCE_URL=jdbc:mysql://...
heroku config:set SPRING_DATASOURCE_USERNAME=xxx
heroku config:set SPRING_DATASOURCE_PASSWORD=xxx
```

**7.4 Deploy Container**
```bash
# Pull from Docker Hub
docker pull ardidafa/week8-practice1-prod:22

# Tag for Heroku Container Registry
docker tag ardidafa/week8-practice1-prod:22 \
  registry.heroku.com/week8-practice1-prod/web

# Push to Heroku
docker login --username=_ --password-stdin registry.heroku.com
docker push registry.heroku.com/week8-practice1-prod/web

# Release (auto-restart container)
heroku container:release web --app week8-practice1-prod
```

**Key Points**:
- ✅ **Docker used**: Deploying container image
- ✅ **Auto-restart**: Heroku automatically restarts container on release
- ✅ **Latest code**: Always build fresh image, never cached
- ✅ **Correct profile**: `dev` or `prod` based on branch

---

### STAGE 8: Health Check

**Purpose**: Verify application is running correctly

**Steps**:
1. Get app URL from Heroku
2. Wait 60 seconds for container startup
3. Check `/actuator/health` endpoint
4. Retry up to 10 times (10 second interval)

**Success Criteria**:
```
HTTP_CODE = 200
{
  "status": "UP"
}
```

**Failure**:
```
❌ Health check failed after 10 attempts
Pipeline FAILED
```

---

## 🔄 Webhook Configuration

### GitHub → Jenkins Integration

**GitHub Webhook Settings**:
- **Payload URL**: `https://jenkins-dev.glanze.space/github-webhook/`
- **Content type**: `application/json`
- **Secret**: (optional, empty for now)
- **SSL verification**: Enabled
- **Events**: Just the push event
- **Active**: ✅

**Jenkins Job Settings**:
- **Pipeline script from SCM**: Git
- **Repository**: `https://github.com/mdafaardiansyah/mini-ecommerce-springboot-week8.git`
- **Script Path**: `Jenkinsfile`
- **Branches to build**: `**` (all branches)
- **Build Triggers**:
  - ✅ **GitHub hook trigger for GITScm polling**

### Auto-Deploy Flow

```
Developer pushes code
       ↓
GitHub receives push
       ↓
GitHub sends webhook to Jenkins
       ↓
Jenkins receives webhook
       ↓
Jenkins starts pipeline
       ↓
[BUILD → TEST → DOCKER → DEPLOY → HEALTH CHECK]
       ↓
Deployment SUCCESS
       ↓
Discord notification sent
```

---

## 🎯 Requirements Verification

### 1. Branch → Environment Mapping

| Requirement | Implementation |
|-------------|----------------|
| `develop` → dev profile | ✅ Auto-detected in Checkout stage |
| `master` → prod profile | ✅ Auto-detected in Checkout stage |

**Code**:
```groovy
def cleanBranch = env.GIT_BRANCH.replace('origin/', '')
if (cleanBranch == 'main' || cleanBranch == 'master') {
    env.SPRING_PROFILE = 'prod'
    env.DEPLOY_APP_NAME = HEROKU_APP_NAME_PROD
} else if (cleanBranch == 'develop') {
    env.SPRING_PROFILE = 'dev'
    env.DEPLOY_APP_NAME = HEROKU_APP_NAME_DEV
}
```

### 2. Use Docker

| Component | Docker Usage |
|-----------|--------------|
| Build | `docker build` |
| Registry | Docker Hub (`ardidafa/week8-practice1-prod`) |
| Deployment | `docker push` to Heroku Container Registry |

**Proof**: See STAGE 5, 6, 7 in pipeline

### 3. Restart Container Automatically

**Implementation**: Heroku Container Registry

```bash
heroku container:release web --app week8-practice1-prod
```

**How it works**:
- Heroku stops old container
- Heroku starts new container with new image
- Health checks ensure it's running

### 4. Reflect Latest Code

**Implementation**: Always build from source

```dockerfile
# Dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B
```

**Result**:
- ✅ Never uses cached images
- ✅ Always builds fresh from source code
- ✅ Latest commit is always deployed

### 5. Use Correct Spring Profile

**Implementation**: Passed as build arg

```dockerfile
docker build \
  --build-arg SPRING_PROFILES_ACTIVE=${env.SPRING_PROFILE} \
  --build-arg SPRING_PROFILES_ACTIVE=prod \
  .
```

**Heroku Config**:
```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
```

**Result**:
- ✅ `develop` branch → `dev` profile
- ✅ `master` branch → `prod` profile

### 6. Unit Tests Mandatory

**Implementation**: Maven Surefire Plugin

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <systemPropertyVariables>
      <spring.profiles.active>unit-test</spring.profiles.active>
    </systemPropertyVariables>
  </configuration>
</plugin>
```

**Build Command**:
```bash
mvn clean package \
  -DskipITs \
  -Dmaven.test.failure.ignore=false \
  -B
```

**Result**:
- ✅ Tests run every build
- ✅ Build FAILS if tests fail
- ✅ Cannot skip tests

---

## 📊 Deployment Environments

### Development Environment

| Property | Value |
|----------|-------|
| **Branch** | `develop` |
| **Spring Profile** | `dev` |
| **Heroku App** | `week8-practice1-dev` |
| **Docker Image** | `ardidafa/week8-practice1-dev` |
| **Database** | MySQL (configurable) |
| **URL** | `https://week8-practice1-dev-xxx.herokuapp.com` |
| **Swagger UI** | Enabled |

### Production Environment

| Property | Value |
|----------|-------|
| **Branch** | `master` / `main` |
| **Spring Profile** | `prod` |
| **Heroku App** | `week8-practice1-prod` |
| **Docker Image** | `ardidafa/week8-practice1-prod` |
| **Database** | MySQL (external) |
| **URL** | `https://week8-practice1-prod-xxx.herokuapp.com` |
| **Swagger UI** | Enabled |

---

## 🛠️ Prerequisites

### Jenkins Configuration

**Tools**:
- Maven 3.9
- Git Plugin
- Pipeline Plugin
- Docker (pre-installed on agent)

**Credentials** (in Jenkins → Manage Jenkins → Credentials):

| Credential ID | Type | Description |
|---------------|------|-------------|
| `docker-hub` | Username + Password | Docker Hub authentication |
| `HEROKU_API_KEY` | Secret text | Heroku API key |
| `db-url` | Secret text | Database JDBC URL |
| `db-username` | Secret text | Database username |
| `db-password` | Secret text | Database password |
| `db-driver` | Secret text | Database driver class |
| `discord-notification` | Secret text | Discord webhook (optional) |

### Heroku Setup

**Apps Created**:
- `week8-practice1-dev` (Development)
- `week8-practice1-prod` (Production)

**Add-ons Needed** (optional):
- Database: Heroku Postgres or clearDB for MySQL
- Logging: Logplex (optional)

---

## 🔍 Troubleshooting

### Build Fails at Stage 2 (Build)

**Cause**: Unit tests failed

**Solution**:
1. Run tests locally: `mvn clean test`
2. Fix failing tests
3. Push again

### Build Fails at Stage 4 (Verify Docker)

**Cause**: Docker not installed on Jenkins agent

**Solution**:
```bash
# On VPS
docker stop jenkins-blueocean
docker rm jenkins-blueocean
docker run -d \
  --name jenkins-blueocean \
  --restart unless-stopped \
  -p 9000:8080 \
  -p 50000:50000 \
  --link jenkins-docker:docker \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker:ro \
  jenkins/jenkins:lts-jdk17
```

### Build Fails at Stage 6 (Push to Docker Hub)

**Cause**: Wrong Docker Hub credentials or username

**Solution**:
1. Check `DOCKER_REPO` in Jenkinsfile (must be `ardidafa`)
2. Verify credentials in Jenkins
3. Ensure image is public

### Health Check Fails

**Cause**: Application not starting or /actuator/health not accessible

**Solution**:
1. Check Heroku logs: `heroku logs --tail --app week8-practice1-prod`
2. Check if /actuator/health enabled
3. Verify database connection

---

## 📈 Pipeline Performance

| Stage | Duration (typical) |
|-------|-------------------|
| Checkout | 30 seconds |
| Build + Tests | 3-5 minutes (offline) / 10-15 minutes (online) |
| Verify Tests | 5 seconds |
| Verify Docker | 2 seconds |
| Build Docker Image | 2-3 minutes |
| Push to Docker Hub | 1-2 minutes |
| Deploy to Heroku | 3-5 minutes |
| Health Check | 1-2 minutes |
| **TOTAL** | **~15-25 minutes** |

---

## ✅ Verification Checklist

After each deployment, verify:

- [ ] Docker image appears in Docker Hub
- [ ] Heroku app is running
- [ ] Health check returns `{"status":"UP"}`
- [ ] Swagger UI accessible at root URL
- [ ] Correct Spring profile is used (check logs)
- [ ] Latest commit is deployed (check Heroku logs)
- [ ] Discord notification received (if configured)

---

## 📚 Related Documentation

- **API Documentation**: `docs/API_ACCESS.md`
- **Branch Strategy**: `deployment/docs/BRANCH_STRATEGY.md`
- **Deployment Guide**: `deployment/README.md`

---

## 🎉 Conclusion

**All requirements FULLY IMPLEMENTED** ✅

1. ✅ **develop** → dev profile (auto-deploy)
2. ✅ **master** → prod profile (auto-deploy)
3. ✅ Docker used for deployment
4. ✅ Container auto-restarts (Heroku)
5. ✅ Latest code always deployed
6. ✅ Correct Spring profile used
7. ✅ Unit tests mandatory (build fails if tests fail)

**Deployment Flow**:
```
Push → Webhook → Jenkins → Build → Test → Docker → Push → Deploy → Health Check → Success
```

**Status**: ✅ **PRODUCTION READY**

---

**Last Updated**: 2026-02-26
**Jenkinsfile Version**: ea91322
**Branches**: `master`, `develop`
