# 微服务作业：Nacos 注册发现 + Sentinel 流控演示说明

本仓库对应作业要求中的两项内容：

1. **Nacos**：注册发现中心 + 3 个 Provider（订单 / 商品 / 支付）+ 1 个 Consumer；Consumer 通过**服务名**调用 Provider，并支持**负载均衡**（多实例场景）。
2. **Sentinel**：QPS 限流、热点参数限流、慢调用比例熔断、异常比例熔断、CPU 系统规则（系统保护）。

作业文档（Word）需自行截图与撰写说明；下文按「如何操作演示」组织，便于你对照每一项截屏。

---

## 一、环境准备

| 依赖 | 说明 |
|------|------|
| JDK | **17**（与父 `pom.xml` 中 `java.version` 一致） |
| Maven | 3.8+ 建议 |
| Docker | 用于启动 Nacos 与 Sentinel 控制台 |

---

## 二、启动基础设施（Nacos + Sentinel）

在项目根目录（存在 `docker-compose.yml` 的目录）执行：

```bash
docker compose up -d
```

### 2.1 访问地址与账号

| 组件 | 地址 | 说明 |
|------|------|------|
| Nacos 控制台 | http://127.0.0.1:8848/nacos | 登录见当前 `docker-compose.yml` 中 `NACOS_ADMIN_USERNAME` / `NACOS_ADMIN_PASSWORD`（示例为 `nacos` / `nacos`） |
| Sentinel 控制台 | http://127.0.0.1:8858 | 常见账号 `sentinel` / `sentinel`（以镜像说明为准） |

### 2.2 Nacos 3.x 镜像与鉴权

Nacos **3.0.3** 镜像启动脚本会校验 **`NACOS_AUTH_TOKEN`**（须为 Base64 字符串），并常配合 `NACOS_AUTH_IDENTITY_KEY` / `NACOS_AUTH_IDENTITY_VALUE` 使用。本项目 `docker-compose.yml` 已示例配置；若你自建环境，可用下面命令生成新 token 后替换：

```bash
openssl rand -base64 32
```

修改 `docker-compose.yml` 后需重新创建容器：

```bash
docker compose down
docker compose up -d
```

查看 Nacos 是否就绪：

```bash
docker compose logs -f nacos
```

---

## 三、作业要求 1：Nacos 注册发现 + 多 Provider + Consumer

### 3.1 服务与端口（默认）

| 模块 | Spring 应用名 | 默认 HTTP 端口 |
|------|----------------|----------------|
| `order-service` | `order-service` | 8081 |
| `product-service` | `product-service` | 8082 |
| `payment-service` | `payment-service` | 8083 |
| `consumer-service` | `consumer-service` | 8090 |

各服务 `application.yml` 中 `spring.cloud.nacos.discovery.server-addr` 均为 `127.0.0.1:8848`，需与 Docker 中 Nacos 端口一致。

### 3.2 编译

```bash
cd /path/to/homework2
mvn clean package -DskipTests
```

### 3.3 启动顺序建议

1. 确认 Nacos、Sentinel 已 `Up`。
2. 启动三个 Provider（顺序不限），再启动 Consumer。

**方式 A：IDE**  
分别运行各模块的主类（`*Application`）。

**方式 B：命令行（示例）**

```bash
# 终端 1
mvn -pl order-service spring-boot:run

# 终端 2
mvn -pl product-service spring-boot:run

# 终端 3
mvn -pl payment-service spring-boot:run

# 终端 4
mvn -pl consumer-service spring-boot:run
```

### 3.4 演示：注册发现（截图建议）

1. 打开 Nacos 控制台 → **服务管理 → 服务列表**。
2. 应能看到：`order-service`、`product-service`、`payment-service`、`consumer-service`。
3. **截图 + 说明**：四个服务均已注册到同一注册中心，体现服务发现能力。

### 3.5 演示：按服务名调用（Feign）

Consumer 使用 **OpenFeign**，客户端上 `@FeignClient(name = "…")` 与 Nacos 中注册的**服务名**一致，由 **Spring Cloud LoadBalancer** 解析实例并发起 HTTP 调用。

