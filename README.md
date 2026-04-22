# 情侣周末

一个温柔日常风格的情侣升温单机 App，使用 `uni-app + Vue3` 构建。当前版本支持本地保存情侣信息、恋爱天数、随机小计划、共同愿望和回忆记录。

## 功能

- 首页：恋爱天数、今日问题、抽小计划、最近完成记录、最近回忆。
- 清单：新增、编辑、删除、完成/取消共同愿望，并提供愿望模板。
- 回忆：新增、编辑、删除文字回忆，支持日期选择和本地图片选择。
- 我们：编辑双方昵称、恋爱开始日期、查看统计、重置本地数据。
- 数据：全部保存在本机，不做登录、情侣绑定或云同步。

## 安装

```bash
npm install
```

## 本地运行

```bash
npm run dev:h5
```

默认会启动 H5 预览服务，可在浏览器中访问终端输出的地址。

## 构建

```bash
npm run build:h5
npm run build:app
```

APP 构建完成后，使用 HBuilderX 导入 `dist/build/app` 继续真机运行或云打包。

## APP 打包备注

- `src/manifest.json` 当前使用本地占位 appid：`__UNI__QINGLVZHOUMO_LOCAL`。
- `static/app-icon.svg` 和 `static/splash.svg` 是占位素材，正式发布前建议替换为平台要求尺寸的 PNG。
- 当前版本申请了相册和相机相关权限，用于本地选择/拍摄回忆照片。
- 隐私说明见 [PRIVACY.md](./PRIVACY.md)。

## GitHub

仓库地址：[https://github.com/quhuiming/qinglvzhoumo](https://github.com/quhuiming/qinglvzhoumo)
