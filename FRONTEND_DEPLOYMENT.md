# Frontend Deployment Guide

## ✅ Production Build Successful!

Your frontend is now ready to deploy. Output is in `dist/certificate-frontend` (1.11 MB initial, 271 KB main chunk).

## Deploy to Netlify

### Option A: Drag & Drop (Fastest - 30 seconds)
1. Go to https://app.netlify.com/drop
2. Drag the entire `dist/certificate-frontend` folder from your file explorer
3. Your site goes live immediately with URL like `https://random-name-123456.netlify.app`

### Option B: Connect Git Repository (Best for updates)

#### Step 1: Push to Git
```bash
cd d:\certificate-service
git add .
git commit -m "Add frontend production build config"
git push origin main
```

#### Step 2: Deploy on Netlify
1. Go to https://app.netlify.com
2. Click "Add new site" → "Import an existing project"
3. Connect your Git provider (GitHub/GitLab/Bitbucket)
4. Select your repository
5. Configure build settings:
   - **Base directory:** `frontend`
   - **Build command:** `npm run build -- --configuration production`
   - **Publish directory:** `dist/certificate-frontend`
6. Click "Deploy site"

Build takes ~2-3 minutes. Your site will be live at `https://[your-site-name].netlify.app`

### Option C: Netlify CLI (For developers)
```bash
npm install -g netlify-cli
cd d:\certificate-service\frontend
netlify deploy --prod --dir=dist/certificate-frontend
```

## What Works in Frontend-Only Demo
✅ Full UI navigation  
✅ CKEditor template editor with all features  
✅ Template list/create/edit pages  
✅ Certificate generation UI  
✅ Customer management UI  
✅ Dashboard layout  

❌ API calls will fail (need backend)

## Next Steps

### After Frontend is Live
1. **Test the deployed site** - Verify UI, CKEditor, routing work
2. **Note the URL** - Share for demo purposes
3. **Deploy backend** (when ready):
   - Follow `BACKEND_DEPLOYMENT.md` for Render setup
   - Update `environment.prod.ts` with backend URL
   - Redeploy frontend

### Custom Domain (Optional)
- Netlify dashboard → Domain settings → Add custom domain
- Free SSL automatically provisioned

## Troubleshooting

**Build fails on Netlify:**
- Check Node version (should be 18+)
- Add in Netlify: Environment variable `NODE_VERSION=18`

**Routing issues (404 on refresh):**
- Already handled by `netlify.toml` redirects

**Large bundle warning:**
- CKEditor adds ~270KB (acceptable for rich editor)
- Can optimize later with lazy loading

