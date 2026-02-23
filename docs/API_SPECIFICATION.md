# API Specification
## Smart Inventory & Order Management System

**Base URL**: `http://localhost:8081/api` (Development)
**Version**: `1.0.0`
**Content-Type**: `application/json`

---

## Table of Contents

1. [Product Management](#1-product-management)
2. [Customer Management](#2-customer-management)
3. [Order Management](#3-order-management)
4. [Error Responses](#error-responses)

---

## 1. Product Management

### 1.1 Create Product

Create a new product in the system.

**Endpoint**: `POST /products`

**Request Body**:
```json
{
  "name": "Laptop Gaming ASUS ROG",
  "category": "ELECTRONICS",
  "price": 15000000,
  "stock": 10
}
```

**Field Validation**:
- `name`: string, required, unique, max 255 chars
- `category`: enum, required (`ELECTRONICS`, `FOOD`, `FASHION`)
- `price`: number, required, must be > 0
  - If `category == FOOD`: max price is 1,000,000
- `stock`: integer, required, must be >= 0

**Success Response**: `201 Created`
```json
{
  "id": 1,
  "name": "Laptop Gaming ASUS ROG",
  "category": "ELECTRONICS",
  "price": 15000000,
  "stock": 10,
  "active": true,
  "createdAt": "2026-02-18T10:00:00",
  "updatedAt": "2026-02-18T10:00:00"
}
```

**Error Responses**:
- `400 Bad Request`: Validation failed
  ```json
  {
    "code": "VALIDATION_ERROR",
    "message": "Price for FOOD category cannot exceed 1,000,000",
    "details": ["product.price: Food category maximum price is 1,000,000"]
  }
  ```
- `409 Conflict`: Product name already exists
  ```json
  {
    "code": "DUPLICATE_RESOURCE",
    "message": "Product with name 'Laptop Gaming ASUS ROG' already exists",
    "details": []
  }
  ```

---

### 1.2 Get Product by ID

Retrieve a specific product by ID.

**Endpoint**: `GET /products/{id}`

**Path Parameters**:
- `id`: long, required - Product ID

**Success Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Laptop Gaming ASUS ROG",
  "category": "ELECTRONICS",
  "price": 15000000,
  "stock": 10,
  "active": true,
  "createdAt": "2026-02-18T10:00:00",
  "updatedAt": "2026-02-18T10:00:00"
}
```

**Error Response**: `404 Not Found`
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Product with id 999 not found",
  "details": []
}
```

---

### 1.3 Get All Products (with Pagination)

Retrieve all products with pagination and filtering.

**Endpoint**: `GET /products`

**Query Parameters**:
- `page`: integer, optional (default: 0) - Page number
- `size`: integer, optional (default: 10) - Items per page
- `sortBy`: string, optional (default: id) - Sort field
- `sortDirection`: string, optional (default: ASC) - ASC or DESC
- `category`: string, optional - Filter by category
- `active`: boolean, optional - Filter by active status
- `name`: string, optional - Filter by name (contains, case-insensitive)

**Request Example**:
```
GET /products?page=0&size=10&category=ELECTRONICS&active=true&sortBy=price&sortDirection=DESC
```

**Success Response**: `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Gaming ASUS ROG",
      "category": "ELECTRONICS",
      "price": 15000000,
      "stock": 10,
      "active": true,
      "createdAt": "2026-02-18T10:00:00",
      "updatedAt": "2026-02-18T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### 1.4 Update Product

Update an existing product.

**Endpoint**: `PUT /products/{id}`

**Path Parameters**:
- `id`: long, required - Product ID

**Request Body**:
```json
{
  "price": 14500000,
  "stock": 15
}
```

**Field Validation**:
- `price`: number, optional, must be > 0
  - Cannot update if product has completed orders
- `stock`: integer, optional, must be >= 0
  - Cannot set < current reserved stock

**Success Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Laptop Gaming ASUS ROG",
  "category": "ELECTRONICS",
  "price": 14500000,
  "stock": 15,
  "active": true,
  "createdAt": "2026-02-18T10:00:00",
  "updatedAt": "2026-02-18T11:00:00"
}
```

**Error Responses**:
- `400 Bad Request`: Business rule violation
  ```json
  {
    "code": "BUSINESS_ERROR",
    "message": "Cannot update product price. Product has completed orders.",
    "details": []
  }
  ```
- `404 Not Found`: Product not found

---

### 1.5 Delete Product (Soft Delete)

Deactivate a product (soft delete).

**Endpoint**: `DELETE /products/{id}`

**Path Parameters**:
- `id`: long, required - Product ID

**Business Rules**:
- Cannot delete if `stock > 0`
- Sets `active = false`

**Success Response**: `204 No Content`

**Error Response**: `400 Bad Request`
```json
{
  "code": "BUSINESS_ERROR",
  "message": "Cannot delete product with stock > 0",
  "details": ["Current stock: 10"]
}
```

---

## 2. Customer Management

### 2.1 Create Customer

Create a new customer.

**Endpoint**: `POST /customers`

**Request Body**:
```json
{
  "name": "Ahmad Sudrajat",
  "email": "ahmad.sudrajat@example.com"
}
```

**Field Validation**:
- `name`: string, required, max 255 chars
- `email`: string, required, unique, valid email format

**Default Values**:
- `membershipLevel`: REGULAR
- `totalSpent`: 0
- `active`: true

**Success Response**: `201 Created`
```json
{
  "id": 1,
  "name": "Ahmad Sudrajat",
  "email": "ahmad.sudrajat@example.com",
  "membershipLevel": "REGULAR",
  "totalSpent": 0,
  "active": true
}
```

**Error Response**: `409 Conflict`
```json
{
  "code": "DUPLICATE_RESOURCE",
  "message": "Customer with email 'ahmad.sudrajat@example.com' already exists",
  "details": []
}
```

---

### 2.2 Get Customer by ID

Retrieve a specific customer by ID.

**Endpoint**: `GET /customers/{id}`

**Path Parameters**:
- `id`: long, required - Customer ID

**Success Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Ahmad Sudrajat",
  "email": "ahmad.sudrajat@example.com",
  "membershipLevel": "REGULAR",
  "totalSpent": 5000000,
  "active": true
}
```

**Error Response**: `404 Not Found`

---

### 2.3 Get All Customers (with Pagination)

Retrieve all customers with pagination and filtering.

**Endpoint**: `GET /customers`

**Query Parameters**:
- `page`: integer, optional (default: 0)
- `size`: integer, optional (default: 10)
- `sortBy`: string, optional (default: id)
- `sortDirection`: string, optional (default: ASC)
- `membershipLevel`: string, optional - Filter by membership level
- `active`: boolean, optional - Filter by active status
- `name`: string, optional - Filter by name (contains)
- `email`: string, optional - Filter by email (contains)

**Request Example**:
```
GET /customers?page=0&size=10&membershipLevel=GOLD&active=true
```

**Success Response**: `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Ahmad Sudrajat",
      "email": "ahmad.sudrajat@example.com",
      "membershipLevel": "GOLD",
      "totalSpent": 12000000,
      "active": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### 2.4 Update Customer

Update an existing customer.

**Endpoint**: `PUT /customers/{id}`

**Path Parameters**:
- `id`: long, required - Customer ID

**Request Body**:
```json
{
  "name": "Ahmad Sudrajat Updated",
  "email": "ahmad.newemail@example.com"
}
```

**Business Rules**:
- Auto-upgrade membership based on `totalSpent`:
  - `totalSpent` ≥ 10,000,000 → GOLD
  - `totalSpent` ≥ 50,000,000 → PLATINUM
- Downgrade is NOT allowed

**Success Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Ahmad Sudrajat Updated",
  "email": "ahmad.newemail@example.com",
  "membershipLevel": "GOLD",
  "totalSpent": 12000000,
  "active": true
}
```

**Error Response**: `409 Conflict`
```json
{
  "code": "DUPLICATE_RESOURCE",
  "message": "Email is already used by another customer",
  "details": []
}
```

---

### 2.5 Delete Customer

Deactivate a customer (soft delete).

**Endpoint**: `DELETE /customers/{id}`

**Path Parameters**:
- `id`: long, required - Customer ID

**Business Rules**:
- Sets `active = false`
- Cannot create new orders for inactive customers

**Success Response**: `204 No Content`

**Error Response**: `404 Not Found`

---

## 3. Order Management

### 3.1 Create Order

Create a new order with items.

**Endpoint**: `POST /orders`

**Request Body**:
```json
{
  "customerId": 1,
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

**Field Validation**:
- `customerId`: long, required - Must be active customer
- `orderItems`: array, required, min 1 item
  - `productId`: long, required - Must be active product
  - `quantity`: integer, required, must be > 0

**Business Rules**:
1. Validate stock availability
2. Reduce stock immediately
3. Calculate `totalAmount` = sum(price * quantity)
4. Apply discount based on customer membership:
   - REGULAR: 0%
   - GOLD: 10%
   - PLATINUM: 20%
5. Apply bonus 5% discount if `totalAmount` > 5,000,000
6. Cap total discount at 30%
7. Initial status: CREATED

**Success Response**: `201 Created`
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "Ahmad Sudrajat",
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop Gaming ASUS ROG",
      "quantity": 2,
      "priceAtPurchase": 15000000,
      "subtotal": 30000000
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Mouse Logitech MX Master",
      "quantity": 1,
      "priceAtPurchase": 1500000,
      "subtotal": 1500000
    }
  ],
  "totalAmount": 31500000,
  "discountAmount": 9450000,
  "finalAmount": 22050000,
  "status": "CREATED",
  "createdAt": "2026-02-18T10:00:00"
}
```

**Discount Calculation Example**:
```
Customer: PLATINUM (20% discount)
Total Amount: 31,500,000
Membership Discount: 20% = 6,300,000
Bonus Discount (Total > 5M): 5% = 1,575,000
Total Discount: 26% (capped below 30%) = 8,190,000
Final Amount: 31,500,000 - 8,190,000 = 23,310,000
```

**Error Responses**:
- `400 Bad Request`: Validation failed
  ```json
  {
    "code": "BUSINESS_ERROR",
    "message": "Insufficient stock for product 'Laptop Gaming ASUS ROG'",
    "details": ["Requested: 5, Available: 3"]
  }
  ```
- `404 Not Found`: Customer or product not found
  ```json
  {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Customer with id 999 not found",
    "details": []
  }
  ```

---

### 3.2 Get Order by ID

Retrieve a specific order by ID.

**Endpoint**: `GET /orders/{id}`

**Path Parameters**:
- `id`: long, required - Order ID

**Success Response**: `200 OK`
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "Ahmad Sudrajat",
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop Gaming ASUS ROG",
      "quantity": 2,
      "priceAtPurchase": 15000000,
      "subtotal": 30000000
    }
  ],
  "totalAmount": 30000000,
  "discountAmount": 6000000,
  "finalAmount": 24000000,
  "status": "CREATED",
  "createdAt": "2026-02-18T10:00:00"
}
```

