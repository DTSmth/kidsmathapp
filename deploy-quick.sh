#!/usr/bin/env bash
# =============================================================================
# KidsMathApp — Quick Deploy (code changes only)
# Builds and deploys without touching infrastructure.
# Use deploy.sh for first-time setup or infrastructure changes.
# =============================================================================
set -euo pipefail

PROJECT_ID="project-3fd8c56e-471a-47c7-b91"
REGION="us-east1"
AR_REPO="${REGION}-docker.pkg.dev/${PROJECT_ID}/kidsmathapp"

# What to deploy: backend, frontend, or both (default)
TARGET="${1:-both}"

deploy_backend() {
  echo "── Building backend ──"
  gcloud builds submit . \
    --tag="${AR_REPO}/backend:latest" \
    --project="$PROJECT_ID" \
    --quiet

  echo "── Deploying backend ──"
  gcloud run deploy kidsmathapp-backend \
    --image="${AR_REPO}/backend:latest" \
    --region="$REGION" \
    --project="$PROJECT_ID" \
    --quiet

  echo "✓ Backend deployed"
}

deploy_frontend() {
  echo "── Building frontend ──"
  gcloud builds submit ./frontend \
    --tag="${AR_REPO}/frontend:latest" \
    --project="$PROJECT_ID" \
    --quiet

  echo "── Deploying frontend ──"
  gcloud run deploy kidsmathapp-frontend \
    --image="${AR_REPO}/frontend:latest" \
    --region="$REGION" \
    --project="$PROJECT_ID" \
    --quiet

  echo "✓ Frontend deployed"
}

case "$TARGET" in
  backend)  deploy_backend ;;
  frontend) deploy_frontend ;;
  both)
    deploy_backend
    deploy_frontend
    ;;
  *)
    echo "Usage: ./deploy-quick.sh [backend|frontend|both]"
    exit 1
    ;;
esac

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║           Deploy Complete! 🚀                    ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║  Frontend: https://kidsmathapp-frontend-244230612831.us-east1.run.app"
echo "║  Backend:  https://kidsmathapp-backend-244230612831.us-east1.run.app"
echo "╚══════════════════════════════════════════════════╝"
