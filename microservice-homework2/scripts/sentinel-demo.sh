#!/usr/bin/env bash
# 压测 consumer-service 的 Sentinel 演示接口（需先启动 consumer，默认 http://127.0.0.1:8090）
# 用法: ./scripts/sentinel-demo.sh
#       BASE_URL=http://127.0.0.1:8090 ./scripts/sentinel-demo.sh

set -euo pipefail
BASE_URL="${BASE_URL:-http://127.0.0.1:8090}"

echo "=== Base: $BASE_URL ==="

#echo
#echo "--- QPS 限流 (约 1 QPS，连发应出现 blocked) ---"
#for i in $(seq 1 12); do
#  curl -sS "${BASE_URL}/sentinel/qps" || true
#  echo
#done

#echo
#echo "--- 热点参数限流 (同一 sku 高频) ---"
#for i in $(seq 1 20); do
#  curl -sS "${BASE_URL}/sentinel/hot?sku=demo-hot-key" || true
#  echo
#done
#
#echo
#echo "--- 热点参数 (换 sku，计数独立) ---"
#for i in $(seq 1 3); do
#  curl -sS "${BASE_URL}/sentinel/hot?sku=other-sku" || true
#  echo
#done

#echo
#echo "--- 慢调用熔断 (多请求积累统计) ---"
#for i in $(seq 1 90); do
#  curl -sS --max-time 30 "${BASE_URL}/sentinel/slow" || true
#  echo
#done

#echo
#echo "--- 异常比例熔断 ---"
#for i in $(seq 1 20); do
#  curl -sS "${BASE_URL}/sentinel/exception-ratio" || true
#  echo
#done

echo
echo "--- 系统规则入口 (CPU 等，需高负载才易触发) ---"
for i in $(seq 1 5); do
  curl -sS "${BASE_URL}/sentinel/system-cpu" || true
  echo
done

echo
echo "=== 完成 ==="
