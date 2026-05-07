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

## Entregable 1

Data layer documentation and initial implementation.
See `docs/entregable1_documentacion.md`

---

## Entregable 2 — Navigation & Screen Flow

> Full documentation: `docs/SuperComp_Entregable2.pdf`

This deliverable focuses on defining and implementing the complete navigation structure of the app — screens, routes, transitions, argument passing, and a coherent user flow.

### Screens & Routes

| Screen | Route | Arguments | Description |
|--------|-------|-----------|-------------|
| Login | `login` | — | Initial screen. User enters email and password. |
| Register | `register` | — | New account creation. Returns to Login on success. |
| Home | `home/{username}/{userId}` | username (String), userId (String) | Main screen after login. Browse products, filter by supermarket, post comments, toggle favourites. |
| Compare | `compare/{username}/{userId}` | username (String), userId (String) | Search products by name and compare prices across supermarkets. |
| Shopping List | `shoppinglist/{username}/{userId}` | username (String), userId (String) | Build a basket, compare total cost per supermarket, save and delete lists. |
| Favourites | `favorites/{username}/{userId}` | username (String), userId (String) | View and remove wishlisted products. |
| Profile | `profile/{username}/{userId}` | username (String), userId (String) | View account info, edit username, change password. |

### Navigation Flow

```
Login ──► Home ◄──────────────────────────┐
  │        │  (Bottom Nav Bar)             │
  ▼        ├──► Compare                    │
Register   ├──► Shopping List              │
           ├──► Favourites                 │
           └──► Profile ──► (Logout) ──► Login
```

- **Initial screen**: `login`
- **After login**: navigates to `home/{username}/{userId}`, login is removed from the back stack
- **Bottom nav bar**: available on all main screens; passes `username` and `userId` on every transition
- **Back behaviour**: pressing back from any main screen exits the app; back from Register returns to Login
- **Logout**: clears the DataStore session and navigates back to Login, clearing the entire back stack

### Main Use Case — Compare prices and save a shopping list

1. User opens the app → **Login** screen
2. Enters credentials → navigates to **Home**
3. Browses products, marks some as favourites
4. Goes to **Shopping List** via the bottom bar
5. Selects products → sees cheapest supermarket for the full basket
6. Saves the list with a name → list persists in the backend
7. Goes to **Compare** → searches a specific product → sees prices sorted cheapest first
8. Goes to **Profile** → edits username → logs out → back to Login

### Argument passing

Every post-login screen receives `username` and `userId` as route arguments (type: `String`, required). These are used to scope API calls (shopping lists, wishlist, profile) to the logged-in user and are stored locally via **DataStore Preferences** (`UserPrefs`) for session persistence between app launches.

---

## Entregable 2 — Navegación y Flujo de Pantallas _(Español)_

> Documentación completa: `docs/SuperComp_Entregable2.pdf`

Este entregable se centra en definir e implementar la estructura de navegación completa de la app: pantallas, rutas, transiciones, paso de argumentos y un flujo de usuario coherente.

### Pantallas y rutas

| Pantalla | Ruta | Argumentos | Descripción |
|----------|------|------------|-------------|
| Login | `login` | — | Pantalla inicial. El usuario introduce email y contraseña. |
| Registro | `register` | — | Creación de cuenta nueva. Vuelve al Login al completar. |
| Home | `home/{username}/{userId}` | username (String), userId (String) | Pantalla principal tras el login. Ver productos, filtrar por supermercado, comentar, marcar favoritos. |
| Comparar | `compare/{username}/{userId}` | username (String), userId (String) | Buscar productos por nombre y comparar precios entre supermercados. |
| Lista de la compra | `shoppinglist/{username}/{userId}` | username (String), userId (String) | Crear cesta, comparar coste total por supermercado, guardar y borrar listas. |
| Favoritos | `favorites/{username}/{userId}` | username (String), userId (String) | Ver y eliminar productos de la wishlist. |
| Perfil | `profile/{username}/{userId}` | username (String), userId (String) | Ver datos de la cuenta, editar nombre de usuario, cambiar contraseña. |

### Flujo de navegación

```
Login ──► Home ◄──────────────────────────┐
  │        │  (Barra de navegación)        │
  ▼        ├──► Comparar                   │
Registro   ├──► Lista de la compra         │
           ├──► Favoritos                  │
           └──► Perfil ──► (Logout) ──► Login
```

- **Pantalla inicial**: `login`
- **Tras el login**: navega a `home/{username}/{userId}`, el login se elimina del back stack
- **Barra de navegación inferior**: disponible en todas las pantallas principales; pasa `username` y `userId` en cada transición
- **Comportamiento al volver**: desde cualquier pantalla principal, el botón atrás cierra la app; desde Registro vuelve al Login
- **Logout**: limpia la sesión en DataStore y navega al Login vaciando el back stack

### Caso de uso principal — Comparar precios y guardar lista de la compra

1. El usuario abre la app → pantalla de **Login**
2. Introduce sus credenciales → navega a **Home**
3. Explora productos, marca algunos como favoritos
4. Va a **Lista de la compra** desde la barra inferior
5. Selecciona productos → ve el supermercado más barato para toda la cesta
6. Guarda la lista con un nombre → persiste en el backend
7. Va a **Comparar** → busca un producto concreto → ve precios ordenados de menor a mayor
8. Va a **Perfil** → edita su nombre de usuario → hace logout → vuelve al Login

### Paso de argumentos

Todas las pantallas posteriores al login reciben `username` y `userId` como argumentos de ruta (tipo: `String`, obligatorios). Se usan para asociar las llamadas a la API (listas, wishlist, perfil) al usuario autenticado, y se almacenan localmente con **DataStore Preferences** (`UserPrefs`) para mantener la sesión entre aperturas de la app.