**Error Response**: `404 Not Found`

---

### 3.3 Get All Orders (with Pagination)

Retrieve all orders with pagination and filtering.

**Endpoint**: `GET /orders`

**Query Parameters**:
- `page`: integer, optional (default: 0)
- `size`: integer, optional (default: 10)
- `sortBy`: string, optional (default: createdAt)
- `sortDirection`: string, optional (default: DESC)
- `status`: string, optional - Filter by status
- `customerId`: long, optional - Filter by customer
- `startDate`: string, optional - Filter by date range (ISO format)
- `endDate`: string, optional - Filter by date range (ISO format)

**Request Example**:
```
GET /orders?page=0&size=10&status=PAID&customerId=1
```

**Success Response**: `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "Ahmad Sudrajat",
      "orderItems": [],
      "totalAmount": 30000000,
      "discountAmount": 6000000,
      "finalAmount": 24000000,
      "status": "PAID",
      "createdAt": "2026-02-18T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### 3.4 Pay Order

Process payment for an order.

**Endpoint**: `POST /orders/{id}/pay`

**Path Parameters**:
- `id`: long, required - Order ID

**Business Rules**:
- Only orders with status `CREATED` can be paid
- Update status to `PAID`
- Update customer `totalSpent`
- Recalculate customer membership based on new `totalSpent`

**Success Response**: `200 OK`
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "Ahmad Sudrajat",
  "orderItems": [],
  "totalAmount": 30000000,
  "discountAmount": 6000000,
  "finalAmount": 24000000,
  "status": "PAID",
  "createdAt": "2026-02-18T10:00:00"
}
```

