# Deployment Guide

This folder contains deployment configurations, scripts, and documentation for the Week 8 Practice 1 project.

## 📁 Structure

```
deployment/
├── docs/
│   └── JENKINS_REFRESH.md    # Jenkins workspace troubleshooting guide
├── scripts/
│   └── TROUBLESHOOT_BUILD.sh # Maven build diagnostics script
```

## 🚀 Quick Start

### Jenkins CI/CD Deployment

The project uses Jenkins for automated CI/CD with the following pipeline:

1. **Checkout** - Pull latest code from GitHub
2. **System Check** - Verify Jenkins agent resources
3. **Build** - Maven build with unit tests (mandatory)
4. **Verify Tests** - Ensure all tests pass
5. **Install Docker** - Auto-install Docker if not present
6. **Build Docker Image** - Create Docker image
7. **Push to Docker Hub** - Push image to Docker Hub registry
8. **Deploy to Heroku** - Deploy to Heroku Container Registry
9. **Health Check** - Verify application is running

### Environment Mapping

| Branch | Environment | Spring Profile | Docker Image | Heroku App |
|--------|-------------|----------------|--------------|------------|
| `master` / `main` | Production | `prod` | `week8-practice1-prod` | `week8-practice1-prod` |
| `develop` | Development | `dev` | `week8-practice1-dev` | `week8-practice1-dev` |

## 🔧 Configuration Files

All deployment configurations are in the **root directory**:

- **`Jenkinsfile`** - CI/CD pipeline definition
- **`Dockerfile`** - Docker image build configuration
- **`Procfile`** - Heroku process configuration
- **`system.properties`** - Java runtime version for Heroku
- **`.dockerignore`** - Files to exclude from Docker build

## 📋 Prerequisites

### Jenkins Credentials

Configure these in Jenkins → Manage Jenkins → Credentials:

- **`docker-hub`** - Docker Hub username:password
- **`HEROKU_API_KEY`** - Heroku API key
- **`db-url`** - Database JDBC URL
- **`db-username`** - Database username
- **`db-password`** - Database password
- **`db-driver`** - Database driver class (e.g., `com.mysql.cj.jdbc.Driver`)

### Required Jenkins Tools/Plugins

- **Maven 3.9** - Configured in Global Tool Configuration
- **Git Plugin** - For SCM checkout
- **Docker** - Auto-installed by pipeline if not present
- **Pipeline Plugin** - For declarative pipeline syntax

## 🐛 Troubleshooting

### Build is slow or timeout

1. **Read the guide:** `deployment/docs/JENKINS_REFRESH.md`
2. **Run diagnostics:** `deployment/scripts/TROUBLESHOOT_BUILD.sh`
3. **Clean Jenkins workspace:** Enable "Delete workspace before build starts" in job config

### Maven dependency collection takes forever

This is **normal for first build** (15-20 minutes). Subsequent builds use cached dependencies and complete in 3-5 minutes.

### Docker not found

The pipeline will **auto-install Docker**. No manual setup required.

### Tests failing

- **Unit tests** (service layer) are **MANDATORY** and must pass
- Tests use **Mockito** (no database required)
- Check test logs in Jenkins output

## 📊 Build Performance

| Scenario | Duration | Notes |
|----------|----------|-------|
| First build (no cache) | 15-20 min | Downloads all dependencies |
| Subsequent builds (cached) | 3-5 min | Uses Maven offline mode |
| Docker build | 2-3 min | Layer caching helps |
| Heroku deployment | 3-5 min | Includes container registry push |

## 🔗 Useful Links

- **API Documentation:** See `docs/API_SPECIFICATION.md`
- **Jenkins Pipeline:** Root `Jenkinsfile`
- **Docker Image:** Root `Dockerfile`

---

**Last Updated:** 2026-02-24
