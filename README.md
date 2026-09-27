# 🔗 LinkNest - URL Shortener & Dynamic QR Code Studio

> A clean, simple, and innovative Java application for **shortening URLs** and **generating customizable, downloadable QR codes** for any website.

---

## ✨ Core Features

1. **✂️ Fast URL Shortener**:
   - Paste any long URL to create a clean `/r/{code}` redirect link.
   - Choose a custom alias (e.g. `/r/portfolio`) or auto-generate a short code.
   - Live click counter for each shortened link.

2. **📱 Branded QR Code Generator**:
   - Generates high-resolution PNG QR codes directly in Java using Google ZXing.
   - Custom **QR Color** and **Background Color** pickers with instant live preview.
   - Adjustable resolution: Small (250px), Medium (400px), High-Res (700px).
   - **Download as PNG** button to save directly to your device.
   - **Copy QR Image** button to copy the image directly to your clipboard.

3. **📋 Easy Link Management**:
   - One-click copy for both short links and QR codes.
   - Table of generated links with total click counts and quick QR viewing.

4. **☁️ Render.com Ready**:
   - Dockerized with a multi-stage `Dockerfile` and `render.yaml`.
   - Lightweight file-based JSON persistence (`linknest-urls.json`).
   - Ready for 1-click free deployment on Render.

---

## 🚀 How to Run Locally

### 1. Run the Executable JAR
```powershell
d:
cd "d:\New folder"
java -jar target/linknest-1.0.0.jar
```

Or run with Maven:
```powershell
mvn spring-boot:run
```

### 2. Open in Your Browser
👉 **`http://localhost:8080`**

---

## ☁️ How to Deploy Live to Render.com

### Step 1: Push Code to Your GitHub Repository
In PowerShell:

```powershell
d:
cd "d:\New folder"
git add .
git commit -m "Update: Focused URL shortener and QR generator"
git push -u origin main
```

### Step 2: Deploy on Render
1. Go to [dashboard.render.com](https://dashboard.render.com) and click **New +** ➔ **Web Service**.
2. Select your `linknest` GitHub repository.
3. Render will auto-detect the `Dockerfile`:
   - **Runtime**: `Docker`
   - **Plan**: `Free`
   - **Health Check Path**: `/api/health`
4. Click **Deploy Web Service**!

---

## 📡 REST API Reference

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/urls` | `POST` | Create a short URL (`{ "targetUrl": "...", "code": "..." }`) |
| `/api/urls` | `GET` | Retrieve list of all shortened URLs |
| `/api/urls/{code}` | `DELETE` | Delete a shortened URL |
| `/api/qr` | `GET` | Generate dynamic QR PNG image (`?text=...&fg=...&bg=...&size=...`) |
| `/r/{code}` | `GET` | Redirect to destination URL and track clicks |
| `/api/health` | `GET` | Cloud health check |
