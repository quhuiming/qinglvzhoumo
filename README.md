# 情侣周末

一个温柔日常风格的情侣升温 App，使用 `uni-app + Vue3` 构建，支持本地优先保存，也可以通过 Java 后端创建情侣空间，在两台设备之间同步共同愿望、每日回答和文字回忆。

## 功能

- 首页：恋爱天数、纪念日提示、双方今日回答、抽小计划、对方动态、最近完成和最近回忆。
- 清单：新增、编辑、删除、完成/取消共同愿望，并在愿望完成后引导记录成回忆。
- 回忆：查看每日记录、双方回答、完成小计划和手动文字回忆，支持日期选择和本地图片选择。
- 我们：编辑双方昵称、恋爱开始日期、查看统计、配置后端地址、创建/加入/退出情侣空间。
- 数据：默认保存在本机；绑定情侣空间后，同步昵称、愿望、文字回忆、每日回答和小计划记录。照片文件本身仍保留在本机。

## 源码结构

前端源码以 `src/` 为唯一入口，根目录不再保留重复的 `pages/`、`store/`、`styles/`、`utils/`。开发时请只修改 `src/` 下的页面、状态和样式。

## 安装

```bash
npm install
```

## 本地运行

```bash
npm run dev:h5
```

默认会启动 H5 预览服务，可在浏览器中访问终端输出的地址。

如需联调共享空间，先启动 Java 后端：

```bash
cd backend
mvn -s maven-settings.xml spring-boot:run
```

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
