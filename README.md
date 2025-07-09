# 智能巡检终端

本项目是地铁隧道巡线车智能巡检系统的手持终端，基于 Vue 3 + Vite 构建，配合后端 API 实现车辆控制、任务管理、缺陷采集与复盘、数据上传等功能，适用于 AGV 智能巡检场景。

---

## 一、项目简介

本系统为地铁隧道巡线车智能巡检的手持终端，支持任务全流程管理、车辆与摄像头控制、缺陷采集与复盘、数据上传等功能。前端采用现代化技术栈，界面友好，易于维护和扩展。

---

## 二、核心功能模块

### 1. 系统自检（InitView）
- 启动后自动检测文件系统、数据库、车辆控制系统、摄像头等依赖。
- 检查通过后方可进入系统主页。
- 检查项及对应API：
  - 文件系统：`GET /system/check/fs`
  - 数据库：`GET /system/check/db`
  - 车辆控制系统：`GET /system/check/agv`
  - 摄像头：`GET /system/check/cam`
- 检查失败时会显示详细错误信息及解决建议。

### 2. 系统设置（SystemSettings）
- 支持配置车辆IP、控制端口、分析端口、云端地址、四路摄像头参数（地址、账号、密码）。
- 配置项通过表单界面管理，保存后实时生效。
- 配置数据通过API与后端同步：
  - 获取配置：`GET /agv/config`
  - 更新配置：`PUT /agv/config`
- 配置项校验严格，防止无效参数导致系统异常。

### 3. 任务管理
#### 任务列表（TaskList）
- 展示所有巡检任务，支持按任务编号、创建人、执行人、状态等多条件筛选。
- 支持分页、重置、刷新。
- 主要API：`GET /agv/task/list`

#### 任务执行（TaskExecuteView）
- 实时展示车辆位置、任务进度、摄像头视频流。
- 支持车辆前进、后退、停止等控制（通过API控制AGV）。
- 缺陷采集：在巡视过程中可标注缺陷，采集图片、视频流。
- 缺陷点在进度条上有可视化标记，点击可查看详情。
- 主要API：
  - AGV控制：`POST /agv/movement/forward`、`POST /agv/movement/backward`、`POST /agv/movement/stop`
  - 获取摄像头列表：`GET /easy-api/devices`
  - 获取任务详情：`GET /agv/task/{id}`
  - 新增缺陷：`POST /agv/flaw`

#### 任务复盘（TaskReview）
- 展示任务所有缺陷，支持图片预览、缺陷确认、补充说明。
- 缺陷确认后可上传。
- 主要API：
  - 获取缺陷列表：`GET /agv/flaw/list`
  - 更新缺陷：`PUT /agv/flaw`
  - 检查缺陷是否全部确认：`GET /agv/flaw/check/{id}`

#### 数据上传（TaskUpload）
- 任务完成后，支持一键上传所有采集数据至云端。
- 上传过程有进度条和详细状态提示。
- 主要API：`POST /agv/task/upload`

---

## 三、技术实现与依赖说明

- **Vue 3**：主流渐进式前端框架，采用 Composition API。
- **Vite**：极速开发与构建工具，支持热更新。
- **Element Plus**：UI 组件库，提供表单、弹窗、进度条等丰富组件。
- **Vuestic UI**：部分界面风格补充。
- **Pinia**：状态管理，管理全局数据。
- **Vue Router**：路由管理，页面跳转。
- **Axios**：HTTP 请求库，统一API调用。
- **EasyPlayer**：集成播放器，支持多路摄像头视频流。
- **Vitest**：单元测试框架。
- **ESLint/Prettier**：代码规范与格式化。

依赖详见 `package.json`，开发建议使用 VSCode + Volar 插件。

---

## 四、目录结构说明

