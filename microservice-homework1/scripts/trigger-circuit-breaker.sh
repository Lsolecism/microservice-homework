#!/usr/bin/env bash
set -euo pipefail

# Provider-side circuit breaker trigger script
# Make sure provider is running on localhost:9001

TARGET_URL="${1:-http://localhost:9001/provider/hello?name=error}"
COUNT="${2:-30}"

echo "Triggering provider circuit-breaker traffic..."
echo "URL: $TARGET_URL"
echo "Count: $COUNT"
echo

for ((i=1; i<=COUNT; i++)); do
  resp="$(curl -sS "$TARGET_URL" || true)"
  echo "[$i/$COUNT] $resp"
  sleep 0.01
done

echo
echo "Done. Open dashboard and monitor:"
echo "  http://localhost:9200/hystrix"
echo "Use stream:"
echo "  http://localhost:9001/actuator/hystrix.stream"
