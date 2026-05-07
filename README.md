# SuperComp 🛒

A price comparison Android app for Spanish supermarkets (Mercadona, Lidl, Dia, Carrefour).

## Tech Stack

- **Android**: Kotlin + Jetpack Compose + Retrofit + Jetpack Navigation
- **Backend**: Node.js + Express
- **Database**: MongoDB Atlas

## Project Structure

```
supercomp-android-app/
├── androidapp/          # Android Studio project (Kotlin + Compose)
├── supercomp-backend/   # Node.js + Express REST API
└── docs/                # Documentation and PDFs
```

---

## Entregable 1 — Data Layer & Collections

> Full documentation: `docs/entregable1_documentacion.md`

This deliverable defines the data layer of the app: MongoDB collections, their fields, relationships, Kotlin data classes, and the repository pattern.

### Collections

| Collection | Description |
|------------|-------------|
| `users` | Registered users (username, email, hashed password) |
| `products` | Products with price per supermarket — same product name appears multiple times, one per supermarket, enabling price comparison |
| `comments` | Community feedback posted from the app |
| `shoppinglists` | Named shopping lists saved per user, containing product references |
| `wishlists` | Products a user has marked as favourite |

### Relationships

```
users (1) ──────< shoppinglists (N)   [shoppinglists.user → users._id]
users (1) ──────< wishlists (N)       [wishlists.userId → users._id]
products (1) ───< wishlists (N)       [wishlists.productId → products._id]
products (same name, N supermarkets)  → price comparison logic
```

### Repositories

Each entity has its own repository. No screen or composable calls the API directly.

| Repository | Operations |
|------------|------------|
| `ProductRepository` | `getAllProducts()`, `getProductsBySupermarket()`, `searchProducts()` |
| `CommentRepository` | `getAllComments()`, `postComment()` |
| `ShoppingListRepository` | `getListsByUser()`, `createList()`, `deleteList()` |
| `WishlistRepository` | `getWishlistByUser()`, `addToWishlist()`, `removeFromWishlist()` |

### Demo use case

1. **WRITE** — user posts a comment → `CommentRepository.postComment()` → `POST /comments` → saved in MongoDB
2. **READ** — user loads products → `ProductRepository.getAllProducts()` → `GET /products` → full list displayed
3. **FILTER** — user selects Mercadona → `ProductRepository.getProductsBySupermarket("Mercadona")` → filtered results shown

---

## Entregable 1 — Capa de Datos y Colecciones _(Español)_

> Documentación completa: `docs/entregable1_documentacion.md`

Este entregable define la capa de datos de la app: colecciones MongoDB, sus campos, relaciones, data classes en Kotlin y el patrón repositorio.

### Colecciones

| Colección | Descripción |
|-----------|-------------|
| `users` | Usuarios registrados (username, email, contraseña hasheada) |
| `products` | Productos con precio por supermercado — el mismo nombre de producto aparece varias veces, una por supermercado, permitiendo la comparación de precios |
| `comments` | Comentarios de la comunidad enviados desde la app |
| `shoppinglists` | Listas de la compra guardadas por usuario, con referencias a productos |
| `wishlists` | Productos marcados como favoritos por el usuario |

### Relaciones

```
users (1) ──────< shoppinglists (N)   [shoppinglists.user → users._id]
users (1) ──────< wishlists (N)       [wishlists.userId → users._id]
products (1) ───< wishlists (N)       [wishlists.productId → products._id]
products (mismo nombre, N supermercados) → lógica de comparación de precios
```

### Repositorios

Cada entidad tiene su propio repositorio. Ninguna pantalla ni composable llama directamente a la API.

| Repositorio | Operaciones |
|-------------|-------------|
| `ProductRepository` | `getAllProducts()`, `getProductsBySupermarket()`, `searchProducts()` |
| `CommentRepository` | `getAllComments()`, `postComment()` |
| `ShoppingListRepository` | `getListsByUser()`, `createList()`, `deleteList()` |
| `WishlistRepository` | `getWishlistByUser()`, `addToWishlist()`, `removeFromWishlist()` |

