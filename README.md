# VoceChat Android 客户端 (Kotlin 原生重构版)

VoceChat Android 官方客户端原生版本，已完全移除 Flutter，基于 **100% Kotlin + Jetpack Compose + Material Design 3** 打造，提供更流畅、更现代化、更友好的用户交互体验。

---

## 🌟 核心特性与技术栈

### 1. 现代化架构
- **编程语言**：100% Kotlin (JVM 17 / 21)
- **UI 框架**：Jetpack Compose + Material Design 3 (`androidx.compose.material3`)
- **导航组件**：Jetpack Navigation Compose (`androidx.navigation:navigation-compose`)
- **网络通信**：
  - REST API：`OkHttp 4` + `Gson`
  - 实时通信：`OkHttp-SSE` (Server-Sent Events) 双向保持，实时接收消息与状态更新
- **图片加载**：`Coil 2` (异步缓存与高性能渲染)
- **异步响应**：Kotlin Coroutines + StateFlow 响应式状态管理

### 2. Material Design 3 界面与友好交互
- 🎨 **Material You 动态取色**：支持 Android 12+ 动态壁纸色彩提取与深浅主题自适应。
- 💬 **频道与私聊会话**：支持公开/私有频道、未读状态、成员数显示、直接私聊。
- 📱 **现代对话页面**：
  - Material 3 椭圆气泡卡片（发送者高亮与接收者容器色彩）。
  - 支持文本、Markdown、图片预览、文件附件及多媒体展示。
  - 引用回复（悬浮引用指示器及一键撤回）、长按消息快捷表情表态（Reactions）。
- 👥 **通讯录**：支持全部/在线过滤，查看详细用户信息并一键发起对话。
- ⚙️ **设置面板**：清晰的服务器状态、用户信息卡片、深色模式与动态颜色切换。
- 🌐 **服务器连接检测**：友好引导连接自托管或官方 VoceChat 服务端，即时验证网络连通性。

---

## 🚀 编译与运行

在 Android Studio 中直接打开项目根目录或 `android/` 目录：

```bash
cd android
./gradlew assembleDebug
```

生成 APK 文件路径：
`android/app/build/outputs/apk/debug/app-debug.apk`
