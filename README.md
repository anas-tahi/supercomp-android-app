# SuperComp 🛒

> Compare grocery prices across **Mercadona, Lidl, Carrefour and Alcampo** in real time — all in one Android app.

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white)

</div>

---

## 📱 Features

- 🔍 Search any product and instantly compare prices across 4 supermarkets
- 📍 Google Maps integration — find the nearest store with the best price
- 🔐 JWT authentication (register/login)
- 💾 Persistent preferences with DataStore
- 🧱 Clean MVVM architecture

## 🏗️ Architecture

```
Frontend (Android)          Backend (Node.js)
─────────────────           ─────────────────
Jetpack Compose UI    ───▶  Express REST API
ViewModel + LiveData        JWT Auth
Retrofit HTTP client  ◀───  MongoDB Atlas
DataStore (prefs)           Product scraper
Google Maps SDK
```

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| UI | Kotlin + Jetpack Compose |
| Architecture | MVVM |
| Networking | Retrofit + OkHttp |
| Auth | JWT |
| Storage | DataStore |
| Maps | Google Maps SDK |
| Backend | Node.js + Express |
| Database | MongoDB Atlas |

## 🚀 Run Locally

```bash
# Backend
cd backend
npm install
npm start

# Android — open in Android Studio and run
```

## 👨‍💻 Author

**Anas Tahir** — [github.com/anas-tahi](https://github.com/anas-tahi)