### Caso de uso demostrado

1. **WRITE** — el usuario escribe un comentario → `CommentRepository.postComment()` → `POST /comments` → guardado en MongoDB
2. **READ** — el usuario carga productos → `ProductRepository.getAllProducts()` → `GET /products` → lista mostrada en pantalla
3. **FILTER** — el usuario selecciona Mercadona → `ProductRepository.getProductsBySupermarket("Mercadona")` → resultados filtrados

---

## Entregable 2 — Navigation & Screen Flow

> Full documentation: `docs/SuperComp_Entregable2.pdf`

This deliverable focuses on defining and implementing the complete navigation structure of the app — screens, routes, transitions, argument passing, and a coherent user flow.

### Navigation Technology

| Library | `androidx.navigation.compose` |
|---------|-------------------------------|
| Container | `NavHost` — defines all destinations and routes |
| Controller | `NavController` — manages the navigation back stack |
| Main navigation | `BottomBar` — bottom bar with routes to main screens |
| Data passing | `navArgument` — username and userId passed via route to each screen |

### Screens & Routes

| Screen | Route | Type | Arguments |
|--------|-------|------|-----------|
| Splash Screen | `splash` | Entry screen | None |
| Login Screen | `login` | startDestination | None |
| Register Screen | `register` | Secondary auth screen | None |
| Home Screen | `home/{username}/{userId}` | Main screen (hub) | username (String, req), userId (String, req) |
| Compare Screen | `compare/{username}/{userId}` | BottomBar tab | username (String, req), userId (String, req) |
| Favorites Screen | `favorites/{username}/{userId}` | BottomBar tab | username (String, req), userId (String, req) |
| Shopping List Screen | `shoppinglist/{username}/{userId}` | BottomBar tab | username (String, req), userId (String, req) |
| Profile Screen | `profile/{username}/{userId}` | BottomBar tab | username (String, req), userId (String, req) |

### Navigation Flow

```
APP LAUNCH
    │
    ▼
Splash Screen (4s)
    │
    ├── Has saved JWT token? ──YES──► Home Screen
    │
    └── NO
        │
        ▼
    Login Screen ──────────────────────────────────────────────┐
        │                                                       │
        ├── login OK ──► Home Screen (popUpTo login)            │
        │                    │                                  │
        └── no account       │  BottomBar                       │
            │                ├──► Compare                       │
            ▼                ├──► Favourites                    │
        Register             ├──► Shopping List                 │
            │                └──► Profile ──── logout ──────────┘
            └── back/success
                ▼
            Login Screen
```

- **Initial screen**: `splash` → checks saved session → routes to `login` or `home`
- **Session persistence**: JWT token stored in DataStore — auto-login if token found
- **After login**: navigates to `home/{username}/{userId}`, login removed from back stack with `popUpTo("login"){ inclusive = true }`
- **Bottom nav bar**: available on all main screens; passes `username` and `userId` on every transition
- **Logout**: clears DataStore → navigates to Login with `popUpTo(0){ inclusive = true }` — full stack cleared

### Screen Details

**Splash Screen** — `splash`
Shows the SuperComp logo centred on a dark background with a loading indicator. Navigates automatically after 4 seconds. Checks for a saved JWT token in DataStore.

**Login Screen** — `login`
Fields: email · password · login button · link to Register. On success → `POST /auth/login` → token + username + userId saved in DataStore → navigate to Home.

**Register Screen** — `register`
Fields: username · email · password · Register button · link to Login. On success → `POST /auth/register` → navigate to Login. Back → `popBackStack()` → Login.

