# GCT Reporter - 前端项目

> **框架**: Vue 3 + TypeScript + Vite
> **UI组件库**: 待集成 Element Plus
> **状态管理**: 待集成 Pinia
> **项目状态**: ✅ 初始化完成

## 项目概述

GCT Reporter 前端项目，基于 Vue 3 组合式 API + TypeScript 开发，使用 Vite 作为构建工具。

本项目是 GCT Reporter 三端架构中的前端部分，包括：
- **管理端** (Admin): 用户管理、角色分配
- **设计端** (Designer): SQL编辑器、报表设计
- **用户端** (Viewer): 报表查询、数据展示、Excel导出

## 技术栈

- **Vue**: 3.5.24
- **TypeScript**: 5.9.3
- **Vite**: 7.2.4
- **构建工具**: vue-tsc + vite

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:5173/

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── src/
│   ├── assets/      # 静态资源
│   ├── components/  # Vue组件
│   ├── App.vue      # 根组件
│   ├── main.ts      # 入口文件
│   └── style.css    # 全局样式
├── public/          # 公共资源
├── index.html       # HTML模板
├── vite.config.ts   # Vite配置
├── tsconfig.json    # TypeScript配置
└── package.json     # 项目依赖

```

## 开发规范

详见项目根目录的 `docs/prompts/分支保护/GitHub Copilot 全局指令.md`

- 遵循 Vue 3 组合式 API 规范
- 使用 TypeScript 类型检查
- 组件命名采用 PascalCase
- 使用 ESLint + Prettier（待配置）

## 参考文档

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [TypeScript 官方文档](https://www.typescriptlang.org/zh/)
- [Vue 3 Script Setup](https://v3.vuejs.org/api/sfc-script-setup.html)