在浏览器或终端访问：

| 演示内容 | URL | 预期现象 |
|----------|-----|----------|
| 只调订单服务 | http://127.0.0.1:8090/remote/order | 返回订单实例信息（含端口） |
| 依次调三个生产者 | http://127.0.0.1:8090/remote/all | JSON 中含 order / product / payment 三段返回 |

**截图 + 说明**：说明响应来自不同 `spring.application.name`，且未写死 IP，依赖注册中心服务名。

### 3.6 演示：负载均衡（同一服务名多实例）

默认仅 **一个** `order-service` 进程时，负载均衡仍会工作，但多次请求会打到同一实例，**看不出轮询差异**。

要演示「多实例 + 轮询」：

1. **保持第一个**订单服务在 **8081**（默认配置即可）。
2. 再启 **第二、第三个**订单服务实例，**仅改端口**，服务名仍为 `order-service`，例如：

```bash
mvn -pl order-service spring-boot:run -Dspring-boot.run.arguments=--server.port=9101
mvn -pl order-service spring-boot:run -Dspring-boot.run.arguments=--server.port=9102
```

3. 在 Nacos 中打开 `order-service` → **实例列表**，应看到 3 个实例（8081、9101、9102）。
4. 多次访问：

```text
http://127.0.0.1:8090/remote/order/lb-demo
```

返回 JSON 数组中应交替出现不同 **port=**。

**截图 + 说明**：Nacos 多实例列表一张；`/remote/order/lb-demo` 返回一张；文字说明 LoadBalancer 对 `order-service` 多实例做负载分配。

---

## 四、作业要求 2：Sentinel 五种能力演示

规则在 **`consumer-service`** 启动时由 `SentinelRulesInitializer` 以代码方式加载；控制台用于观察 QPS、熔断状态等。请先启动 **consumer-service**，并确保 `application.yml` 中：

- `spring.cloud.sentinel.transport.dashboard: 127.0.0.1:8858`
- `spring.cloud.sentinel.eager: true`（便于进程启动后即连接控制台）

在 Sentinel 控制台左侧 **机器列表** 中出现你的应用后，再在 **簇点链路 / 实时监控** 中查看资源名。

### 4.0 命令行压测脚本（可选）

在项目根目录执行（需已启动 `consumer-service`）：

```bash
chmod +x scripts/sentinel-demo.sh
./scripts/sentinel-demo.sh
# 或指定地址
BASE_URL=http://127.0.0.1:8090 ./scripts/sentinel-demo.sh
```

脚本会依次高频请求 QPS、热点、慢调用、异常比例、系统规则等接口，便于在终端与 Sentinel 控制台对照观察。

**热点参数与「白名单」类报错**：热点限流要求 `SphU.entry` 与 `entry.exit` 传入**同一组**热点参数；仅依赖 `@SentinelResource` 传参在部分环境下会导致 ParamFlow 统计异常，界面或日志易误报为白名单/校验错误。当前工程已对 `hotSpotDemo` 改为显式 `SphU.entry(..., sku)` / `exit(1, sku)`。若在控制台仍手动加过「授权规则」或冲突的热点规则，请删除后重试。

**若 `/sentinel/hot` 一直返回 500 且看不到限流**：多为 Spring MVC 解析 `@RequestParam` 失败（日志里会有 `IllegalArgumentException: Name for argument ... not specified ... use the '-parameters' flag`）。原因是没有写 `name = "sku"` 且编译未保留参数名。当前已在控制器写 `@RequestParam(name = "sku", ...)`，并在父 `pom.xml` 为 `maven-compiler-plugin` 开启 `<parameters>true</parameters>`。修改后请 **`mvn clean compile` 并重启** consumer。

### 4.1 控制台入口

浏览器打开：http://127.0.0.1:8858 ，进入后选择应用 **`consumer-service`**（名称以 `spring.application.name` 为准）。

### 4.2 各能力对应 HTTP 与操作建议

