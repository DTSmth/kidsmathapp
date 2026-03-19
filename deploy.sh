#!/usr/bin/env bash
# =============================================================================
# KidsMathApp — GCP Deployment Script
# Cloud Run (frontend + backend) + Cloud SQL (PostgreSQL)
#
# Prerequisites:
#   - gcloud CLI installed and authenticated: gcloud auth login
#   - Run once, then re-run to redeploy (all create commands are idempotent)
#
# Usage:
#   1. Edit the CONFIG section below
#   2. chmod +x deploy.sh && ./deploy.sh
# =============================================================================
set -euo pipefail

# =============================================================================
# CONFIG — edit before first run
# =============================================================================
PROJECT_ID="project-3fd8c56e-471a-47c7-b91"          # gcloud projects list
REGION="us-east1"
BACKEND_SERVICE="kidsmathapp-backend"
FRONTEND_SERVICE="kidsmathapp-frontend"
DB_INSTANCE="kidsmathapp-db"
DB_NAME="kidsmathapp"
DB_USER="kidsmathapp"
# DB_PASSWORD and JWT_SECRET are generated ONCE on first run and stored in
# Secret Manager. On re-runs the script reads the existing secret so it never
# regenerates them (which would break the DB connection / invalidate JWT tokens).
DB_PASSWORD=""   # populated in Step 4 below
JWT_SECRET=""    # populated in Step 4 below
# =============================================================================

AR_REPO="${REGION}-docker.pkg.dev/${PROJECT_ID}/kidsmathapp"
CLOUD_SQL_CONNECTION="${PROJECT_ID}:${REGION}:${DB_INSTANCE}"

echo "▶ Project:  $PROJECT_ID"
echo "▶ Region:   $REGION"
echo "▶ Cloud SQL: $CLOUD_SQL_CONNECTION"
echo ""

# -----------------------------------------------------------------------------
# 1. Enable required APIs
# -----------------------------------------------------------------------------
echo "── Step 1: Enabling GCP APIs ──"
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  sql-component.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  secretmanager.googleapis.com \
  --project="$PROJECT_ID"

# -----------------------------------------------------------------------------
# 2. Artifact Registry repo
# -----------------------------------------------------------------------------
echo "── Step 2: Artifact Registry ──"
gcloud artifacts repositories describe kidsmathapp \
  --location="$REGION" --project="$PROJECT_ID" &>/dev/null \
  || gcloud artifacts repositories create kidsmathapp \
       --repository-format=docker \
       --location="$REGION" \
       --project="$PROJECT_ID"

# -----------------------------------------------------------------------------
# 3. Secrets in Secret Manager
#    First run: generates random values and creates the secrets.
#    Subsequent runs: reads existing values — never regenerates them.
# -----------------------------------------------------------------------------
echo "── Step 3: Secrets ──"
ensure_secret() {
  local name="$1" generate_cmd="$2"
  if gcloud secrets describe "$name" --project="$PROJECT_ID" &>/dev/null; then
    echo "   Secret exists, reading: $name"
    gcloud secrets versions access latest --secret="$name" --project="$PROJECT_ID"
  else
    echo "   Creating secret: $name"
    local value
    value=$(eval "$generate_cmd")
    echo -n "$value" | gcloud secrets create "$name" --data-file=- --project="$PROJECT_ID"
    echo "$value"
  fi
}

DB_PASSWORD=$(ensure_secret "kidsmathapp-db-password" \
  "openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 32")
JWT_SECRET=$(ensure_secret "kidsmathapp-jwt-secret" \
  "openssl rand -base64 48")

# -----------------------------------------------------------------------------
# 4. Cloud SQL — PostgreSQL 15, cheapest config
#    db-f1-micro  ~$7/month (compute, runs 24/7)
#    10 GB SSD    ~$1.70/month (minimum storage)
#    No backups   saves ~$0.08/GB/month
#
#    💡 To pause the instance when not developing (charges storage only, ~$0.17/mo):
#       gcloud sql instances patch $DB_INSTANCE --activation-policy=NEVER --project=$PROJECT_ID
#    To resume:
#       gcloud sql instances patch $DB_INSTANCE --activation-policy=ALWAYS --project=$PROJECT_ID
# -----------------------------------------------------------------------------
echo "── Step 4: Cloud SQL ──"
if ! gcloud sql instances describe "$DB_INSTANCE" --project="$PROJECT_ID" &>/dev/null; then
  echo "   Creating Cloud SQL instance (this takes ~5 minutes)…"
  gcloud sql instances create "$DB_INSTANCE" \
    --database-version=POSTGRES_15 \
    --tier=db-f1-micro \
    --region="$REGION" \
    --no-backup \
    --storage-size=10GB \
    --storage-type=SSD \
    --project="$PROJECT_ID"
else
  echo "   Cloud SQL instance exists — applying cost settings…"
  gcloud sql instances patch "$DB_INSTANCE" \
    --no-backup \
    --project="$PROJECT_ID" --quiet
fi