```
vehicleTerminal/
├── public/                # 静态资源（含 EasyPlayer 播放器）
├── src/
│   ├── api/               # API 封装（index.js、vehicle.js、taskApi.js）
│   ├── assets/            # 静态资源与样式（base.css、main.css、logo.svg）
│   ├── router/            # 路由配置（index.js）
│   ├── stores/            # Pinia 状态管理（counter.js）
│   ├── utils/             # 工具函数（request.js：Axios实例与拦截器）
│   ├── views/             # 主要页面
│   │   ├── systemCheck/   # 系统自检与设置
│   │   └── task/          # 任务相关页面
│   └── main.js            # 应用入口
├── package.json           # 依赖与脚本
├── vite.config.js         # Vite 配置（含API代理）
├── vitest.config.js       # 测试配置
├── README.md              # 项目说明
└── 手持终端接口文档V1.2.md # 后端接口文档
```

---

## 五、前后端交互与数据流

- **API统一代理**：所有 `/prod-api` 请求由 Vite 代理到后端服务器，开发环境无需修改代码即可切换后端。
- **接口调用**：所有API调用均封装在 `src/api/` 下，便于维护和统一错误处理。
- **数据流**：页面通过API获取数据，Pinia管理全局状态，组件间通过props和事件通信。
- **异常处理**：所有API响应均有统一拦截与错误提示，关键操作有弹窗确认。

---

## 六、开发调试与配置细节

- **端口与代理**：默认开发端口5173，API代理见 `vite.config.js`，如需切换后端地址只需修改代理配置。
- **摄像头与车辆参数**：务必在“系统设置”页面正确填写，否则相关功能无法使用。
- **EasyPlayer播放器**：相关js/wasm文件需放置于 `public/js/`，页面通过 `<script src="./js/EasyPlayer-pro.js"></script>` 引入。
- **环境变量**：如需扩展环境变量，可参考Vite官方文档，当前主要依赖代理配置。

---

## 七、测试与质量保障

- **单元测试**：使用 Vitest，测试用例可扩展至 `src/` 各业务模块。
- **代码规范**：ESLint + Prettier，提交前建议执行 `pnpm lint` 和 `pnpm format`。
- **推荐工具**：VSCode + Volar 插件，便于类型提示和代码补全。

---

## 八、常见问题与排查建议

1. **接口无法访问/跨域报错**
   - 检查后端服务是否启动，或 `vite.config.js` 代理配置是否正确。
   - 生产环境建议使用 Nginx 反向代理。
2. **摄像头/车辆连接失败**
   - 请在“系统设置”中检查 IP、端口、账号密码等参数，确保与后端实际配置一致。
   - 检查网络连通性。
3. **页面空白或报错**
   - 检查依赖是否安装完整，建议删除 `node_modules` 后重新 `pnpm install`。
   - 查看浏览器控制台报错信息。
4. **EasyPlayer无法播放视频**
   - 检查 `public/js/` 下播放器文件是否齐全，摄像头流地址是否正确。
5. **任务/缺陷数据异常**
   - 检查后端接口返回数据结构，或参考接口文档核对参数。

---

## 九、接口文档与数据结构

详见《手持终端接口文档V1.2.md》，主要接口包括：
- 系统配置相关：`/agv/config`
- 巡检任务相关：`/agv/task/list`、`/agv/task/{id}`、`/agv/task/upload`
- 缺陷管理相关：`/agv/flaw/list`、`/agv/flaw`、`/agv/flaw/check/{id}`
- AGV控制相关：`/agv/movement/forward`、`/agv/movement/backward`、`/agv/movement/stop`
- 系统检查相关：`/system/check/fs`、`/system/check/db`、`/system/check/agv`、`/system/check/cam`
- 摄像头服务相关：`/easy-api/devices`

接口返回结构统一为：
- `AjaxResult`：`{ code, msg, data }`
- `TableDataInfo`：`{ total, rows, code, msg }`

---

## 十、贡献与反馈

如需二次开发、问题反馈或功能建议，请联系项目维护者或提交 Issue。

---

如需更详细的接口说明、数据结构、业务流程图等，请参考项目内的《手持终端接口文档V1.2.md》或联系开发团队。
