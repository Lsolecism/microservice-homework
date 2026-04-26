#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/run"

mkdir -p "$LOG_DIR" "$PID_DIR"

SERVICES=(
  "eureka-peer1|eureka-server|8761|peer1"
  "eureka-peer2|eureka-server|8762|peer2"
  "eureka-peer3|eureka-server|8763|peer3"
  "provider-node1|provider|9001|node1"
  "provider-node2|provider|9002|node2"
  "provider-node3|provider|9003|node3"
  "consumer|consumer|9100|"
  "dashboard|dashboard|9200|"
  "turbine|turbine|9300|"
)

usage() {
  cat <<'EOF'
Usage:
  bash scripts/homework.sh build
  bash scripts/homework.sh start-all
  bash scripts/homework.sh stop-all
  bash scripts/homework.sh status
  bash scripts/homework.sh trigger
  bash scripts/homework.sh urls

Commands:
  build      Build all modules
  start-all  Start all services in background
  stop-all   Stop all started services
  status     Show process and port status
  trigger    Trigger normal/error/timeout requests for screenshots
  urls       Print URLs used in the homework report
EOF
}

wait_port() {
  local port="$1"
  local retries=60
  while (( retries > 0 )); do
    if ss -ltn 2>/dev/null | awk '{print $4}' | rg -q ":${port}$"; then
      return 0
    fi
    sleep 1
    retries=$((retries - 1))
  done
  return 1
}

is_pid_running() {
  local pid="$1"
  kill -0 "$pid" 2>/dev/null
}

start_one() {
  local name="$1" module="$2" port="$3" profile="$4"
  local pid_file="$PID_DIR/${name}.pid"
  local log_file="$LOG_DIR/${name}.log"

  if [[ -f "$pid_file" ]] && is_pid_running "$(cat "$pid_file")"; then
    echo "[SKIP] $name already running (pid $(cat "$pid_file"))."
    return 0
  fi

  rm -f "$pid_file"
  local cmd="mvn -pl ${module} spring-boot:run"
  if [[ -n "$profile" ]]; then
    cmd="${cmd} -Dspring-boot.run.profiles=${profile}"
  fi

  echo "[START] $name on port $port ..."
  nohup bash -lc "cd \"$ROOT_DIR\" && ${cmd}" >"$log_file" 2>&1 &
  local pid=$!
  echo "$pid" >"$pid_file"

  if wait_port "$port"; then
    echo "[OK] $name is up on $port (pid $pid)"
  else
    echo "[WARN] $name did not open port $port in time. Check: $log_file"
  fi
}

stop_one() {
  local name="$1"
  local pid_file="$PID_DIR/${name}.pid"
  if [[ ! -f "$pid_file" ]]; then
    echo "[SKIP] $name pid file not found."
    return 0
  fi
  local pid
  pid="$(cat "$pid_file")"
  if is_pid_running "$pid"; then
    echo "[STOP] $name (pid $pid)"
    kill "$pid" || true
    sleep 1
    if is_pid_running "$pid"; then
      kill -9 "$pid" || true
    fi
  else
    echo "[SKIP] $name process already stopped."
  fi
  rm -f "$pid_file"
}

cmd_build() {
  (cd "$ROOT_DIR" && mvn clean package -DskipTests)
}

cmd_start_all() {
  for item in "${SERVICES[@]}"; do
    IFS='|' read -r name module port profile <<<"$item"
    start_one "$name" "$module" "$port" "$profile"
  done
  echo
  cmd_urls
}

cmd_stop_all() {
  for (( idx=${#SERVICES[@]}-1 ; idx>=0 ; idx-- )); do
    IFS='|' read -r name _ _ _ <<<"${SERVICES[$idx]}"
    stop_one "$name"
  done
}

cmd_status() {
  echo "=== PID status ==="
  for item in "${SERVICES[@]}"; do
    IFS='|' read -r name _ port _ <<<"$item"
    pid_file="$PID_DIR/${name}.pid"
    if [[ -f "$pid_file" ]] && is_pid_running "$(cat "$pid_file")"; then
      echo "[UP]   $name pid=$(cat "$pid_file") port=$port"
    else
      echo "[DOWN] $name port=$port"
    fi
  done
  echo
  echo "=== Listening ports ==="
  ss -ltn | rg ":(8761|8762|8763|9001|9002|9003|9100|9200|9300)\b" || true
}

cmd_trigger() {
  echo "[1/4] Warmup traffic..."
  for _ in {1..8}; do
    curl -sS "http://localhost:9100/consumer/hello?name=Tom" >/dev/null || true
  done

  echo "[2/4] Trigger provider-side fallback (name=error)..."
  for _ in {1..10}; do
    curl -sS "http://localhost:9001/provider/hello?name=error" >/dev/null || true
  done

  echo "[3/4] Trigger consumer-side degradation (sleepMs=3000)..."
  for _ in {1..8}; do
    curl -sS "http://localhost:9100/consumer/hello?name=Tom&sleepMs=3000" >/dev/null || true
  done

  echo "[4/4] Keep normal traffic for dashboard charts..."
  for _ in {1..8}; do
    curl -sS "http://localhost:9100/consumer/hello?name=Tom" >/dev/null || true
  done

  echo "[DONE] Triggers executed. Now open dashboard pages and take screenshots."
}

cmd_urls() {
  cat <<'EOF'
=== Homework URLs ===
Eureka:
  http://localhost:8761/

Business APIs:
  Normal:   http://localhost:9100/consumer/hello?name=Tom
  Provider fallback: http://localhost:9001/provider/hello?name=error
  Consumer degrade:  http://localhost:9100/consumer/hello?name=Tom&sleepMs=3000

Hystrix Dashboard:
  http://localhost:9200/hystrix
  Single instance stream: http://localhost:9001/actuator/hystrix.stream
  Cluster stream:         http://localhost:9300/turbine.stream?cluster=default
EOF
}

main() {
  local action="${1:-}"
  case "$action" in
    build) cmd_build ;;
    start-all) cmd_start_all ;;
    stop-all) cmd_stop_all ;;
    status) cmd_status ;;
    trigger) cmd_trigger ;;
    urls) cmd_urls ;;
    *) usage ;;
  esac
}

main "${1:-}"
