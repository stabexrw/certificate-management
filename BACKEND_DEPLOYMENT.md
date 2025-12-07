# Backend Deployment Guide - Render

Your backend is ready to deploy to Render using an external PostgreSQL (no billing required on Render).

## Prerequisites
- Backend code pushed to GitHub (already done ✓)
- Render account (free tier available)

## Step 1: Create External PostgreSQL (No card required)

Use a free Postgres provider (pick one):
- **Supabase (free tier):** https://supabase.com → New project → Database password (save it) → Get `Connection string → URI` (use `postgres://...`).
- **Railway (free tier):** https://railway.app → New Project → Provision Postgres → Get the `Postgres Connection URL`.

Record these values from your provider:
- Host, Port
- Database name
- Username
- Password
- Full URL `postgresql://<user>:<password>@<host>:<port>/<database>`

## Step 2: Create Web Service for Backend

### Option A: Using render.yaml (Recommended - Easiest)

1. In Render dashboard, click "New +" → "Blueprint"
2. Connect your GitHub repository
3. Render detects `render.yaml` and creates a Docker-based web service (`certificate-backend`).
4. Add environment variables (Step 4) using your external Postgres credentials.
5. Deploy will start automatically

### Option B: Manual Setup

1. In Render dashboard, click "New +" → "Web Service"
2. **Connect GitHub:**
   - Click "Connect account" and authorize GitHub
   - Select `certificate-management` repository
3. **Configure Build:**
   - **Name:** `certificate-backend`
   - **Environment:** `Docker`
   - **Region:** Same as database
   - **Branch:** `main`
   - **Root Directory:** `backend`
   - **Dockerfile Path:** `./Dockerfile`
   - No build/start commands needed (Docker handles this)
4. **Environment Variables:**
   Add these using your external Postgres values (from Supabase/Railway). Use the **jdbc:** prefix and SSL:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>?sslmode=require
   SPRING_DATASOURCE_USERNAME=<user>
   SPRING_DATASOURCE_PASSWORD=<password>
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   SERVER_PORT=8080
   CORS_ALLOWED_ORIGINS=https://spectacular-cucurucho-793843.netlify.app
   ```

5. **Plan:** Select "Free" tier (if available) or "Standard" ($7/month)
6. Click "Create Web Service"

The backend will start building. This takes 3-5 minutes.

## Step 3: Get Backend URL

Once deployed successfully:
1. Go to your service dashboard
2. Copy the service URL (e.g., `https://certificate-backend.onrender.com`)
3. **Important:** Render free tier services spin down after 15 minutes of inactivity. They restart on next request but with ~30 second delay.

## Step 4: Update Frontend with Backend URL

1. Update `frontend/src/environments/environment.prod.ts`:
   ```typescript
   export const environment = {
     apiUrl: 'https://certificate-backend.onrender.com/api'
   };
   ```

2. Commit and push:
   ```bash
   git add frontend/src/environments/environment.prod.ts
   git commit -m "Update backend URL for production"
   git push
   ```

3. Netlify will automatically redeploy the frontend with the new backend URL

## Step 5: Test the Deployment

1. Visit your frontend URL: https://spectacular-cucurucho-793843.netlify.app/
2. Try registering a new account
3. Create a template with the CKEditor
4. Generate a certificate

## Troubleshooting

**Backend not responding:**
- Check Render dashboard for build/deploy errors
- Free tier services may take 30+ seconds to wake up first request
- Check environment variables are set correctly

**Database connection errors:**
- Verify database URL format in Render environment variables
- Ensure PostgreSQL database is running (check Render dashboard)

**CORS errors:**
- Backend CORS config should allow Netlify domain
- Check `backend/src/main/java/.../config/CorsConfig.java`

**Deploy with Docker (Alternative):**
If Render Maven build fails, use Docker:
1. Add `backend/Dockerfile`:
   ```dockerfile
   FROM maven:3.9-eclipse-temurin-17 AS builder
   WORKDIR /build
   COPY . .
   RUN mvn clean package -DskipTests

   FROM eclipse-temurin:17-jre
   COPY --from=builder /build/target/certificate-service-1.0.0.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```
2. In Render, select "Docker" environment instead of Java
3. No build/start commands needed - Render auto-detects Dockerfile

## Backend URLs

- **Internal API:** `https://certificate-backend.onrender.com/api`
- **Auth:** `POST /api/auth/register` or `/api/auth/login`
- **Swagger Docs:** `https://certificate-backend.onrender.com/swagger-ui.html` (if enabled)

## Next Steps

- Monitor both Render and Netlify dashboards for errors
- Set up custom domain (optional)
- Consider upgrading from free tier for better uptime
