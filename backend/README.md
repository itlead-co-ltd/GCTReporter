# GCT Report Generator - 后端服务

> SpringBoot 3.1.5 + Java 17 低代码报表生成工具后端服务

## 项目信息

- **项目名称**: GCT Report Generator
- **Group**: com.gct
- **Artifact**: report-generator
- **版本**: 0.0.1-SNAPSHOT
- **Java版本**: 17
- **SpringBoot版本**: 3.1.5

## 技术栈

- **核心框架**: Spring Boot 3.1.5
- **Web层**: Spring Boot Web (Tomcat 10.1.15)
- **数据访问**: Spring Data JPA + Hibernate 6.2.13
- **数据库**: SQLite (开发环境) / Oracle 12g (生产环境)
- **数据库迁移**: Flyway 9.16.3
- **Excel处理**: Apache POI 5.2.5
- **工具库**: Lombok
- **测试框架**: JUnit 5 + Mockito

## 目录结构

```
backend/
├── pom.xml                                 # Maven配置文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/gct/reportgenerator/
│   │   │       ├── ReportGeneratorApplication.java  # 应用入口
│   │   │       ├── controller/                       # 控制器层
│   │   │       ├── service/                          # 服务层
│   │   │       ├── repository/                       # 数据访问层
│   │   │       ├── entity/                           # 实体类
│   │   │       ├── dto/                              # 数据传输对象
│   │   │       └── config/                           # 配置类
│   │   └── resources/
│   │       ├── application.yml                       # 应用配置
│   │       └── db/migration/                         # Flyway迁移脚本
│   └── test/
│       └── java/
│           └── com/gct/reportgenerator/
│               └── ReportGeneratorApplicationTests.java
└── data/                                             # SQLite数据库文件 (git忽略)
```

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.8+

### 编译项目

```bash
mvn clean compile
```

### 运行项目

```bash
mvn spring-boot:run
```

### 运行测试

```bash
mvn test
```

### 打包项目

```bash
mvn clean package
```

## 配置说明

### application.yml

```yaml
spring:
  application:
    name: gct-report-generator
  
  datasource:
    url: jdbc:sqlite:./data/report.db      # SQLite数据库路径
    driver-class-name: org.sqlite.JDBC
  
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: validate                    # 生产环境使用validate
    show-sql: true                           # 显示SQL语句
  
  flyway:
    enabled: true                            # 启用Flyway
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: 8080                                 # 服务端口
```

## API文档

服务启动后访问: `http://localhost:8080`

API文档将通过Swagger/SpringDoc自动生成 (待配置)

## 开发规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok减少样板代码
- 单元测试覆盖率 > 80%
- SQL使用参数化查询（防止SQL注入）
- 所有公共API添加JavaDoc注释

## 数据库

### 开发环境

使用SQLite数据库，数据文件位于 `./data/report.db`

### 生产环境

使用Oracle 12g数据库，连接配置通过环境变量注入

### 数据库迁移

使用Flyway管理数据库版本，迁移脚本位于 `src/main/resources/db/migration/`

命名规范: `V<版本号>__<描述>.sql`
- 例如: `V1__init_schema.sql`
- 例如: `V2__add_user_table.sql`

## 部署

### 生产环境部署

1. 修改application-prod.yml配置
2. 打包应用: `mvn clean package -Pprod`
3. 运行jar包: `java -jar target/report-generator-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`

### Docker部署 (待实现)

```bash
docker build -t gct-report-generator:latest .
docker run -p 8080:8080 gct-report-generator:latest
```

## 许可证

Copyright © 2026 GCT Team

---

**任务**: T000-5 后端项目初始化  
**状态**: ✅ 已完成  
**完成时间**: 2026-01-15
