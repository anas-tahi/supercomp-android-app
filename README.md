# SuperComp 🛒

**SuperComp** is an Android app that helps users compare grocery prices across Spain's top supermarkets — Mercadona, Lidl, Carrefour, and Alcampo — in real time.

---

## 📱 Features

- **Price Comparison** — Search any product and instantly compare prices across all 4 supermarkets
- **Shopping List** — Build a cart and see the total cost per supermarket, sorted cheapest to most expensive
- **Favourites** — Save products to a personal wishlist with one tap
- **Nearby Map** — Find the nearest supermarket branches using Google Maps
- **Community Feed** — Read and post comments on the Home screen
- **Profile Management** — Edit username, password, phone, city and profile photo
- **Voice Search** — Search products by voice using Android's speech recognition
- **JWT Authentication** — Secure login and registration with persistent session via DataStore

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Android | Kotlin, Jetpack Compose, MVVM |
| Navigation | Jetpack Navigation Compose |
| Networking | Retrofit 2, OkHttp, Gson |
| Local Storage | Jetpack DataStore (Preferences) |
| Images | Coil |
| Maps | Google Maps SDK for Android |
| Backend | Node.js, Express.js |
| Database | MongoDB Atlas |
| Auth | JWT (JSON Web Token) |

---

## 🏗 Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with a clean separation of layers:

```
UI Layer       →  Composable screens + BottomBar
ViewModel      →  State management, business logic
Data Layer     →  RetrofitClient (remote) + DataStore (local)
```

Package structure:
```
com.supercomp.android/
├── data/
│   ├── local/       UserPrefs.kt (DataStore)
│   ├── model/       Models.kt (data classes)
│   └── remote/      ApiService.kt, RetrofitClient.kt
├── ui/
│   ├── auth/        login/, register/
│   ├── components/  BottomBar, MapUtils, VoiceRecognizer
│   ├── navigation/  AppNavGraph.kt
│   └── screens/     home/, compare/, favorites/, shoppinglist/, map/, profile/
└── MainActivity.kt
```

---

## 🚀 Getting Started

### Android App

1. Clone this repo
2. Open `androidapp/` in Android Studio
3. Create `androidapp/local.properties` with your SDK path:
   ```
   sdk.dir=/path/to/your/Android/Sdk
   ```
4. Add your Google Maps API key in `AndroidManifest.xml`
5. Update `BASE_URL` in `RetrofitClient.kt` to point to your backend
6. Run on a device or emulator (API 24+)

### Backend

1. Go to `supercomp-backend/`
2. Copy `.env.example` to `.env` and fill in your values:
   ```
   MONGO_URI=your_mongodb_connection_string
   PORT=5000
   JWT_SECRET=your_secret
   ```
3. Install dependencies and run:
   ```bash
   npm install
   node server.js
   ```

---

## 📂 Repository Structure

```
supercomp-android-app/
├── androidapp/          Android Kotlin project
├── supercomp-backend/   Node.js + Express API
└── docs/                Course deliverables (Entregable 1 & 2)
```

---

## 👨‍💻 Author

**Anas Tahir** — Android Development Course, 2025–2026
