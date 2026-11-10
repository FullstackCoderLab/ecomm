# eCommWeb - React + Vite + Tailwind CSS Frontend

A modern, responsive e-commerce web application built with **React**, **Vite**, and **Tailwind CSS**, fully integrated with the **`eCommAPIGateway`** (running on `http://localhost:8761`).

---

## 🌟 Key Features

1. **API Gateway & Eureka Integration**:
   - All authentication and microservice requests are routed via `http://localhost:8761`.
   - Real-time Gateway status badge displaying health (`online` / `offline`) and the load-balanced target instance (e.g. `http://localhost:8081`).
   - Captures telemetry headers: `X-LoadBalanced-Instance`, `X-Correlation-Id`, and `X-Gateway-Duration-Ms`.

2. **User Authentication & Management**:
   - **User Registration**: `POST /api/v1/auth/register` (Name, Email, Password, Role selection).
   - **User Login**: `POST /api/v1/auth/login` (Email, Password).
   - Form input validation, password show/hide toggle, loading spinners, and error alerts.
   - Session persistence with `localStorage`.

3. **Interactive Dashboard**:
   - **User Profile Display**: Full name, email, granted roles (`ROLE_USER`, `ROLE_ADMIN`).
   - **JWT Inspector**: 1-click token copy button for testing in Bruno, Postman, or cURL.
   - **API Test Playground**: Calls `GET /api/v1/users/me` with Bearer JWT token to test authorization live!
   - **Featured Products Showcase**: E-commerce catalog with simulated cart actions.

---

## 📁 Project Structure

```
eCommWeb/
├── package.json                   # Dependencies (React 18, Vite 5, Tailwind 3.4, Axios, Lucide)
├── vite.config.js                 # Vite configuration (port 3000)
├── tailwind.config.js             # Tailwind CSS theme & content paths
├── postcss.config.js              # PostCSS plugins
├── index.html                     # HTML5 entry with Inter font
├── .env                           # VITE_API_GATEWAY_URL=http://localhost:8761
└── src/
    ├── main.jsx                   # React root mount
    ├── App.jsx                    # Main application controller
    ├── index.css                  # Tailwind CSS directives & custom styles
    ├── context/
    │   └── AuthContext.jsx        # Authentication state (user, token, login, register, logout)
    ├── services/
    │   └── api.js                 # Axios client with interceptors for Gateway & JWT Bearer token
    └── components/
        ├── Navbar.jsx             # Top bar with logo, gateway status badge & user actions
        ├── GatewayStatus.jsx      # Live health checker for eCommAPIGateway
        ├── Login.jsx              # Sign-in form with demo helper button
        ├── Register.jsx           # Sign-up form with role selection
        └── Dashboard.jsx          # Protected user view, JWT viewer, API test & catalog
```

---

## 🚀 Getting Started

### 1. Ensure Backend Services are Running
Before starting the web app, make sure:
1. **`eCommAPIGateway`** is running on `http://localhost:8761`.
2. **`eCommUserAuth`** is running on `http://localhost:8081` (registered with Eureka).

### 2. Start the Development Server
In your terminal, navigate to the `eCommWeb` directory and run:

```bash
cd C:\Users\NADEEM\STS_NoorAI\eCommWeb
npm run dev
```

The application will start at:
👉 **`http://localhost:3000`**

---

## 🧪 Testing the Authentication Flow

1. Open **`http://localhost:3000`** in your browser.
2. Check the top bar: the Gateway indicator will show **`Gateway: http://localhost:8761 Online`** (green dot).
3. Click the **Register** tab:
   - Name: `Nadeem`
   - Email: `nadeem@example.com`
   - Password: `Password@123`
   - Role: `Customer` or `Administrator`
   - Click **Create Account**.
4. You will immediately be logged in and taken to the **Dashboard**!
5. In the Dashboard:
   - Check the **Target Instance** box (e.g. `http://localhost:8081`).
   - Click **Copy** to copy your JWT token.
   - Click **"Call GET /api/v1/users/me"** to test protected endpoint authorization in real time!
6. Click the logout icon to return to the sign-in screen.
