# Spring Cloud 作业实现

本项目包含以下模块：

- `eureka-server`：注册中心（3 节点）
- `provider`：生产者（3 实例）
- `consumer`：消费者（Feign + Hystrix 降级）
- `dashboard`：Hystrix Dashboard
- `turbine`：Hystrix 集群流聚合

## 1. 环境要求

- JDK 8
- Maven 3.6+

## 2. 编译

```bash
mvn clean package -DskipTests
```

## 3. 启动顺序（必须按顺序）

建议每条命令单独开一个终端执行。

### 3.1 启动 Eureka 三节点

```bash
mvn -pl eureka-server spring-boot:run -Dspring-boot.run.profiles=peer1
mvn -pl eureka-server spring-boot:run -Dspring-boot.run.profiles=peer2
mvn -pl eureka-server spring-boot:run -Dspring-boot.run.profiles=peer3
```

访问：`http://localhost:8761/`

要求在 Eureka 页面能看到 3 个注册中心节点。

### 3.2 启动生产者三实例

```bash
mvn -pl provider spring-boot:run -Dspring-boot.run.profiles=node1
mvn -pl provider spring-boot:run -Dspring-boot.run.profiles=node2
mvn -pl provider spring-boot:run -Dspring-boot.run.profiles=node3
```

要求在 Eureka 页面看到 3 个 `PROVIDER-SERVICE` 实例（端口 `9001/9002/9003`）。

### 3.3 启动消费者

```bash
mvn -pl consumer spring-boot:run
```

### 3.4 启动 Dashboard

```bash
mvn -pl dashboard spring-boot:run
```

Dashboard 页面：`http://localhost:9200/hystrix`

### 3.5 启动 Turbine

```bash
mvn -pl turbine spring-boot:run
```

## 4. 功能验证

### 4.1 消费者调用生产者

- 正常调用：`http://localhost:9100/consumer/hello?name=Tom`
- 触发生产者异常：`http://localhost:9100/consumer/hello?name=error`
- 触发超时（消费者降级）：`http://localhost:9100/consumer/hello?name=Tom&sleepMs=3000`

### 4.2 Dashboard 单实例监控

在 Dashboard 输入：

- `http://localhost:9001/actuator/hystrix.stream`

注意：先多次访问消费者接口制造请求流量，否则页面可能长时间空白。

### 4.3 Dashboard + Turbine 集群监控

在 Dashboard 输入：

- `http://localhost:9300/turbine.stream?cluster=default`

## 5. 常见问题排查

### 问题 1：Eureka 里只有 1 个生产者

原因：只启动了一个 `provider` 实例。  
检查：本机应监听 `9001/9002/9003` 三个端口。

### 问题 2：Hystrix Dashboard 无法连接流

按下面顺序检查：

1. `provider` 是否已启动，且 `http://localhost:9001/actuator` 中能看到 `hystrix.stream`
2. 监控地址是否写成 `http://localhost:9001/actuator/hystrix.stream`
3. 是否已先调用业务接口产生监控数据
4. 使用 Turbine 时是否写了 `?cluster=default`

## 6. Word 报告截图建议

1. Eureka 页面显示 3 个注册中心节点
2. Eureka 页面显示 3 个 `PROVIDER-SERVICE` + 1 个 `CONSUMER-SERVICE`
3. 消费者正常调用结果
4. 生产者熔断/降级结果
5. 消费者降级结果
6. Dashboard 单实例监控页面
7. Dashboard + Turbine 集群监控页面
