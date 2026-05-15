# Katerly API Documentation

Base URL: `{{base_url}}`

> **Authentication:** Most endpoints require a Bearer Token. Pass the `access_token` in the `Authorization` header as `Bearer {{access_token}}`.

---

## Table of Contents

- [Health](#health)
- [Auth](#auth)
- [Business Profile](#business-profile)
- [Dashboard](#dashboard)
- [Ingredients](#ingredients)
- [Recipes](#recipes)
- [Notas](#notas)
- [Shopping Lists](#shopping-lists)
- [Subscriptions](#subscriptions)

---

## Health

### Health Check
- **Method:** `GET`
- **URL:** `/api/health`
- **Auth:** None

---

## Auth

### Register
- **Method:** `POST`
- **URL:** `/api/auth/register`
- **Auth:** None
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "namaPemilik": "Hanif",
  "email": "hanifafghani92@gmail.com",
  "password": "password123"
}
```

---

### Login
- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Auth:** None
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "email": "hanifafghani92@gmail.com",
  "password": "newpassword1234"
}
```

---

### Logout
- **Method:** `POST`
- **URL:** `/api/auth/logout`
- **Auth:** None

---

### Refresh Token
- **Method:** `POST`
- **URL:** `/api/auth/refresh`
- **Auth:** None

---

### Forgot Password
- **Method:** `POST`
- **URL:** `/api/auth/forgot-password`
- **Auth:** None
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "email": "hanifafghani92@gmail.com"
}
```

---

### Reset Password
- **Method:** `POST`
- **URL:** `/api/auth/reset-password`
- **Auth:** None
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "token": "15b794a9-d44a-456f-a841-bcd7f581667f",
  "newPassword": "newpassword1234",
  "confirmPassword": "newpassword1234"
}
```

---

## Business Profile

### Get Business Profile
- **Method:** `GET`
- **URL:** `/api/business-profile`
- **Auth:** Bearer Token

---

### Save Business Profile
- **Method:** `POST`
- **URL:** `/api/business-profile`
- **Auth:** Bearer Token
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "namaUsaha": "Catering Ibu Sari",
  "kota": "Jakarta",
  "noWhatsapp": "08123456789",
  "email": "catering@example.com",
  "alamat": "Jl. Contoh No. 1",
  "marginDefault": 30,
  "matauang": "IDR",
  "pajakDefault": 10,
  "biayaPengantaranDefault": 50000
}
```

---

### Upload Logo
- **Method:** `POST`
- **URL:** `/api/business-profile/logo`
- **Auth:** Bearer Token
- **Body:** `form-data`

| Key  | Type | Description        |
|------|------|--------------------|
| file | file | Logo image file    |

---

## Dashboard

### Get Dashboard
- **Method:** `GET`
- **URL:** `/api/dashboard`
- **Auth:** Bearer Token
- **Query Params:**

| Parameter | Description          |
|-----------|----------------------|
| year      | Filter by year       |
| month     | Filter by month      |

---

## Ingredients

### Get All Ingredients
- **Method:** `GET`
- **URL:** `/api/ingredients`
- **Auth:** Bearer Token

---

### Search Ingredients
- **Method:** `GET`
- **URL:** `/api/ingredients/search`
- **Auth:** Bearer Token
- **Query Params:**

| Parameter | Description        |
|-----------|--------------------|
| keyword   | Search keyword     |

---

### Get Ingredient by ID
- **Method:** `GET`
- **URL:** `/api/ingredients/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description    |
|----------|---------|----------------|
| id       | 1       | Ingredient ID  |

---

### Create Ingredient
- **Method:** `POST`
- **URL:** `/api/ingredients`
- **Auth:** Bearer Token
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "nama": "Ayam",
  "satuan": "kg",
  "hargaPerSatuan": 35000
}
```

---

### Update Ingredient
- **Method:** `PUT`
- **URL:** `/api/ingredients/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description    |
|----------|---------|----------------|
| id       | 1       | Ingredient ID  |

- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "nama": "Ayam",
  "satuan": "kg",
  "hargaPerSatuan": 35000
}
```

---

### Delete Ingredient
- **Method:** `DELETE`
- **URL:** `/api/ingredients/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description    |
|----------|---------|----------------|
| id       | 1       | Ingredient ID  |

---

## Recipes

### Get All Recipes
- **Method:** `GET`
- **URL:** `/api/recipes`
- **Auth:** Bearer Token

---

### Search Recipes
- **Method:** `GET`
- **URL:** `/api/recipes/search`
- **Auth:** Bearer Token
- **Query Params:**

| Parameter | Description    |
|-----------|----------------|
| keyword   | Search keyword |

---

### Get Recipe by ID
- **Method:** `GET`
- **URL:** `/api/recipes/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description |
|----------|---------|-------------|
| id       | 1       | Recipe ID   |

---

### Create Recipe
- **Method:** `POST`
- **URL:** `/api/recipes`
- **Auth:** Bearer Token
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "namaResep": "Nasi Goreng",
  "jumlahPorsi": 10,
  "margin": 30,
  "hppManual": null,
  "ingredients": [
    {
      "ingredientId": 1,
      "quantity": 2.5
    }
  ]
}
```

---

### Update Recipe
- **Method:** `PUT`
- **URL:** `/api/recipes/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description |
|----------|---------|-------------|
| id       | 1       | Recipe ID   |

- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "namaResep": "Nasi Goreng",
  "jumlahPorsi": 10,
  "margin": 30,
  "hppManual": null,
  "ingredients": [
    {
      "ingredientId": 1,
      "quantity": 2.5
    }
  ]
}
```

---

### Delete Recipe
- **Method:** `DELETE`
- **URL:** `/api/recipes/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description |
|----------|---------|-------------|
| id       | 1       | Recipe ID   |

---

## Notas

### Get All Notas
- **Method:** `GET`
- **URL:** `/api/notas`
- **Auth:** Bearer Token

---

### Get Nota by ID
- **Method:** `GET`
- **URL:** `/api/notas/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description |
|----------|---------|-------------|
| id       | 1       | Nota ID     |

---

### Search Notas
- **Method:** `GET`
- **URL:** `/api/notas/search`
- **Auth:** Bearer Token
- **Query Params:**

| Parameter | Description              |
|-----------|--------------------------|
| keyword   | Search keyword           |
| type      | Filter by nota type      |

---

### Create Nota
- **Method:** `POST`
- **URL:** `/api/notas`
- **Auth:** Bearer Token
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "namaClient": "Budi Santoso",
  "noWaClient": "08123456789",
  "namaAcara": "Pernikahan",
  "tanggalAcara": "2025-12-25",
  "pajakPersen": 10,
  "biayaPengantaran": 50000,
  "items": [
    {
      "recipeId": 1,
      "jumlahPorsi": 100
    }
  ]
}
```

---

### Update Nota Status
- **Method:** `PATCH`
- **URL:** `/api/notas/:id/status`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description |
|----------|---------|-------------|
| id       | 1       | Nota ID     |

- **Query Params:**

| Parameter | Description        |
|-----------|--------------------|
| status    | New status value   |

---

### Delete Nota
- **Method:** `DELETE`
- **URL:** `/api/notas/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description |
|----------|---------|-------------|
| id       | 1       | Nota ID     |

---

### Download Nota PDF
- **Method:** `GET`
- **URL:** `/api/notas/:id/pdf`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description |
|----------|---------|-------------|
| id       | 1       | Nota ID     |

---

## Shopping Lists

### Get All Shopping Lists
- **Method:** `GET`
- **URL:** `/api/shopping-lists`
- **Auth:** Bearer Token

---

### Get Shopping List by ID
- **Method:** `GET`
- **URL:** `/api/shopping-lists/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description       |
|----------|---------|-------------------|
| id       | 1       | Shopping List ID  |

---

### Generate Shopping List
- **Method:** `POST`
- **URL:** `/api/shopping-lists/generate`
- **Auth:** Bearer Token
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "recipeIds": [1, 2]
}
```

---

### Update Item Bought Status
- **Method:** `PATCH`
- **URL:** `/api/shopping-lists/:id/items/:itemId/bought`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description       |
|----------|---------|-------------------|
| id       | 1       | Shopping List ID  |
| itemId   | 1       | Item ID           |

- **Query Params:**

| Parameter | Description                  |
|-----------|------------------------------|
| isBought  | Boolean — bought status      |

---

### Add Item to Shopping List
- **Method:** `POST`
- **URL:** `/api/shopping-lists/:id/items`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description       |
|----------|---------|-------------------|
| id       | 1       | Shopping List ID  |

- **Query Params:**

| Parameter    | Description         |
|--------------|---------------------|
| ingredientId | Ingredient ID       |
| quantity     | Quantity to add     |

---

### Delete Shopping List
- **Method:** `DELETE`
- **URL:** `/api/shopping-lists/:id`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description       |
|----------|---------|-------------------|
| id       | 1       | Shopping List ID  |

---

### Download Shopping List PDF
- **Method:** `GET`
- **URL:** `/api/shopping-lists/:id/pdf`
- **Auth:** Bearer Token
- **Path Variables:**

| Variable | Example | Description       |
|----------|---------|-------------------|
| id       | 1       | Shopping List ID  |

---

## Subscriptions

### Create Subscription Transaction
- **Method:** `POST`
- **URL:** `/api/subscriptions/create`
- **Auth:** Bearer Token

---

### Get Active Subscription
- **Method:** `GET`
- **URL:** `/api/subscriptions/active`
- **Auth:** Bearer Token

---

### Get Subscription History
- **Method:** `GET`
- **URL:** `/api/subscriptions/history`
- **Auth:** Bearer Token

---

### Subscription Webhook
- **Method:** `POST`
- **URL:** `/api/subscriptions/webhook`
- **Auth:** None
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "order_id": "ORDER-123",
  "transaction_status": "settlement",
  "payment_type": "bank_transfer"
}
```

---

*Generated from Katerly API collection — 2026-05-15*
