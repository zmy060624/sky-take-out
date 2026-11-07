# 云厨智链 · 餐饮外卖点餐与配送系统（后端）

面向餐饮商户的外卖业务后端系统，覆盖 **C 端用户点餐下单** 与 **B 端商家经营管理** 两条主线，实现从菜品上架、购物车、下单支付、商家接单、配送到运营数据统计的完整业务闭环。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 核心框架 | Spring Boot 2.7、Spring MVC |
| 持久层 | MyBatis-Plus、MySQL 8.0、Druid 连接池 |
| 缓存 | Redis（店铺营业状态、购物车数据） |
| 实时通信 | WebSocket（商家来单提醒、用户催单） |
| 鉴权 | JWT + HandlerInterceptor（管理端 / 用户端双拦截器） |
| 任务调度 | Spring Task（超时未支付订单自动取消） |
| 文件存储 | 阿里云 OSS |
| 报表导出 | Apache POI |
| 辅助能力 | 百度地图 API（配送范围校验） |

## 功能模块

**商家管理端**

| 模块 | 说明 |
| --- | --- |
| 员工管理 | 登录认证、新增、分页查询、启用禁用、信息编辑 |
| 分类管理 | 菜品分类 / 套餐分类的增删改查、类型与排序控制 |
| 菜品管理 | 菜品 CRUD、口味配置、起售停售、批量删除、条件分页查询 |
| 套餐管理 | 套餐 CRUD、套餐与菜品关联、起售停售 |
| 店铺管理 | 营业状态设置与查询（状态缓存在 Redis） |
| 订单管理 | 接单、拒单、取消、派送、完成、条件搜索与统计 |
| 数据统计 | 营业额、订单量、用户量统计，销量 Top10，运营数据 Excel 导出 |
| 工作台 | 当日经营数据概览 |

**用户端**

| 模块 | 说明 |
| --- | --- |
| 微信登录 | 小程序 code 换取 openid，签发 JWT |
| 菜品浏览 | 分类查询、菜品列表、套餐详情、菜品口味选择 |
| 购物车 | 加购、减购、清空、列表查询 |
| 下单支付 | 提交订单、微信支付、支付结果回调 |
| 订单跟踪 | 订单列表、详情、取消、再来一单、催单 |

## 项目结构

```
sky-take-out
├── sky-common          # 公共模块：常量、异常、工具类、统一返回结果
├── sky-pojo            # 实体模块：Entity / DTO / VO
├── sky-server          # 业务模块：controller / service / mapper / config / interceptor
│   └── src/main/java/com/sky
│       ├── controller  # admin（管理端）、user（用户端）、notify（支付回调）
│       ├── service     # 业务逻辑
│       ├── mapper      # 数据访问
│       ├── interceptor # JWT 鉴权拦截器
│       ├── handler     # 全局异常处理
│       ├── task        # 定时任务
│       └── webSocket   # 实时消息推送
└── sql                 # 建库脚本
```

## 本地运行

### 环境要求

- JDK 8+
- MySQL 8.0
- Redis 5.0+
- Maven 3.6+

### 1. 初始化数据库

```bash
mysql -uroot -p -e "CREATE DATABASE sky_take_out DEFAULT CHARSET utf8mb4;"
mysql -uroot -p sky_take_out < sql/sky.sql
```

### 2. 配置环境变量

第三方密钥全部通过环境变量注入，代码中不保存任何真实凭据：

| 变量名 | 说明 | 是否必需 |
| --- | --- | --- |
| `ALIYUN_OSS_ACCESS_KEY_ID` | 阿里云 OSS AccessKey ID | 使用图片上传时必需 |
| `ALIYUN_OSS_ACCESS_KEY_SECRET` | 阿里云 OSS AccessKey Secret | 使用图片上传时必需 |
| `ALIYUN_OSS_BUCKET_NAME` | OSS Bucket 名称 | 使用图片上传时必需 |
| `ALIYUN_OSS_ENDPOINT` | OSS 区域节点，默认 `oss-cn-beijing.aliyuncs.com` | 否 |
| `WECHAT_APPID` | 微信小程序 AppID | 使用微信登录 / 支付时必需 |
| `WECHAT_SECRET` | 微信小程序 AppSecret | 使用微信登录 / 支付时必需 |
| `BAIDU_MAP_AK` | 百度地图开放平台 AK | 使用配送范围校验时必需 |

在 IntelliJ IDEA 中配置：`Run` → `Edit Configurations` → `Environment variables` 填入上述键值对。
数据库连接、Redis 地址等非敏感项在 `application-dev.yml` 中直接配置。

### 3. 启动

```bash
mvn clean package -DskipTests
java -jar sky-server/target/sky-server-*.jar
```

服务默认端口 `8080`。

## 实现说明

- **统一响应与异常**：自定义 `Result` 统一返回结构，`GlobalExceptionHandler` 集中处理业务异常、参数校验异常与未捕获异常，避免 Controller 重复书写 try-catch。
- **双端鉴权**：管理端与用户端各用一套 JWT 拦截器，令牌名与密钥相互隔离，防止越权访问。
- **超时订单治理**：Spring Task 定时扫描超时未支付订单，自动取消并回滚库存，防止订单长期悬挂占用库存。
- **实时提醒**：WebSocket 长连接推送新订单至商家端并触发语音播报，替代前端轮询。
- **报表导出**：基于 Apache POI 模板填充方式生成运营数据 Excel，保留原始表头样式。

## 声明

本项目的业务模型参考公开教学案例，**代码为本人独立完成**，并在统一异常处理、双端鉴权、定时任务与 WebSocket 推送等模块做了补充实现。仓库中的第三方密钥均已替换为环境变量占位符，不存在凭据泄露。
