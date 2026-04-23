# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

情侣周末是一个温柔日常风格的情侣升温单机 App，使用 `uni-app + Vue3` 构建。数据全部保存在本地，不做登录或云同步。

## 常用命令

```bash
# H5 本地开发
npm run dev:h5

# H5 构建
npm run build:h5

# App 构建（需 HBuilderX 导入 dist/build/app 继续打包）
npm run build:app
```

## 架构

### 状态管理
所有数据集中在 `store/love.js`，使用 `uni.getStorageSync` / `uni.setStorageSync` 做本地持久化。state 结构：

- `profile` - 双方昵称、恋爱开始日期
- `plans` - 随机小计划模板（12个默认计划）
- `questions` - 每日问题列表（12个）
- `wishes` - 共同愿望清单
- `memories` - 回忆记录
- `today` - 当日状态（日期、今日计划、已完成计划）
- `planHistory` - 最近完成的计划历史（最多50条）

### 页面结构
4个 tab 页面在 `src/pages/` 下：
- `today/` - 首页（恋爱天数、今日问题、抽小计划）
- `wishes/` - 愿望清单
- `memories/` - 回忆记录
- `profile/` - 个人信息设置

### 路由配置
`pages.json` 定义页面路径和 tabBar 配置。注意根目录和 `src/` 目录各有一份 `pages.json`，构建时会使用 `src/pages.json`。

### 工具函数
`utils/date.js` 提供日期相关工具函数。

### 样式
`styles/common.css` 定义全局样式（主色调粉色系 `#ffe9dd` / `#fff6ee`，强调色 `#ff6d57`）。

## 重要约束

**改动前必须征得同意**：如果要对项目做任何改动（代码、配置、依赖等），必须先告诉用户具体改什么，获得同意后才能执行。不能擅自修改任何文件。

## 注意事项

- `manifest.json` 中使用的是本地占位 appid `__UNI__QINGLVZHOUMO_LOCAL`，正式发布需要替换
- 图片资源放在 `static/` 目录
- App 构建后需要用 HBuilderX 导入 `dist/build/app` 继续完成云打包或真机运行