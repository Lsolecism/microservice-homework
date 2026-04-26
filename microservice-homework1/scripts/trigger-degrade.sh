#!/usr/bin/env bash
set -euo pipefail

# Consumer-side degradation trigger script
# Make sure consumer is running on localhost:9100

TARGET_URL="${1:-http://localhost:9100/consumer/hello?name=Tom&sleepMs=3000}"
COUNT="${2:-20}"

echo "Triggering consumer degradation traffic..."
echo "URL: $TARGET_URL"
echo "Count: $COUNT"
echo

for ((i=1; i<=COUNT; i++)); do
  resp="$(curl -sS "$TARGET_URL" || true)"
  echo "[$i/$COUNT] $resp"
  sleep 0.1
done

echo
echo "Done. Expected response contains consumer fallback text."
echo "You can also keep dashboard open at:"
echo "  http://localhost:9200/hystrix"
