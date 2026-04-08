# SuperComp – Entregable 1: Estructura de Datos

## Descripción de la App

**SuperComp** es una app Android para comparar precios de productos del supermercado.
El usuario puede buscar un producto (por ejemplo, "Leche Entera 1L") y ver el precio
en distintos supermercados (Mercadona, Lidl, Dia…), crear listas de la compra guardadas,
guardar favoritos y dejar comentarios/valoraciones.

**Stack:** Node.js + Express (backend), MongoDB Atlas (base de datos remota), Android (Kotlin + Jetpack Compose + Retrofit).

---

## Colecciones y Estructura de Datos

### 1. `users`
Representa a los usuarios registrados en la app.

| Campo       | Tipo     | Obligatorio | Descripción                      |
|-------------|----------|-------------|----------------------------------|
| `_id`       | ObjectId | ✅ (auto)   | Identificador único (MongoDB)    |
| `name`      | String   | ✅          | Nombre del usuario               |
| `email`     | String   | ✅ único    | Email para login                 |
| `password`  | String   | ✅          | Contraseña hasheada (bcrypt)     |
| `avatar`    | String   | ❌          | URL de foto de perfil (opcional) |
| `createdAt` | Date     | ✅ (auto)   | Fecha de registro                |
| `updatedAt` | Date     | ✅ (auto)   | Última modificación              |

**Ejemplo real:**
```json
{
  "_id": { "$oid": "692dd095a1327564ac6d9fdc" },
  "name": "Cliente",
  "email": "cliente@gmail.com",
  "password": "$2b$10$wCJY.t0y9RaNiOzLUhUu...",
  "avatar": "",
  "createdAt": { "$date": "2026-01-19T16:33:20.245Z" },
  "updatedAt": { "$date": "2026-01-19T16:33:20.245Z" }
}
```

---

### 2. `products`
Representa un producto con su precio en un supermercado concreto.

| Campo         | Tipo     | Obligatorio | Descripción                        |
|---------------|----------|-------------|------------------------------------|
| `_id`         | ObjectId | ✅ (auto)   | Identificador único                |
| `name`        | String   | ✅          | Nombre del producto                |
| `supermarket` | String   | ✅          | Nombre del supermercado            |
| `price`       | Double   | ✅          | Precio en euros                    |

**Ejemplo real:**
```json
{
  "_id": { "$oid": "68ddbffdf8d47ba5f81eac7a" },
  "name": "Leche Entera 1L",
  "supermarket": "Mercadona",
  "price": 1.01
}
```

> El mismo producto (misma `name`) puede aparecer en varios documentos con distinto
> `supermarket` y `price`. Así se realiza la comparación de precios.

---

### 3. `comments`
Almacena los comentarios/feedback que los usuarios envían desde la app.

| Campo       | Tipo     | Obligatorio | Descripción              |
|-------------|----------|-------------|--------------------------|
| `_id`       | ObjectId | ✅ (auto)   | Identificador único      |
| `name`      | String   | ✅          | Nombre del autor         |
| `message`   | String   | ✅          | Contenido del comentario |
| `createdAt` | Date     | ✅ (auto)   | Fecha de envío           |

**Ejemplo real:**
```json
{
  "_id": { "$oid": "..." },
  "name": "test2",
  "message": "good work",
  "createdAt": { "$date": "2025-12-31T20:17:31.019Z" }
}
```

---

### 4. `shoppinglists`
Representa una lista de la compra guardada por un usuario para comparar precios.

| Campo       | Tipo       | Obligatorio | Descripción                            |
|-------------|------------|-------------|----------------------------------------|
| `_id`       | ObjectId   | ✅ (auto)   | Identificador único                    |
| `user`      | ObjectId   | ✅          | Referencia al usuario (`users._id`)    |
| `name`      | String     | ✅          | Nombre de la lista                     |
| `items`     | [String]   | ✅          | Lista de nombres de productos          |
| `createdAt` | Date       | ✅ (auto)   | Fecha de creación                      |
| `updatedAt` | Date       | ✅ (auto)   | Última modificación                    |

**Ejemplo real:**
```json
{
  "_id": { "$oid": "..." },
  "user": { "$oid": "696ab0054f560dd4a0304bb7" },
  "name": "lista 2",
  "items": ["Aceite de Girasol 1L", "Arroz Redondo 1kg", "Leche Entera 1L"],
  "createdAt": { "$date": "2026-01-16T22:02:19.375Z" },
  "updatedAt": { "$date": "2026-01-16T22:02:19.375Z" }
}
```

