# 🔗 LinkNest - Custom Bio-Page & Dynamic QR Code Studio

> A simple, innovative, and cloud-ready Java application that functions as your own self-hosted **Linktree + Bitly + Branded QR Code Generator** with real-time click and device analytics.

---

## ✨ Features & Innovation

1. **🎨 Interactive Bio-Page Builder with Live Phone Mockup**:
   - Create your personal bio-link page (e.g. `http://localhost:8080/p/alex`).
   - Live interactive **Phone Mockup** updates in real-time as you type your name, bio, or add links.
   - Choose between **5 beautiful themes**: *Midnight Indigo, Cyberpunk Neon, Sunset Gradient, Emerald Dark, and Minimal Monochrome*.
   - Add social icons (GitHub, LinkedIn, Twitter/X, YouTube, Email).

2. **✂️ URL Shortener & Click Tracking**:
   - Shorten any long URL with customizable slugs (e.g., `/r/portfolio` or `/r/resume`).
   - Automatically tracks click counts, referral sources, and timestamps.

3. **📱 Dynamic Branded QR Code Studio (Pure Java ZXing)**:
   - Generates high-resolution PNG QR codes directly in Java.
   - Fully customizable **Foreground** and **Background** colors (matching your personal brand).
   - One-click **Download as PNG** or copy short link.

4. **📊 Real-Time Analytics Dashboard**:
   - **KPI Cards**: Total Profile Views, Bio Link Clicks, Short URL Clicks, Grand Total Engagements.
   - **Device Breakdown**: Detects device types (*Mobile vs. Desktop vs. Tablet*) from User-Agent headers with visual progress bars.
   - **Top Links Leaderboard**: Shows which links are getting the most traffic.
   - **Live Interaction Feed**: Real-time stream of incoming clicks.

5. **☁️ Render.com Deployment Ready**:
   - Comes with multi-stage `Dockerfile` and `render.yaml`.
   - Zero-dependency file persistence (`linknest-data.json`).
   - Health check endpoint `/api/health` for cloud monitoring.

---

## 🏗️ Architecture

```
d:/New folder/
├── Dockerfile                  # Multi-stage Docker build for Render
├── render.yaml                 # Render Blueprint configuration
├── pom.xml                     # Maven build (Java 21/24, Spring Boot 3.4, Google ZXing)
├── src/
│   ├── main/
│   │   ├── java/com/linknest/
│   │   │   ├── LinkNestApplication.java       # Spring Boot main class
│   │   │   ├── controller/
│   │   │   │   ├── ApiController.java         # REST APIs (Profile, Links, URLs, Analytics, Health)
│   │   │   │   └── PublicPageController.java  # Public bio page /p/{user}, redirects /r/{code}, QR API
│   │   │   ├── model/
│   │   │   │   ├── Profile.java               # Bio-page model (theme, links, socials)
│   │   │   │   ├── LinkItem.java              # Individual link card
│   │   │   │   ├── ShortUrl.java              # Shortened URL entity with device counters
│   │   │   │   ├── ClickEvent.java            # Event model for click streams
│   │   │   │   └── AnalyticsSummary.java      # Aggregated dashboard metrics
│   │   │   └── service/
│   │   │       ├── QrCodeService.java         # Pure Java ZXing dynamic QR generator
│   │   │       ├── AnalyticsService.java      # Device detection & engagement metrics
│   │   │       └── DataStorageService.java    # JSON file persistence with demo seed
│   │   └── resources/
│   │       ├── application.properties         # Server port & storage path
│   │       └── static/
│   │           ├── index.html                 # Admin Studio (Phone mockup, QR studio, Analytics)
│   │           ├── bio.html                   # Public Bio-Page template
│   │           ├── css/style.css              # Studio styles & dark theme
│   │           ├── css/bio.css                # Public Bio-Page styling & 5 theme presets
│   │           ├── js/app.js                  # Studio reactive logic & live phone preview
│   │           └── js/bio.js                  # Public bio-page client script
│   └── test/
│       └── java/com/linknest/
│           └── LinkNestServicesTest.java      # Automated unit tests for QR, Analytics, Storage
```

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
- **Studio Dashboard (Builder & Analytics)**:
  👉 **`http://localhost:8080`**
- **Public Bio Page**:
  👉 **`http://localhost:8080/p/alex`**

---

## ☁️ How to Deploy Live to Render.com

### Step 1: Push Code to GitHub
Open PowerShell and run:

```powershell
d:
cd "d:\New folder"
git init
git add .
git commit -m "Initial commit of LinkNest"
git branch -M main
git remote add origin https://github.com/<YOUR-USERNAME>/<YOUR-REPO-NAME>.git
git push -u origin main
```

*(Note: If you don't have Git installed, install it in PowerShell with `winget install Git.Git` or upload the folder to GitHub via browser).*

### Step 2: Deploy on Render
1. Log in to [dashboard.render.com](https://dashboard.render.com).
2. Click **New +** ➔ **Web Service**.
3. Connect your GitHub repository.
4. Render will auto-detect the `Dockerfile`:
   - **Runtime**: `Docker`
   - **Plan**: `Free`
   - **Health Check Path**: `/api/health`
5. Click **Deploy Web Service**!
6. Once built, you will get your live public URL:
   ```
   https://linknest-xxxx.onrender.com
   ```
   Your public bio will be live at:
   ```
   https://linknest-xxxx.onrender.com/p/alex
   ```

---

## 📡 REST API Reference

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/p/{username}` | `GET` | Public bio-page |
| `/r/{code}` | `GET` | Short URL redirect with click logging |
| `/click/{linkId}` | `GET` | Bio link redirect with click logging |
| `/api/qr` | `GET` | Dynamic QR code PNG (`?text=...&fg=...&bg=...`) |
| `/api/profile` | `GET` / `PUT` | Read or update bio-page details |
| `/api/links` | `POST` | Add a new bio link |
| `/api/links/{id}` | `PUT` / `DELETE` | Edit or remove a link |
| `/api/urls` | `GET` / `POST` | List or create short URLs |
| `/api/analytics` | `GET` | Device metrics, click counts, live feed |
| `/api/health` | `GET` | Cloud health check |
