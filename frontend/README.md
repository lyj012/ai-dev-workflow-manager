# 前端说明

本目录是 `ai-dev-workflow-manager` 的 Vue 3 + TypeScript + Vite 前端项目。

## 技术栈

- Vue 3
- TypeScript
- Vue Router
- Element Plus
- Axios

## 本地开发

```bash
npm install
npm run dev
```

默认通过 Vite 代理把 `/api` 转发到：

```text
http://localhost:8080
```

## 构建

```bash
npm run build
```

## 当前范围

当前前端覆盖 MVP 主链路页面：

- `/tasks`：任务列表、筛选、进入详情。
- `/tasks/create`：任务创建。
- `/tasks/:taskId`：任务详情、workflow 匹配入口、阶段初始化、阶段操作入口、Prompt 生成、阶段输出回填、测试清单、交付总结、Markdown 预览。

后端当前尚未完整实现匹配、阶段推进、Prompt、交付记录等接口；前端已按文档契约预留调用和错误提示。
