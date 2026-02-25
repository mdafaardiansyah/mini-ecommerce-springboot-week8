# API Access Guide

## 🌐 Application URLs

### Production Environment
- **Application**: https://week8-practice1-prod-048e7c835f1d.herokuapp.com/
- **Swagger UI**: https://week8-practice1-prod-048e7c835f1d.herokuapp.com/swagger-ui.html
- **Health Check**: https://week8-practice1-prod-048e7c835f1d.herokuapp.com/actuator/health

### Development Environment
- **Application**: https://week8-practice1-dev-048e7c835f1d.herokuapp.com/
- **Swagger UI**: https://week8-practice1-dev-048e7c835f1d.herokuapp.com/swagger-ui.html
- **Health Check**: https://week8-practice1-dev-048e7c835f1d.herokuapp.com/actuator/health

## 🎯 Root Path Redirect

When you open the domain, it **automatically redirects to Swagger UI**:

```
https://week8-practice1-prod-xxx.herokuapp.com/
  ↓ (automatic redirect)
https://week8-practice1-prod-xxx.herokuapp.com/swagger-ui.html
```

## 📚 API Documentation

### Available Endpoints

Open any of the URLs above to see complete API documentation with:

| Feature | Description |
|---------|-------------|
| **Customer APIs** | CRUD operations for customers |
| **Product APIs** | CRUD operations for products |
| **Order APIs** | Order management and processing |

### Swagger UI Features

- 📖 **Interactive API documentation**
- 🧪 **Try API endpoints directly** from browser
- 📝 **Request/Response schemas**
- 🔒 **Authentication info** (if applicable)

## 🔧 Quick Test

### Test Health Endpoint

```bash
curl https://week8-practice1-prod-048e7c835f1d.herokuapp.com/actuator/health
```

Expected Response:
```json
{
  "status": "UP"
}
```

### Test Root Redirect

```bash
curl -I https://week8-practice1-prod-048e7c835f1d.herokuapp.com/
```

Expected Response:
```
HTTP/1.1 302 Found
Location: /swagger-ui.html
```

## 📋 Endpoint Summary

| Path | Method | Description |
|------|--------|-------------|
| `/` | GET | Redirect to Swagger UI |
| `/swagger-ui.html` | GET | Swagger UI interface |
| `/actuator/health` | GET | Application health status |
| `/api-docs` | GET | OpenAPI JSON spec |
| `/v3/api-docs` | GET | OpenAPI 3.0 spec |

## 🚀 Accessing API from Code

### JavaScript/Fetch

```javascript
// Get API documentation
fetch('https://week8-practice1-prod-048e7c835f1d.herokuapp.com/v3/api-docs')
  .then(res => res.json())
  .then(spec => console.log(spec));

// Health check
fetch('https://week8-practice1-prod-048e7c835f1d.herokuapp.com/actuator/health')
  .then(res => res.json())
  .then(health => console.log(health.status));
```

### cURL

```bash
# Get API spec
curl https://week8-practice1-prod-048e7c835f1d.herokuapp.com/v3/api-docs

# Health check
curl https://week8-practice1-prod-048e7c835f1d.herokuapp.com/actuator/health

# List customers
curl https://week8-practice1-prod-048e7c835f1d.herokuapp.com/api/customers

# Get specific customer
curl https://week8-practice1-prod-048e7c835f1d.herokuapp.com/api/customers/1
```

---

**Last Updated**: 2026-02-25
**Version**: 1.0
**Status**: ✅ Production Ready
