\# SuperComp 🛒



A price comparison Android app for Spanish supermarkets (Mercadona, Lidl, Dia, Carrefour).



\## Description

SuperComp lets users compare product prices across different supermarkets, save shopping lists, mark favourites, and leave feedback.



\## Tech Stack

\- \*\*Android\*\*: Kotlin + Jetpack Compose + Retrofit

\- \*\*Backend\*\*: Node.js + Express

\- \*\*Database\*\*: MongoDB Atlas



\## Project Structure

supercomp-android-app/

├── androidapp/          # Android Studio project

├── supercomp-backend/   # Node.js + Express API

└── docs/                # Documentation



\## Entregable 1

Data layer documentation and implementation.

See `docs/entregable1\_documentacion.md`



\## Entregable 2 — Android App + Full Backend Integration

Full-stack delivery of the SuperComp app, connecting the Android client to the Node.js/MongoDB backend via a REST API.

See `docs/SuperComp\_Entregable2.pdf`



\### What was built

\#### Android App (Kotlin + Jetpack Compose)

Five screens, each with its own ViewModel and API integration:

\- **Home** — browse all products, filter by supermarket (Mercadona, Lidl, Dia, Carrefour), view community comments and post new ones, toggle favourites
\- **Compare** — search products by name and compare prices across supermarkets side by side, sorted cheapest first
\- **Shopping List** — build a cart from all available products, save named lists per user, delete saved lists; includes a compare tab showing the cheapest supermarket for the full basket
\- **Favourites** — view and remove wishlisted products
\- **Profile** — view account info, edit username, change password



\#### Backend API (Node.js + Express + MongoDB Atlas)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and receive JWT |
| GET | `/auth/profile/:userId` | Get user profile |
| PUT | `/auth/profile/:userId` | Update username or password |
| GET | `/products` | Get all products |
| GET | `/products/supermarket/:name` | Get products by supermarket |
| GET | `/products/search?name=` | Search products by name |
| GET | `/comments` | Get all community comments |
| POST | `/comments` | Post a new comment |
| GET | `/shoppinglists/user/:userId` | Get user's saved lists |
| POST | `/shoppinglists` | Save a new shopping list |
| DELETE | `/shoppinglists/:id` | Delete a shopping list |
| GET | `/wishlist/user/:userId` | Get user's wishlist |
| POST | `/wishlist` | Add product to wishlist |
| DELETE | `/wishlist/:id` | Remove from wishlist |



\### Key Data Models

\- **Product** — name, supermarket, price, category, imageUrl
\- **User** — username, email, hashed password, JWT auth
\- **ShoppingList** — userId, name, list of product IDs
\- **Wishlist** — userId, productId
\- **Comment** — username, message, createdAt



\### Local session persistence

User credentials (token, userId, username, email) are stored locally using **DataStore Preferences** via `UserPrefs`, so users stay logged in between sessions.

