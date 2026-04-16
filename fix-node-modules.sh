#!/bin/bash

# Script to remove node_modules from git tracking
# Run this script to fix node_modules being committed

echo "Removing node_modules from git tracking..."

# Remove node_modules from git index (but keep the files locally)
git rm -r --cached frontend/node_modules 2>/dev/null || echo "frontend/node_modules not tracked"
git rm -r --cached backend/node_modules 2>/dev/null || echo "backend/node_modules not tracked"
git rm -r --cached node_modules 2>/dev/null || echo "root node_modules not tracked"

echo ""
echo "Done! Now commit the changes:"
echo "  git add .gitignore"
echo "  git commit -m 'Remove node_modules from git tracking'"
echo ""
echo "node_modules will no longer be committed to git."
