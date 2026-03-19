#!/bin/sh
set -e

# Write runtime environment config for the SPA.
# Cloud Run injects API_URL as an env var (backend base URL); we append /api/v1.
cat > /usr/share/nginx/html/env-config.js <<EOF
window.__ENV__ = {
  API_URL: "${API_URL:-http://localhost:8080}/api/v1"
};
EOF

exec "$@"