**Home Screen** — `home/{username}/{userId}`
Shows: animated banner with username · stats indicators · supermarket filter chips (Mercadona / Lidl / Carrefour / Alcampo) · best deals with images · community comments feed · feedback input field.
Actions: filter by supermarket · toggle favourite (`POST /wishlist`) · post comment (`POST /comments`) · navigate via BottomBar.
Back: root screen — back button has no effect.

**Compare Screen** — `compare/{username}/{userId}`
Shows: search bar · product cards grouped by name · price per supermarket · "Best price" badge on cheapest · product image · heart button.
Actions: search → `GET /products/search?name=…` · toggle favourite (`POST /wishlist`).

**Favourites Screen** — `favorites/{username}/{userId}`
Shows: saved products with image · name · supermarket badge · price in green · delete button. Empty state shown when no favourites.
Actions: delete → `DELETE /wishlist/:id`.

**Shopping List Screen** — `shoppinglist/{username}/{userId}`
Two tabs — **Create & Compare** and **Saved Lists**.
Create tab: search products · add to basket · remove items · compare total per supermarket sorted cheapest first · save list (`POST /shoppinglists`).
Saved Lists tab: named lists with product thumbnails · delete list (`DELETE /shoppinglists/:id`).

**Profile Screen** — `profile/{username}/{userId}`
Shows: avatar circle with initial · username · SuperComp member label · Edit Profile button · info table (username / userId / version v3.0) · Logout button.
Actions: Edit Profile → dialog to change username or password → `PUT /auth/profile/:userId`. Logout → clear DataStore → navigate to Login.

### Argument Flow

`username` and `userId` are captured at login and passed through all routes. No global state is used.

| From | To | Arguments | Purpose |
|------|----|-----------|---------|
| Login | Home | username, userId | Identify the authenticated user |
| Home | Compare | username, userId | Keep session active across BottomBar |
| Home | Favourites | username, userId | Load this user's wishlist |
| Home | Shopping List | username, userId | Save/load this user's lists |
| Home | Profile | username, userId | Display and edit user data |
| Profile | Login | (none) | Logout — stack fully cleared |

### Back Button Behaviour

