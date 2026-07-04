# 后端说明

## 技术栈规划

第一版后端计划使用：

- Spring Boot
- MyBatis-Plus
- MySQL
- Knife4j

当前阶段已创建 Spring Boot 后端 MVP Phase 1，先实现任务创建、分页查询和详情查询。

## 模块规划

- `task`：任务创建、查询、状态管理。
- `template`：workflow 模板和阶段模板管理。
- `matching`：根据任务类型、复杂度和风险标签匹配 workflow 模板。
- `stage`：任务阶段初始化、推进和输出记录。
- `prompt`：阶段指令、测试清单和交付总结生成。
- `delivery`：交付记录和 Markdown 导出。

## 第一版接口方向

- 任务创建和查询。
- 模板创建、查询和维护。
- 模板匹配。
- 阶段初始化和推进。
- 阶段输出记录。
- 指令生成。
- 测试清单生成。
- 交付总结生成。
- Markdown 导出。

## 本地验证

```bash
mvn test
```

启动服务前需先创建 MySQL 数据库并执行 `../sql/schema.sql`，再按环境调整 `src/main/resources/application.yml` 中的数据源配置。