**Error Responses**:
- `400 Bad Request`: Invalid status
  ```json
  {
    "code": "BUSINESS_ERROR",
    "message": "Cannot pay order. Order is already PAID",
    "details": []
  }
  ```
- `404 Not Found`: Order not found

---

### 3.5 Cancel Order

Cancel an order and restore stock.

**Endpoint**: `POST /orders/{id}/cancel`

**Path Parameters**:
- `id`: long, required - Order ID

**Business Rules**:
- Only orders with status `CREATED` can be cancelled
- Cannot cancel PAID orders
- Restore stock for all items
- Update status to `CANCELLED`

**Success Response**: `200 OK`
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "Ahmad Sudrajat",
  "orderItems": [],
  "totalAmount": 30000000,
  "discountAmount": 6000000,
  "finalAmount": 24000000,
  "status": "CANCELLED",
  "createdAt": "2026-02-18T10:00:00"
}
```

**Error Responses**:
- `400 Bad Request`: Cannot cancel paid order
  ```json
  {
    "code": "BUSINESS_ERROR",
    "message": "Cannot cancel order. Order is already PAID",
    "details": ["Only orders with CREATED status can be cancelled"]
  }
  ```
- `404 Not Found`: Order not found

---

## Error Responses

All error responses follow this format:

### Standard Error Response
```json
{
  "code": "ERROR_CODE",
  "message": "Human readable error message",
  "details": ["Specific detail 1", "Specific detail 2"]
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `BUSINESS_ERROR` | 400 | Business rule violation |
| `RESOURCE_NOT_FOUND` | 404 | Resource not found |
| `DUPLICATE_RESOURCE` | 409 | Resource already exists |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

### Common HTTP Status Codes

| Status | Description |
|--------|-------------|
| `200 OK` | Request successful |
| `201 Created` | Resource created successfully |
| `204 No Content` | Request successful, no content returned |
| `400 Bad Request` | Invalid request or business rule violation |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Resource already exists |
| `500 Internal Server Error` | Server error |

---

## Enums

### ProductCategory
```json
{
  "values": ["ELECTRONICS", "FOOD", "FASHION"]
}
```

### CustomerMembership
```json
{
  "values": ["REGULAR", "GOLD", "PLATINUM"]
}
```

### OrderStatus
```json
{
  "values": ["CREATED", "PAID", "CANCELLED"]
}
```

---

## Notes

1. **Authentication**: Currently not implemented (add in future)
2. **Rate Limiting**: Currently not implemented (add in future)
3. **Pagination**: All list endpoints support pagination
4. **Sorting**: All list endpoints support sorting
5. **Filtering**: All list endpoints support filtering
6. **Soft Delete**: Products and Customers use soft delete
7. **Discount Rules**:
   - Membership: REGULAR (0%), GOLD (10%), PLATINUM (20%)
   - Bonus: 5% if total > 5,000,000
   - Max discount: 30%
8. **Membership Upgrade**:
   - Auto-upgrade when totalSpent threshold reached
   - No downgrade allowed

---

## Testing the API

### Using cURL

**Create Product**:
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Gaming ASUS ROG",
    "category": "ELECTRONICS",
    "price": 15000000,
    "stock": 10
  }'
```

**Create Customer**:
```bash
curl -X POST http://localhost:8081/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ahmad Sudrajat",
    "email": "ahmad.sudrajat@example.com"
  }'
```

**Create Order**:
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "orderItems": [
      {
        "productId": 1,
        "quantity": 1
      }
    ]
  }'
```

### Using Swagger UI

Access Swagger UI at: `http://localhost:8081/swagger-ui.html`

---

## Changelog

### Version 1.0.0 (2026-02-18)
- Initial API specification
- Product, Customer, and Order management endpoints
- Pagination and filtering support
- Business rules implementation