| Screen | Behaviour | Implementation |
|--------|-----------|----------------|
| Splash | Auto-navigates after 4s, no back button | `LaunchedEffect + delay(4000)` |
| Login | Back closes the app (it's the root) | `startDestination` — no popBackStack |
| Register | Back → Login | `popBackStack()` |
| After login | Login removed from stack | `popUpTo("login"){ inclusive = true }` |
| Home | Root — back has no effect | Root of main NavHost graph |
| Compare / Favourites / Shopping List / Profile | Back → previous screen | Default Android back gesture |
| After logout | Full stack cleared → Login | `popUpTo(0){ inclusive = true }` |

### Complete Use Case — Open app, compare prices, create a shopping list, save a favourite, logout

| Step | Action |
|------|--------|
| 1 | App launch → Splash (4s) → no token → Login screen |
| 2 | Enter email + password → `POST /auth/login` → token + userId saved in DataStore |
| 3 | Navigate to `home/anas tahir/69d6c4cf` with `popUpTo("login"){ inclusive = true }` |
| 4 | Home loads — banner shows "Hola, anas tahir" · best deals loaded with images |
| 5 | Tap heart on a deal → `POST /wishlist { userId, productId }` |
| 6 | BottomBar → Compare → `navigate("compare/anas tahir/69d6c4cf")` |
| 7 | Search "leche" → `GET /products/search?name=leche` → prices shown per supermarket |
| 8 | BottomBar → Shopping List → `navigate("shoppinglist/anas tahir/69d6c4cf")` |
| 9 | Search "arroz" → tap + · Search "aceite" → tap + · basket shows both products |
| 10 | Compare tab → totals for Mercadona / Lidl / Carrefour / Alcampo · cheapest highlighted in green |
| 11 | Tap "Guardar lista" → dialog → name "Lista semanal" → `POST /shoppinglists` |
| 12 | Saved Lists tab → "Lista semanal" appears with product thumbnails |
| 13 | BottomBar → Profile → `navigate("profile/anas tahir/69d6c4cf")` |
| 14 | Tap Logout → DataStore cleared → `navigate("login"){ popUpTo(0){ inclusive = true } }` |

---

## Entregable 2 — Navegación y Flujo de Pantallas _(Español)_

> Documentación completa: `docs/SuperComp_Entregable2.pdf`

### Tecnología de Navegación

| Librería | `androidx.navigation.compose` |
|----------|-------------------------------|
| Contenedor | `NavHost` — define todos los destinos y rutas |
| Controlador | `NavController` — gestiona el stack de navegación |
| Navegación principal | `BottomBar` — barra inferior con rutas a las pantallas principales |
| Paso de datos | `navArgument` — username y userId pasados por ruta a cada pantalla |

### Rutas de Navegación

| Pantalla | Ruta exacta (NavHost) | Tipo | Argumentos |
|----------|-----------------------|------|------------|
| Splash Screen | `splash` | Pantalla de entrada | Ninguno |
| Login Screen | `login` | startDestination | Ninguno |
| Register Screen | `register` | Pantalla secundaria de auth | Ninguno |
| Home Screen | `home/{username}/{userId}` | Pantalla principal (hub) | username (String, req), userId (String, req) |
| Compare Screen | `compare/{username}/{userId}` | Tab — BottomBar | username (String, req), userId (String, req) |
| Favorites Screen | `favorites/{username}/{userId}` | Tab — BottomBar | username (String, req), userId (String, req) |
| Shopping List Screen | `shoppinglist/{username}/{userId}` | Tab — BottomBar | username (String, req), userId (String, req) |
| Profile Screen | `profile/{username}/{userId}` | Tab — BottomBar | username (String, req), userId (String, req) |

### Flujo de Navegación

```
INICIO DE LA APP
    │
    ▼
Splash Screen (4s)
    │
    ├── ¿Hay token JWT guardado? ──SÍ──► Home Screen
    │
    └── NO
        │
        ▼
    Login Screen ──────────────────────────────────────────────┐
        │                                                       │
        ├── login OK ──► Home Screen (popUpTo login)            │
        │                    │                                  │
        └── sin cuenta       │  BottomBar                       │
            │                ├──► Comparar                      │
            ▼                ├──► Favoritos                     │
        Registro             ├──► Lista de la compra            │
            │                └──► Perfil ──── logout ───────────┘
            └── back/éxito
                ▼
            Login Screen
```

- **Pantalla inicial**: `splash` → comprueba sesión guardada → redirige a `login` o `home`
- **Sesión persistente**: token JWT guardado en DataStore — auto-login si se encuentra token
- **Tras el login**: navega a `home/{username}/{userId}`, login eliminado del stack con `popUpTo("login"){ inclusive = true }`
- **Barra de navegación inferior**: disponible en todas las pantallas principales; pasa `username` y `userId` en cada transición
- **Logout**: limpia DataStore → navega al Login con `popUpTo(0){ inclusive = true }` — stack completamente limpiado

### Comportamiento del Botón Atrás

| Pantalla | Comportamiento | Implementación |
|----------|---------------|----------------|
| Splash | Navega automáticamente tras 4s, sin botón atrás | `LaunchedEffect + delay(4000)` |
| Login | Atrás cierra la app (es la raíz) | `startDestination` — no popBackStack |
| Register | Atrás → Login | `popBackStack()` |
| Tras el login | Login eliminado del stack | `popUpTo("login"){ inclusive = true }` |
| Home | Raíz — el botón atrás no tiene efecto | Raíz del grafo principal NavHost |
| Compare / Favoritos / Lista / Perfil | Atrás → pantalla anterior | Gesto atrás por defecto de Android |
| Tras cerrar sesión | Stack completo limpiado → Login | `popUpTo(0){ inclusive = true }` |
