# 🔧 Fix for Backend 503 Error - Setup Guide

## Problem
The backend on Render is returning **503 Service Unavailable** because the database credentials are not configured in the Render environment variables.

## Solution

### Option 1: Render Dashboard Configuration (Recommended)

1. **Open Render Dashboard**
   - Go to https://dashboard.render.com/
   - Log in with your account

2. **Select your service**
   - Click on **certificate-backend** service
   - Go to the **Settings** tab

3. **Add Environment Variables**
   - Scroll down to **Environment** section
   - Click **Add Environment Variable**
   - Add these variables:

   | Key | Value |
   |-----|-------|
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://aws-0-us-west-1.pooler.supabase.com:6543/postgres?sslmode=require` |
   | `SPRING_DATASOURCE_USERNAME` | `postgres` |
   | `SPRING_DATASOURCE_PASSWORD` | `[YOUR_SUPABASE_PASSWORD]` |
   | `CORS_ALLOWED_ORIGINS` | `https://spectacular-cucurucho-793843.netlify.app` |
   | `SPRING_PROFILES_ACTIVE` | `production` |

   **⚠️ Replace `[YOUR_SUPABASE_PASSWORD]` with your actual Supabase password**

4. **Redeploy**
   - Click **Redeploy latest commit** at the top of the page
   - Wait 2-3 minutes for the deployment to complete

---

### Option 2: Update render.yaml with Hard-coded Credentials

If you want to store credentials in Git (less secure):

```yaml
services:
  - type: web
    name: certificate-backend
    plan: free
    runtime: docker
    region: oregon
    dockerfilePath: ./backend/Dockerfile
    dockerContext: ./backend
    envVars:
      - key: SPRING_DATASOURCE_URL
        value: jdbc:postgresql://aws-0-us-west-1.pooler.supabase.com:6543/postgres?sslmode=require
      - key: SPRING_DATASOURCE_USERNAME
        value: postgres
      - key: SPRING_DATASOURCE_PASSWORD
        value: YOUR_SUPABASE_PASSWORD_HERE
      - key: CORS_ALLOWED_ORIGINS
        value: https://spectacular-cucurucho-793843.netlify.app
      - key: SPRING_PROFILES_ACTIVE
        value: production
```

Then commit and push:
```bash
git add render.yaml
git commit -m "Configure Render environment variables with Supabase credentials"
git push origin main
```

---

### Option 3: Use Render Secrets (Most Secure)

1. Go to your Render dashboard
2. Click on your service → **Settings**
3. Scroll to **Secret Files** section
4. Create a file named `.env.production`:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-us-west-1.pooler.supabase.com:6543/postgres?sslmode=require
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=[YOUR_PASSWORD]
   ```
5. Update render.yaml to reference it:
   ```yaml
   envVars:
     - key: SPRING_DATASOURCE_PASSWORD
       fromFile: /etc/secrets/.env.production
   ```

---

## How to Get Supabase Credentials

If you don't have them:

1. Go to https://supabase.com/dashboard
2. Select your **certificate-service** project
3. Go to **Project Settings** → **Database**
4. Under **Connection Info**, copy the password for user `postgres`
5. The connection details are:
   - **Host**: `aws-0-us-west-1.pooler.supabase.com` (pooler host)
   - **Port**: `6543` (for connection pooler)
   - **Database**: `postgres`
   - **User**: `postgres`
   - **Password**: [Your password]

---

## Verification Steps

After configuring the environment variables:

### 1. Check Backend is Running
```bash
curl -X GET https://certificate-backend-orsd.onrender.com/api/health
# Should return 200 OK
```

### 2. Test CORS Preflight
```bash
curl -X OPTIONS https://certificate-backend-orsd.onrender.com/api/auth/login \
  -H "Origin: https://spectacular-cucurucho-793843.netlify.app" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type"
# Should return 200 OK with CORS headers
```

### 3. Test Registration
```bash
curl -X POST https://certificate-backend-orsd.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Origin: https://spectacular-cucurucho-793843.netlify.app" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!",
    "fullName": "Test User"
  }'
# Should return 201 Created with JWT token
```

### 4. Check Render Logs
1. Go to https://dashboard.render.com/
2. Click on **certificate-backend**
3. Go to **Logs** tab
4. Look for connection errors or startup issues
5. Common errors:
   - `Unable to acquire JDBC Connection` = Database not reachable
   - `Connection refused` = Wrong host/port
   - `FATAL: password authentication failed` = Wrong password

---

## Troubleshooting

### Error: "Unable to acquire JDBC Connection"
- ✅ Check SPRING_DATASOURCE_URL has correct host and port
- ✅ Check SPRING_DATASOURCE_PASSWORD is correct
- ✅ Check Supabase database is running (not paused)
- ✅ Check firewall rules allow connection from Render

### Error: "CORS policy violation"
- ✅ Check CORS_ALLOWED_ORIGINS matches frontend URL exactly
- ✅ Make sure CORS is enabled in SecurityConfig
- ✅ Clear browser cache and retry

### Error: "503 Service Unavailable"
- ✅ Check backend service is running (Render Logs)
- ✅ Wait 5 minutes after deployment
- ✅ Try accessing backend health endpoint

### Live Demo Not Working
1. Open browser **Developer Tools** (F12)
2. Go to **Network** tab
3. Try to register or login
4. Look for failed requests
5. Click on failed request to see error details
6. Share the error response in browser console

---

## Quick Links

- **Render Dashboard**: https://dashboard.render.com/
- **Supabase Dashboard**: https://supabase.com/dashboard
- **Live Frontend**: https://spectacular-cucurucho-793843.netlify.app/
- **Backend URL**: https://certificate-backend-orsd.onrender.com/api
- **Repository**: https://github.com/stabexrw/certificate-management

---

## Next Steps

1. **Get Supabase Password** from https://supabase.com/dashboard
2. **Add Environment Variables** to Render dashboard (or commit to render.yaml)
3. **Redeploy** the backend service
4. **Test** using the curl commands above
5. **Refresh** the live demo and try again