---

### 5. `wishlists`
Registra los productos que un usuario ha marcado como favorito.

| Campo       | Tipo     | Obligatorio | Descripción                              |
|-------------|----------|-------------|------------------------------------------|
| `_id`       | ObjectId | ✅ (auto)   | Identificador único                      |
| `userId`    | ObjectId | ✅          | Referencia al usuario (`users._id`)      |
| `productId` | ObjectId | ✅          | Referencia al producto (`products._id`)  |

**Ejemplo real:**
```json
{
  "_id": { "$oid": "..." },
  "userId":    { "$oid": "692dd095a1327564ac6d9fdc" },
  "productId": { "$oid": "68ddbffdf8d47ba5f81eac7a" }
}
```

---

## Relaciones entre Colecciones

```
users (1) ──────────────< shoppinglists (N)
  │                         (user → users._id)
  │
  └──────────────────────< wishlists (N)
                             (userId → users._id)
                             (productId → products._id)

products (1) ────────────< wishlists (N)
  │                         (productId → products._id)
  │
  └── (mismo name en varios documentos = comparación entre supermercados)
```

| Relación                        | Cardinalidad | Clave foránea          |
|---------------------------------|--------------|------------------------|
| users → shoppinglists           | 1:N          | `shoppinglists.user`   |
| users → wishlists               | 1:N          | `wishlists.userId`     |
| products → wishlists            | 1:N          | `wishlists.productId`  |
| products (mismo nombre)         | N:M lógico   | campo `name` compartido|

---

## Data Classes en Kotlin

Cada colección tiene su data class con `@SerializedName` para mapear correctamente
los campos de la API REST (Gson/Retrofit).

```kotlin
// Product.kt
data class Product(
    @SerializedName("_id")         val id: String = "",
    @SerializedName("name")        val name: String,
    @SerializedName("supermarket") val supermarket: String,
    @SerializedName("price")       val price: Double
)

// User.kt
data class User(
    @SerializedName("_id")       val id: String = "",
    @SerializedName("name")      val name: String,
    @SerializedName("email")     val email: String,
    @SerializedName("password")  val password: String = "",
    @SerializedName("avatar")    val avatar: String = "",
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

// Comment.kt
data class Comment(
    @SerializedName("_id")       val id: String = "",
    @SerializedName("name")      val name: String,
    @SerializedName("message")   val message: String,
    @SerializedName("createdAt") val createdAt: String = ""
)

// ShoppingList.kt
data class ShoppingList(
    @SerializedName("_id")       val id: String = "",
    @SerializedName("user")      val userId: String,
    @SerializedName("name")      val name: String,
    @SerializedName("items")     val items: List<String>,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

// Wishlist.kt
data class Wishlist(
    @SerializedName("_id")       val id: String = "",
    @SerializedName("userId")    val userId: String,
    @SerializedName("productId") val productId: String
)
```

---

## Repositorios (Capa de Acceso a Datos)

Cada entidad tiene su propio repositorio. Ninguna Activity ni Composable accede
directamente a la API — todo pasa por el repositorio correspondiente.

| Repositorio              | Operaciones implementadas                                      |
|--------------------------|----------------------------------------------------------------|
| `ProductRepository`      | `getAllProducts()`, `getProductsBySupermarket()`, `searchProducts()` |
| `CommentRepository`      | `getAllComments()`, `postComment()`                             |
| `ShoppingListRepository` | `getListsByUser()`, `createList()`, `deleteList()`             |
| `WishlistRepository`     | `getWishlistByUser()`, `addToWishlist()`, `removeFromWishlist()` |

---

## Caso de Uso Demostrado (Demo Screen)

**Flujo completo de la capa de datos:**

1. **WRITE** – El usuario escribe un comentario → `CommentRepository.postComment()` → POST `/comments` → guardado en MongoDB.
2. **READ** – El usuario pulsa "Load All Products" → `ProductRepository.getAllProducts()` → GET `/products` → lista completa mostrada en pantalla.
3. **FILTER** – El usuario pulsa "Mercadona" → `ProductRepository.getProductsBySupermarket("Mercadona")` → GET `/products/supermarket/Mercadona` → solo productos de ese supermercado.

Todos los resultados se muestran en tiempo real en la UI sin que la pantalla acceda
directamente a la API.
