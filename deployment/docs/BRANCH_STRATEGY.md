# Branch Strategy & Deployment Mapping

## 🌳 Branch Structure

```
mini-ecommerce-springboot-week8/
├── master      → Production (prod profile)
├── develop     → Development (dev profile)
└── feature/*   → Feature branches (optional)
```

## 🚀 Deployment Rules

### Automatic Deployment (Webhook)

| Push to Branch | Environment | Spring Profile | Docker Image | Heroku App |
|---------------|-------------|----------------|--------------|------------|
| `develop` | Development | `dev` | `week8-practice1-dev` | `week8-practice1-dev` |
| `master` | Production | `prod` | `week8-practice1-prod` | `week8-practice1-prod` |

### Manual Deployment

Use Jenkins "Build with Parameters":

| Parameter | Value | Deploys To |
|-----------|-------|------------|
| `DEPLOY_ENV` | `development` | `week8-practice1-dev` (dev profile) |
| `DEPLOY_ENV` | `production` | `week8-practice1-prod` (prod profile) |

## 📋 Workflow

### Development Workflow

```bash
# 1. Create feature branch from develop
git checkout develop
git checkout -b feature/new-feature

# 2. Make changes and commit
git add .
git commit -m "feat: add new feature"

# 3. Push to GitHub
git push origin feature/new-feature

# 4. Create PR: feature/new-feature → develop

# 5. After merge, develop auto-deploys to DEV
```

### Production Workflow

```bash
# 1. Merge develop to master
git checkout master
git merge develop

# 2. Push to GitHub
git push origin master

# 3. Master auto-deploys to PROD
```

## 🔧 Configuration Differences

### Development (dev profile)

- **Database**: H2 in-memory (for testing)
- **Logging**: DEBUG level
- **API Docs**: Enabled (`/swagger-ui.html`)
- **Features**: All features enabled

### Production (prod profile)

- **Database**: MySQL/PostgreSQL (external)
- **Logging**: INFO level
- **API Docs**: Disabled in production
- **Features**: Production-ready configuration

## 🎯 Quick Reference

| Task | Command | Branch |
|------|---------|--------|
| **Start new feature** | `git checkout -b feature/xxx develop` | from develop |
| **Deploy to DEV** | `git push origin develop` | develop |
| **Deploy to PROD** | `git push origin master` | master |
| **Test locally with dev** | `mvn spring-boot:run -Dspring.profiles.active=dev` | - |
| **Test locally with prod** | `mvn spring-boot:run -Dspring.profiles.active=prod` | - |

## 📊 Jenkins Detection Logic

The Jenkinsfile automatically detects:

```groovy
if (branch == 'main' || branch == 'master') {
    // PRODUCTION
    profile = 'prod'
    appName = 'week8-practice1-prod'
} else if (branch == 'develop') {
    // DEVELOPMENT
    profile = 'dev'
    appName = 'week8-practice1-dev'
} else {
    // Use parameter for other branches
    if (DEPLOY_ENV == 'production') {
        appName = 'week8-practice1-prod'
    } else {
        appName = 'week8-practice1-dev'
    }
}
```

## ⚠️ Important Notes

1. **NEVER commit directly to master** - Always merge from develop
2. **develop branch is for testing** - Auto-deploys to dev environment
3. **master branch is for production** - Auto-deploys to production
4. **Feature branches** - Don't auto-deploy, require PR first

## 🔗 Links

- **GitHub Repository**: https://github.com/mdafaardiansyah/mini-ecommerce-springboot-week8
- **Dev Environment**: https://week8-practice1-dev.herokuapp.com
- **Prod Environment**: https://week8-practice1-prod.herokuapp.com
- **Dev Health Check**: https://week8-practice1-dev.herokuapp.com/actuator/health
- **Prod Health Check**: https://week8-practice1-prod.herokuapp.com/actuator/health

---

**Last Updated:** 2026-02-25
**Branches:** `master`, `develop`