以下接口基址均为：**http://127.0.0.1:8090**

| 能力 | 路径 | 如何操作演示 | 截图建议 |
|------|------|----------------|----------|
| **QPS 限流** | `/sentinel/qps` | 规则为资源 `qpsDemo` 约 **1 QPS**；在浏览器地址栏**快速连续刷新**或脚本高频请求 | 一张：返回中出现限流/降级文案；可选一张：控制台该资源 QPS |
| **热点参数限流** | `/sentinel/hot?sku=商品A` | 固定同一 `sku` 快速多次请求；再试不同 `sku` 对比 | 说明：同一参数热点阈值内被限流，体现热点参数规则 |
| **慢调用比例熔断** | `/sentinel/slow` | 方法内有延迟；连续请求多次，待统计窗口满足后触发熔断 | 返回出现慢调用熔断相关 block 文案；控制台降级规则或资源 RT |
| **异常比例熔断** | `/sentinel/exception-ratio` | 多次刷新，业务随机异常积累后触发熔断 | 返回 `blocked: exception ratio...` 或 fallback 文案 |
| **CPU 使用率（系统规则）** | `/sentinel/system-cpu` | 全局 `SystemRule` 配置了 `highestCpuUsage`；CPU 持续较高时可能触发系统保护 | 压测 CPU 同时访问该接口；截图系统块或说明文字 |

**说明写作提示**：每条规则对应 Sentinel 中的资源名（如 `qpsDemo`、`hotSpotDemo` 等），可在代码 `SentinelRulesInitializer` / `SentinelDemoService` 中对照。

### 4.3 若控制台看不到应用

1. 确认 `consumer-service` 已启动且无防火墙拦截本机 `8719`（客户端与控制台通信端口，见配置 `spring.cloud.sentinel.transport.port`）。
2. 访问任意 `/sentinel/*` 接口产生流量后，再在控制台刷新 **机器列表**。

---

## 五、作业 Word 文档对照清单（评分用）

作业要求：**每一项至少一张单独截图 + 至少一条说明**。可参考下表自检。

| 作业条目 | 建议截图内容 |
|----------|----------------|
| 注册发现中心 | Nacos 服务列表（含 4 个服务） |
| 3 个 Provider | 同上或分别点开各服务实例 |
| Consumer 按名调用 | `/remote/order` 或 `/remote/all` 响应 |
| 负载均衡 | Nacos 中 `order-service` 多实例 + `/remote/order/lb-demo` 多端口 |
| QPS 限流 | 限流触发时的接口返回或 Sentinel 流控统计 |
| 热点参数限流 | 同上，注明参数 `sku` |
| 慢调用熔断 | 熔断触发返回或降级规则界面 |
| 异常比例熔断 | 同上 |
| CPU 系统规则 | 系统规则或系统块相关说明 + 访问记录 |

Word 文件名按老师要求：**学号-姓名-班级-第N次作业.doc**。

---

## 六、常见问题

**端口被占用**  
修改对应模块 `src/main/resources/application.yml` 的 `server.port`，并注意多订单实例负载均衡时不要冲突。

**无法连接 Nacos**  
确认 `server-addr` 与 Docker 映射端口一致；Nacos 未完全启动时 Provider 会注册失败，可稍后重启应用或查看 Nacos 日志。

**Sentinel 镜像拉取失败**  
若 `docker-compose.yml` 中使用了镜像加速域名，请按你网络环境改回 `bladex/sentinel-dashboard:latest` 等可访问的仓库。

---

## 七、模块结构（便于答辩/写报告）

```text
homework2/
├── docker-compose.yml          # Nacos 3.0.3 + Sentinel 控制台
├── pom.xml                     # 父工程，统一依赖版本
├── order-service/              # Provider：订单
├── product-service/            # Provider：商品
├── payment-service/           # Provider：支付
└── consumer-service/           # Consumer + Sentinel 演示 API
```

更多实现细节可直接阅读 `consumer-service` 下的 `client`（Feign）、`RemoteInvokeController`（远程调用）、`sentinel` 包（规则与演示接口）。
