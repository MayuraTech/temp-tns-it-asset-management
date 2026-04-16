# Docker Build Fix - Frontend

## Problem

Docker build was failing with:
```
ERROR: "/app/dist/it-asset-management-frontend/browser": not found
```

## Root Cause

The Dockerfile was looking for the build output in:
```
/app/dist/it-asset-management-frontend/browser
```

But Angular 17's build output is actually at:
```
/app/dist/it-asset-management-frontend
```

Angular 17 changed the output structure - there's no `/browser` subdirectory by default.

## Solution

Updated `frontend/Dockerfile` line 22:

**Before**:
```dockerfile
COPY --from=build /app/dist/it-asset-management-frontend/browser ./
```

**After**:
```dockerfile
COPY --from=build /app/dist/it-asset-management-frontend ./
```

## Verification

The build should now complete successfully. The Docker image will contain the Angular application files at `/usr/share/nginx/html/`.

## Build Output Structure

Angular 17 outputs:
```
dist/
└── it-asset-management-frontend/
    ├── index.html
    ├── main-[hash].js
    ├── polyfills-[hash].js
    ├── styles-[hash].css
    └── assets/
```

NOT:
```
dist/
└── it-asset-management-frontend/
    └── browser/          ← This doesn't exist in Angular 17
        ├── index.html
        └── ...
```

## Testing

To test the Docker build locally:

```bash
cd frontend
docker build -t it-asset-frontend .
```

Should complete without errors.

## Related Files

- `frontend/Dockerfile` - Fixed COPY path
- `frontend/angular.json` - Defines `outputPath: "dist/it-asset-management-frontend"`

## Status

✅ Fixed - Docker build should now work correctly
