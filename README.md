# VoceChat Android Client

[中文](#中文) · [English](#english)

---

## 中文

VoceChat Android 原生客户端，基于 **100% Kotlin + Jetpack Compose + Material Design 3** 打造，已完全移除 Flutter，提供更流畅、更现代的用户交互体验。

### 核心特性

- **现代化架构**
  - 100% Kotlin（JVM 17 / 21）
  - Jetpack Compose + Material Design 3
  - Jetpack Navigation Compose
  - Kotlin Coroutines + StateFlow 响应式状态管理
- **网络通信**
  - REST API：OkHttp 4 + Gson
  - 实时通信：OkHttp-SSE（Server-Sent Events），实时接收消息与在线状态
  - 图片加载：Coil 2（异步缓存与高性能渲染）
- **Material You 界面**
  - Android 12+ 动态壁纸取色，深浅主题自适应
  - 频道与私聊会话、未读状态、成员数展示
  - 文本 / Markdown / 图片预览 / 文件附件 / 语音消息
  - 引用回复、长按表情表态、消息删除
  - 通讯录全部 / 在线过滤、用户详情与一键私聊
  - 服务器连接检测与连接状态提示
- **多语言支持**
  - 内置 7 种语言：简体中文、English、Deutsch、Français、Español、日本語、한국어
  - 跟随系统语言自动切换，也可在 Android 13+ 的「应用语言」设置中单独选择
  - 所有界面文案、错误提示、Toast 与资源字符串均已本地化，无硬编码文案

### 项目结构

```
android/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/privoce/vocechatclient/
│       │   ├── data/          # API、模型、仓库、本地存储
│       │   └── ui/            # Compose 页面、组件、主题、导航
│       └── res/
│           ├── values/            # 默认语言（English）
│           ├── values-zh/         # 简体中文
│           ├── values-de/         # 德语
│           ├── values-fr/         # 法语
│           ├── values-es/         # 西班牙语
│           ├── values-ja/         # 日语
│           ├── values-ko/         # 韩语
│           └── xml/locales_config.xml
└── build.gradle
```

### 编译与运行

使用 Android Studio 打开项目根目录或 `android/` 目录，等待 Gradle Sync 完成后运行：

```bash
cd android
./gradlew assembleDebug
```

生成的 APK：

```
android/app/build/outputs/apk/debug/app-debug.apk
```

> 若仓库中未包含 Gradle Wrapper，可使用本机 Gradle 或让 Android Studio 自动生成 `gradlew`。

### 本地化说明

- 默认语言资源位于 `values/strings.xml`（English），其余语言位于对应的 `values-<locale>/strings.xml`。
- 新增文案时，请同时在所有 `values-*` 目录中补充翻译，避免回退到默认语言。
- `locales_config.xml` 声明了应用支持的语言列表，用于 Android 13+ 的按应用语言设置。
- 带占位符的字符串使用 `%1$s` / `%1$d` 格式，翻译时需保持占位符一致。

---

## English

A native VoceChat Android client built with **100% Kotlin + Jetpack Compose + Material Design 3**. The Flutter implementation has been fully removed, delivering a smoother and more modern user experience.

### Features

- **Modern architecture**
  - 100% Kotlin (JVM 17 / 21)
  - Jetpack Compose + Material Design 3
  - Jetpack Navigation Compose
  - Kotlin Coroutines + StateFlow for reactive state management
- **Networking**
  - REST API: OkHttp 4 + Gson
  - Real-time: OkHttp-SSE (Server-Sent Events) for messages and presence
  - Image loading: Coil 2 (async caching and high-performance rendering)
- **Material You UI**
  - Dynamic wallpaper colors on Android 12+ with light/dark theme support
  - Channels and direct messages, unread state, member counts
  - Text / Markdown / image preview / file attachments / voice messages
  - Quoted replies, long-press reactions, message deletion
  - Contacts with all/online filters, user details, one-tap direct message
  - Server connectivity check and connection status indicator
- **Localization**
  - 7 built-in languages: Simplified Chinese, English, German, French, Spanish, Japanese, Korean
  - Follows the system language automatically; can also be selected per app on Android 13+
  - All UI text, error messages, toasts, and resource strings are localized — no hardcoded copy

### Project structure

```
android/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/privoce/vocechatclient/
│       │   ├── data/          # API, models, repository, local storage
│       │   └── ui/            # Compose screens, components, theme, navigation
│       └── res/
│           ├── values/            # Default locale (English)
│           ├── values-zh/         # Simplified Chinese
│           ├── values-de/         # German
│           ├── values-fr/         # French
│           ├── values-es/         # Spanish
│           ├── values-ja/         # Japanese
│           ├── values-ko/         # Korean
│           └── xml/locales_config.xml
└── build.gradle
```

### Build & run

Open the project root or the `android/` directory in Android Studio, wait for Gradle Sync, then run:

```bash
cd android
./gradlew assembleDebug
```

Output APK:

```
android/app/build/outputs/apk/debug/app-debug.apk
```

> If the Gradle Wrapper is not included in the repository, use your local Gradle installation or let Android Studio generate `gradlew`.

### Localization notes

- Default strings live in `values/strings.xml` (English); other locales live in the corresponding `values-<locale>/strings.xml`.
- When adding new copy, add translations to every `values-*` directory to avoid falling back to the default locale.
- `locales_config.xml` declares the supported languages for Android 13+ per-app language settings.
- Strings with placeholders use `%1$s` / `%1$d`; keep placeholders consistent across translations.
