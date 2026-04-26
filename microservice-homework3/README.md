# homework3 - Spring Cloud Gateway + Seata

本项目按作业要求提供了两部分：

1. Spring Cloud Gateway 通过微服务名路由 + 负载均衡 + 断言匹配
2. Seata 分布式事务（正常提交 / 异常回滚）

## 一、启动准备

### 1) 环境

- JDK 17
- Maven 3.8+
- MySQL 8
- Seata Server 1.7.x（默认端口 8091）

### 2) 初始化数据库

在 MySQL 中执行：

- `sql/inventory_db.sql`
- `sql/order_db.sql`

默认账号密码写在配置中：`root/root`，可按需修改 `application.yml`。

### 3) 启动 Seata Server（Docker 示例）

```bash
docker run --name sealand-seata-server -p 8091:8091 -p 7091:7091 -e SEATA_IP=127.0.0.1 seataio/seata-server
```

## 二、启动服务

按顺序启动：

1. `inventory-service`（9001）
2. `inventory-service` 第二实例（9002）：激活 profile `node2`
3. `order-service`（9010）
4. `gateway-service`（8080）

命令行示例：

```bash
mvn -pl inventory-service spring-boot:run
mvn -pl inventory-service spring-boot:run -Dspring-boot.run.profiles=node2
mvn -pl order-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

## 三、作业测试命令（可直接截图）

### 1) Gateway 通过微服务名访问 + 负载均衡

连续调用 4~6 次，观察返回里的 `instancePort` 在 `9001` / `9002` 间切换：

```bash
curl "http://127.0.0.1:8080/lb/echo"
```

### 2) Path 断言

```bash
curl "http://127.0.0.1:8080/predicate/path/check"
```

### 3) Header 断言

```bash
curl -H "X-Auth: gateway" "http://127.0.0.1:8080/predicate/header/check"
```

### 4) Query 断言

```bash
curl "http://127.0.0.1:8080/predicate/query/check?from=gateway"
```

### 5) Method 断言

```bash
curl -X POST "http://127.0.0.1:8080/predicate/method/check"
```

### 6) Seata 正常提交

```bash
curl -X POST "http://127.0.0.1:8080/seata/order/create?userId=1&productId=1&count=2&simulateFail=false"
```

预期：返回“下单成功，事务提交”，并且订单表新增记录、库存减少。

### 7) Seata 异常回滚

```bash
curl -X POST "http://127.0.0.1:8080/seata/order/create?userId=1&productId=1&count=2&simulateFail=true"
```

预期：接口报错，`orders` 和 `inventory` 都不发生最终变更（全局回滚）。

## 四、Word 文档建议结构（按评分标准）

建议文件名：`学号-姓名-班级-第N次作业.doc`

1. 封面（课程、姓名、学号、班级）
2. 环境信息（JDK/Maven/MySQL/Seata 版本）
3. Gateway 微服务名访问 + 负载均衡截图（至少 1 张）+ 说明
4. Path 断言截图 + 说明
5. Header 断言截图 + 说明
6. Query 断言截图 + 说明
7. Method 断言截图 + 说明
8. Seata 正常提交截图（接口 + 数据库）+ 说明
9. Seata 异常回滚截图（接口 + 数据库）+ 说明

说明建议写法：每张图写“测试目的、测试命令、实际结果、结论”四行。
