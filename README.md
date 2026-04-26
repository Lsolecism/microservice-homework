# Microservice Homework

Three microservice projects for 服务工程与方法论 course assignments.

## Homework 1 — Eureka Service Discovery

Spring Cloud microservices with Eureka server (3-node cluster), service provider/consumer, Hystrix dashboard, and Turbine aggregation.

- **eureka-server**: 3-node Eureka cluster for service registration
- **provider**: Service provider with multi-instance deployment
- **consumer**: Feign-based consumer with Hystrix circuit breaker
- **dashboard**: Hystrix dashboard for monitoring
- **turbine**: Aggregates Hystrix streams from multiple instances

## Homework 2 — Sentinel + Docker

Spring Cloud Alibaba microservices with Sentinel flow control and Docker Compose deployment.

- **order-service**: Order management service
- **payment-service**: Payment processing service
- **product-service**: Product catalog service
- **consumer-service**: Client consumer with Sentinel rules

## Homework 3 — Gateway + Seata

Spring Cloud microservices with API Gateway and distributed transactions via Seata.

- **gateway-service**: API gateway routing
- **order-service**: Order service with Seata distributed transactions
- **inventory-service**: Inventory management with stock deduction