# Create database (ignore "already exists" error)
if ! gcloud sql databases describe "$DB_NAME" --instance="$DB_INSTANCE" --project="$PROJECT_ID" &>/dev/null; then
  echo "   Creating database: $DB_NAME"
  gcloud sql databases create "$DB_NAME" --instance="$DB_INSTANCE" --project="$PROJECT_ID"
else
  echo "   Database exists: $DB_NAME"
fi

# Create or update user
if ! gcloud sql users describe "$DB_USER" --instance="$DB_INSTANCE" --project="$PROJECT_ID" &>/dev/null; then
  echo "   Creating user: $DB_USER"
  gcloud sql users create "$DB_USER" \
    --instance="$DB_INSTANCE" \
    --password="$DB_PASSWORD" \
    --project="$PROJECT_ID"
else
  echo "   User exists, updating password: $DB_USER"
  gcloud sql users set-password "$DB_USER" \
    --instance="$DB_INSTANCE" \
    --password="$DB_PASSWORD" \
    --project="$PROJECT_ID"
fi

# Grant the default Cloud Run service account access to both secrets
PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')
CR_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

for secret in kidsmathapp-db-password kidsmathapp-jwt-secret; do
  gcloud secrets add-iam-policy-binding "$secret" \
    --member="serviceAccount:${CR_SA}" \
    --role="roles/secretmanager.secretAccessor" \
    --project="$PROJECT_ID" --quiet
done

# Grant Cloud Run SA the Cloud SQL Client role
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:${CR_SA}" \
  --role="roles/cloudsql.client" --quiet

# -----------------------------------------------------------------------------
# 5. Build & push backend image using Cloud Build (no local Docker needed)
# -----------------------------------------------------------------------------
echo "── Step 5: Backend image (Cloud Build) ──"
BACKEND_IMAGE="${AR_REPO}/backend:latest"
gcloud builds submit . \
  --tag="$BACKEND_IMAGE" \
  --project="$PROJECT_ID" \
  --quiet

# -----------------------------------------------------------------------------
# 6. Deploy backend to Cloud Run
# -----------------------------------------------------------------------------
echo "── Step 6: Deploy backend ──"
gcloud run deploy "$BACKEND_SERVICE" \
  --image="$BACKEND_IMAGE" \
  --region="$REGION" \
  --platform=managed \
  --allow-unauthenticated \
  --port=8080 \
  --memory=512Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=2 \
  --add-cloudsql-instances="$CLOUD_SQL_CONNECTION" \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,DB_NAME=${DB_NAME},DB_USER=${DB_USER},CLOUD_SQL_CONNECTION_NAME=${CLOUD_SQL_CONNECTION}" \
  --set-secrets="DB_PASSWORD=kidsmathapp-db-password:latest,JWT_SECRET=kidsmathapp-jwt-secret:latest" \
  --project="$PROJECT_ID"

BACKEND_URL=$(gcloud run services describe "$BACKEND_SERVICE" \
  --region="$REGION" --project="$PROJECT_ID" \
  --format='value(status.url)')
echo "   Backend URL: $BACKEND_URL"

# -----------------------------------------------------------------------------
# 7. Build & push frontend image using Cloud Build
# -----------------------------------------------------------------------------
echo "── Step 7: Frontend image (Cloud Build) ──"
FRONTEND_IMAGE="${AR_REPO}/frontend:latest"
gcloud builds submit ./frontend \
  --tag="$FRONTEND_IMAGE" \
  --project="$PROJECT_ID" \
  --quiet

# -----------------------------------------------------------------------------
# 8. Deploy frontend to Cloud Run
# -----------------------------------------------------------------------------
echo "── Step 8: Deploy frontend ──"
gcloud run deploy "$FRONTEND_SERVICE" \
  --image="$FRONTEND_IMAGE" \
  --region="$REGION" \
  --platform=managed \
  --allow-unauthenticated \
  --port=8080 \
  --memory=256Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=2 \
  --set-env-vars="API_URL=${BACKEND_URL}" \
  --project="$PROJECT_ID"

FRONTEND_URL=$(gcloud run services describe "$FRONTEND_SERVICE" \
  --region="$REGION" --project="$PROJECT_ID" \
  --format='value(status.url)')
echo "   Frontend URL: $FRONTEND_URL"

# -----------------------------------------------------------------------------
# 9. Update backend CORS with the live frontend URL
# -----------------------------------------------------------------------------
echo "── Step 9: Update CORS ──"
gcloud run services update "$BACKEND_SERVICE" \
  --region="$REGION" \
  --project="$PROJECT_ID" \
  --update-env-vars="CORS_ALLOWED_ORIGINS=${FRONTEND_URL}"

# -----------------------------------------------------------------------------
# Done
# -----------------------------------------------------------------------------
echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║           Deployment Complete! 🎉                ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║  Frontend : $FRONTEND_URL"
echo "║  Backend  : $BACKEND_URL"
echo "╚══════════════════════════════════════════════════╝"
echo ""
echo "⚠  Save your DB password and JWT secret — they are stored in"
echo "   Secret Manager but NOT printed here. To view:"
echo "   gcloud secrets versions access latest --secret=kidsmathapp-db-password --project=$PROJECT_ID"
