# 🚨 CRITICAL: Fix "Tenant or user not found" Error

## The Problem
Your backend is failing with:
```
FATAL: Tenant or user not found
org.postgresql.util.PSQLException: FATAL: Tenant or user not found
```

This means the **database password is not set correctly** in Render.

---

## ✅ Solution: Set Environment Variables in Render Dashboard

### Step 1: Get Your Supabase Credentials
1. Go to https://supabase.com/dashboard
2. **Select your certificate-service project**
3. Go to **Project Settings** → **Database**
4. Under **Connection strings**, find the **Session pooler** section
5. **Copy the password** - it's the part after `password=` in the connection string

Example connection string looks like:
```
postgresql://postgres:YOUR_PASSWORD_HERE@aws-0-us-west-1.pooler.supabase.com:6543/postgres
```

**Your actual password is: `YOUR_PASSWORD_HERE`**

---

### Step 2: Set Environment Variables on Render

1. **Open Render Dashboard**: https://dashboard.render.com/
2. **Click on your service**: `certificate-backend`
3. **Go to Settings tab** (top navigation)
4. **Scroll down to Environment** section
5. **Add these environment variables** (exactly as shown):

| Key | Value | Notes |
|-----|-------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://aws-0-us-west-1.pooler.supabase.com:6543/postgres?sslmode=require` | Connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Default Supabase user |
| `SPRING_DATASOURCE_PASSWORD` | **[PASTE YOUR SUPABASE PASSWORD]** | ⚠️ THIS IS CRITICAL |
| `SPRING_PROFILES_ACTIVE` | `production` | Spring profile |
| `CORS_ALLOWED_ORIGINS` | `https://spectacular-cucurucho-793843.netlify.app` | Frontend URL |

### Step 3: Redeploy

1. **Click "Redeploy latest commit"** button at top of page
2. **Wait 3-5 minutes** for deployment
3. **Check the Logs** tab to verify startup

Expected success log:
```
Certificate Service Application      : Starting CertificateServiceApplication
...
CertificateServiceApplication         : Started CertificateServiceApplication
```

---

## 🔍 Verify Your Supabase Password

**To find your password without the connection string:**

1. Go to https://supabase.com/dashboard
2. Click your project
3. Click **Project Settings** (gear icon, bottom left)
4. Click **Database**
5. Look for the default password that was created when you set up Supabase
6. If you forgot it, scroll down to **Reset database password** and create a new one

---

## 🆘 Troubleshooting

### Still Getting "Tenant or user not found"?

**Check 1: Is the password correct?**
```bash
# Test locally first (optional)
psql "postgresql://postgres:YOUR_PASSWORD@aws-0-us-west-1.pooler.supabase.com:6543/postgres?sslmode=require"
```

**Check 2: Are environment variables actually set?**
- Go to Render Settings → Environment
- Take a screenshot to verify all 5 variables are there
- Make sure no spaces at the beginning/end of values

**Check 3: Did you redeploy after adding env vars?**
- Just changing env vars doesn't restart the service
- Must click "Redeploy latest commit" at top of page

**Check 4: Is Supabase project paused?**
- Go to https://supabase.com/dashboard
- Check if your project status shows "PAUSED"
- Free tier projects pause after 1 week of inactivity
- Click resume if needed

**Check 4: Check Render logs for exact error**
1. Go to https://dashboard.render.com/
2. Click `certificate-backend`
3. Click **Logs** tab
4. Look for the exact error message
5. Search for "FATAL" or "password"

---

## 📋 Quick Checklist

Before clicking redeploy:
- [ ] I found my Supabase password from dashboard
- [ ] I copied it without extra spaces
- [ ] I pasted it into `SPRING_DATASOURCE_PASSWORD` env var
- [ ] All 5 environment variables are set in Render Settings
- [ ] I clicked "Redeploy latest commit"
- [ ] I waited 3-5 minutes for deployment
- [ ] I checked the Logs tab

---

## 💡 If Still Failing

1. **Check Supabase is running**:
   - Go to https://supabase.com/dashboard
   - Click your project
   - Status should show green checkmark
   - If paused, click Resume

2. **Check password is correct**:
   - Delete the env var
   - Go to Supabase settings
   - Copy password again (don't trust clipboard)
   - Paste fresh

3. **Check network rules**:
   - Supabase has IP allowlist by default
   - Go to Project Settings → Network → IP Whitelist
   - Add `*` or Render's IP range

4. **Still need help?**:
   - Take screenshot of Render environment variables
   - Take screenshot of Supabase connection string
   - Check browser console (F12) for actual API errors

---

## 📞 Support

If you're still stuck:
1. Share the exact error from Render Logs
2. Confirm you see all 5 env vars in Render Settings
3. Verify Supabase project is not paused

