# 汉化说明

本文档记录本项目简体中文本地化的全部改动，便于维护、审查与后续扩展。

## 概述

本项目原为纯英文界面，且界面文案**大量硬编码**在 Kotlin 源码的字符串字面量中，仅 4 条文本存放于 `values/strings.xml`。

本次汉化采用 Android 标准的资源化方案：

1. 将散落在代码中的硬编码文案**全部提取**为字符串资源
2. 新建 `values-zh-rCN` 中文资源目录
3. 系统按设备语言自动切换，**英文设备不受影响**

## 改动统计

| 项目 | 数量 |
|---|---|
| 修改的 Kotlin 文件 | 9 个 |
| 代码中资源引用点 | 63 处 |
| 新增字符串资源 | 64 条（中英各 64 条，完全对齐） |
| 新增目录 | `app/src/main/res/values-zh-rCN/` |

## 文件改动清单

### 新增文件

| 文件 | 说明 |
|---|---|
| `app/src/main/res/values-zh-rCN/strings.xml` | 简体中文文案，64 条 |

### 修改文件

| 文件 | 引用数 | 改动内容 |
|---|---|---|
| `ui/screen/SettingsScreen.kt` | 33 | 设置页全部文案；主题选项数据结构重构 |
| `ui/screen/HomeScreen.kt` | 17 | 顶栏、开关项、两个弹窗、Toast |
| `core/notification/DefaultNotification.kt` | 5 | 通知标题、倒计时文本、3 个操作按钮 |
| `ScreenOffTimerApp.kt` | 2 | 通知渠道名称与描述 |
| `utils/MediaUtils.kt` | 2 | 返回桌面的兜底通知 |
| `service/MyDeviceAdminReceiver.kt` | 1 | 设备管理员授权说明 |
| `ui/components/settings/ListSection.kt` | 1 | 图标无障碍描述 |
| `ui/screen/LibrariesScreen.kt` | 1 | 开源库页面标题 |
| `ui/components/timer/TimerProgress.kt` | 1 | 倒计时单位文本 |
| `res/values/strings.xml` | — | 英文基准扩充，由 4 条增至 64 条 |

## 关键技术要点

### 1. Composable 上下文使用 `stringResource`

界面代码中通过 `stringResource(R.string.xxx)` 读取资源：

```kotlin
Text(text = stringResource(R.string.home_title))
```

### 2. 非 Composable 上下文使用 `getString`

`Toast`、`NotificationCompat.Builder`、`NotificationChannel` 等场景不在 Compose 作用域内，必须使用 `context.getString()`：

```kotlin
// HomeScreen.kt - Toast
Toast.makeText(context, context.getString(R.string.toast_enable_notification), Toast.LENGTH_LONG).show()

// DefaultNotification.kt - 通知
.setContentTitle(context.getString(R.string.notification_title))
```

### 3. 主题选项数据结构重构

原代码使用 `Pair` 列表存储「显示名 → 枚举」映射：

```kotlin
// 原始写法
val options = listOf(
    "Light" to ThemeMode.LIGHT,
    "Dark" to ThemeMode.DARK,
    "System" to ThemeMode.SYSTEM
)
```

由于 `stringResource()` 只能在 `@Composable` 中调用，无法直接写入列表构造，改为分离「枚举列表」与「显示名列表」：

```kotlin
// 改造后
val options = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
val optionLabels = listOf(
    stringResource(R.string.theme_light),
    stringResource(R.string.theme_dark),
    stringResource(R.string.theme_system)
)
```

相应地，`options[index].second` 改为 `options[index]`，`it.second == settings.theme` 改为 `it == settings.theme`。

### 4. AnnotatedString 场景

设备管理员说明弹窗使用 `buildAnnotatedString` 构建富文本，可正常调用 `stringResource`：

```kotlin
val descriptionText = buildAnnotatedString {
    append(stringResource(R.string.dialog_device_admin_desc_body))
    withStyle(style = SpanStyle(fontSize = 12.sp)) {
        append(stringResource(R.string.dialog_device_admin_desc_line1))
        append(stringResource(R.string.dialog_device_admin_desc_line2))
    }
}
```

### 5. 系统级文案

以下 4 条资源由**系统读取**，代码中不会出现 `R.string` 引用，但必须存在才能被系统汉化：

- `app_name` — 应用名称（桌面图标下方显示）
- `accessibility_summary` — 无障碍服务摘要
- `accessibility_description` — 无障碍服务详细说明
- `device_admin_description` — 设备管理员说明

## 生效方式

无需任何配置，系统按语言自动切换：

| 设备语言 | 显示语言 | 资源目录 |
|---|---|---|
| 简体中文 | 中文 | `values-zh-rCN/` |
| 其他语言 | 英文 | `values/` |

## 资源命名规范

本次新增资源遵循以下命名约定：

| 前缀 | 用途 | 示例 |
|---|---|---|
| `home_` | 主界面 | `home_title` |
| `settings_` | 设置项 | `settings_go_home_title` |
| `dialog_` | 弹窗 | `dialog_lock_method_title` |
| `action_` | 按钮 | `action_agree`、`action_cancel` |
| `notification_` | 通知 | `notification_title` |
| `theme_` | 主题选项 | `theme_light` |
| `option_` | 功能开关 | `option_lock_screen_title` |
| `toast_` | 轻提示 | `toast_enable_notification` |

## 后续扩展

如需支持其他语言，只需新建对应资源目录（如 `values-ja`、`values-ko`）并复制一份 `strings.xml` 翻译即可，**无需修改任何代码**。

## 校验结果

改动完成后进行的静态校验：

- 中英文资源 key 数量一致（64 / 64），无缺失、无冗余、无重复
- 代码引用的 60 个 key 全部存在，无悬空引用
- 所有含 `stringResource` 的文件均已正确 import
- 扫描全部 Kotlin 源码，未发现残留的英文 UI 字面量
- XML 中无未转义的裸 `&` 字符

> 注：本次改动未进行 Gradle 编译验证，首次构建时建议关注 `SettingsScreen.kt` 主题选项部分与 `HorizontalSelected` 组件的参数匹配。

## 许可证

与主项目一致，遵循 [Apache License 2.0](LICENSE)。
