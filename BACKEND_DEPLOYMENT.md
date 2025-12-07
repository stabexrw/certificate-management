# Backend Deployment Guide - Render

Your backend is ready to deploy to Render with PostgreSQL database.

## Prerequisites
- Backend code pushed to GitHub (already done ✓)
- Render account (free tier available)

## Step 1: Create PostgreSQL Database on Render

1. Go to https://dashboard.render.com
2. Click "New +" → "PostgreSQL"
3. Configure:
   - **Name:** `certificate-db`
   - **Database:** `certificate_db`
   - **User:** `postgres`
   - **Region:** Choose closest to you
   - **PostgreSQL Version:** 15 or higher
4. Click "Create Database"
5. **Save these values:**
   - Internal Database URL (copy full connection string)
   - External Database URL (if connecting from outside)

## Step 2: Create Web Service for Backend

1. In Render dashboard, click "New +" → "Web Service"
2. **Connect GitHub:**
   - Click "Connect account" and authorize GitHub
   - Select `certificate-service` repository
3. **Configure Build:**
   - **Name:** `certificate-backend`
   - **Environment:** `Java`
   - **Region:** Same as database
   - **Branch:** `main`
   - **Build Command:** `./mvnw clean package -DskipTests`
   - **Start Command:** `java -jar target/certificate-service-1.0.0.jar`
4. **Environment Variables:**
   Add these (get database URL from Step 1):
   ```
   SPRING_DATASOURCE_URL=postgresql://<user>:<password>@<host>:<port>/<database>
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=<your-db-password>
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   SERVER_PORT=8080
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
