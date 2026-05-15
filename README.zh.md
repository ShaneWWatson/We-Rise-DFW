# We Rise DFW

> **翻译版本：
** [English](README.md) · [Español](README.es.md) · [العربية](README.ar.md) · [中文](README.zh.md)

一款面向达拉斯 / 沃思堡（DFW）地区的开源 Android 应用，根据用户当前位置展示附近的**食物**、**衣物**和*
*庇护所**等服务提供机构。由 **We Rise DFW**（Shane W. Watson）开发，按 MIT
许可证免费开放——见 [LICENSE](LICENSE)。

## 立项原因

DFW 地区有许多人——包括正面临住房、食品、康复或突发流离失所等困境的群体——既没有充裕的时间，也没有稳定的网络或手机来翻阅冗长的服务目录。We
Rise DFW 的目标是提供一种快速、低门槛的方式，让他们立刻看到身边可用的资源。

> **想了解如何使用本应用？** 请查看 [USER_GUIDE.zh.md](USER_GUIDE.zh.md) ——面向最终用户编写，无需技术背景。本
> README 面向开发者。

## 隐私第一

- 仅在用户点击 **搜索** 或 **在线查找更多** 时读取一次位置。
- **绝不写入磁盘**，绝不记录日志，绝不分享给任何分析服务。
- 本地 Room 数据库只缓存服务提供机构的列表（以便离线使用）；用户位置不在缓存范围内。
- 翻译通过 Google ML Kit 在设备本地完成，文本不会离开手机。
- 仅有的对外网络请求来自：OpenStreetMap 地图瓦片、显式触发的"在线查找更多"对公共 Overpass API 的查询，以及
  ML Kit 的一次性语言模型下载。

## 功能

- 三个标签页：**食物**、**衣物**、**庇护所**
- 用户可调节搜索半径（1–25 英里）
- 信仰背景的服务提供机构标有十字图标，并可在设置中选择是否包含
- 当前营业状态：开放时显示绿点，关闭时显示红点
- 点击地址 → 在默认地图应用中打开
- 点击电话 → 在默认拨号应用中打开
- 点击网站 → 在默认浏览器中打开
- 上半屏地图，红/绿大头针匹配当前选中的标签
- 黑红配色方案
- 严格限制在 DFW 都会区；超出此范围时地图会显示"超出范围"提示
- **在线查找更多** 按钮可从 OpenStreetMap 拉取额外的服务提供机构并合并到本地缓存
- **语言选择器**支持约 59 种语言，使用 Google ML Kit 在设备上翻译（默认英语）

## 技术栈

- Kotlin · AndroidX · 经典 XML 视图（刻意保留——可保持安装包小、代码易读）
- 最低 SDK 24（Android 7.0）· 目标 SDK 34
- [OSMDroid](https://github.com/osmdroid/osmdroid) —— 地图渲染，无需 API 密钥
- [Room](https://developer.android.com/training/data-storage/room) —— 本地缓存数据库
- 平台 `LocationManager` —— 单次定位读取，本路径不依赖 Google Play Services
- [ML Kit Translation](https://developers.google.com/ml-kit/language/translation) —— 设备端翻译
- [Overpass API](https://overpass-api.de/) —— 免费的 OpenStreetMap 查询端点，被在线搜索按钮使用

## 构建与运行

1. 在 Android Studio 中通过 `File → Open` 打开 `WeRiseApp` 文件夹。
2. 当 Android Studio 询问 Gradle wrapper 的设置时，让它使用
   `gradle/wrapper/gradle-wrapper.properties` 中声明的版本（Gradle 8.4）。如果它提示缺少
   `gradle-wrapper.jar`，请运行 **File → Sync Project with Gradle Files**，或在项目根目录的终端中运行一次
   `gradle wrapper`。
3. 让 Gradle 完成同步。首次同步会下载依赖项，可能耗时几分钟。
4. 在 API 24 及以上的设备或模拟器上运行。

## 向内置列表添加更多服务

编辑 `app/src/main/java/com.riseup.werisedfw/data/SeedData.kt`。每条记录包含稳定的 `id`
、名称、分类、地址、电话、营业时间、经纬度、信仰标记、简介和网站。营业时间格式见 `util/HoursParser.kt`。

## 翻译

设置界面提供语言选择器，列出 Google ML Kit 在设备上支持的约 59 种语言。默认是英语（不翻译）。

当用户首次选择非英语语言时，ML Kit 会下载对应的语言模型（仅一次，约 10–30 MB）。之后翻译完全在设备上进行。翻译结果按
`(语言, 原文)` 缓存在本地 Room 数据库中，因此每个短语只会经过 ML Kit 一次。

如果你想用其他翻译器（例如支持 ML Kit 集合之外的语言），可以实现 `i18n/Translator.kt` 中的 `Translator`
接口，并替换 `TranslatorFactory.get()` 中的构造参数。

## 在线搜索

**在线查找更多** 按钮会向 OpenStreetMap 的 [Overpass API](https://overpass-api.de/) 发起查询，搜索带有
`social_facility=food_bank | soup_kitchen | clothing_bank | shelter`（以及若干相关 amenity
标签）的节点，范围由用户位置和所选半径推导出的边界框决定。查询结果会被映射到相同的 `Service`
数据结构，并合并到本地缓存中，因此会与内置列表一起出现在常规搜索结果与地图上。

Overpass API 是社区维护的免费资源。查询会把基于用户单次定位推导的边界框发送到 `overpass-api.de`
，但位置本身不会保存在设备上。

## 文档

- [USER_GUIDE.zh.md](USER_GUIDE.zh.md) —— 终端用户指南（主屏幕、搜索流程、设置、隐私、故障排查）。
- [README.zh.md](README.zh.md) —— 本文档，面向开发者。
- [LICENSE](LICENSE) —— MIT 许可证文本。

## 许可证

基于 [MIT 许可证](LICENSE) 发布。版权所有 © 2026 Shane W Watson。

你可以自由使用、复制、修改、合并、发布、分发、再许可与销售本软件，前提是保留版权与许可声明完整不变。

## 状态

这是一个个人开源项目，**按"原样"提供**，不附带任何担保，也不承诺提供后续支持、更新或缺陷修复。欢迎提交
Pull Request，但不保证会被合并。

## 致谢

- `SeedData.kt` 中的 DFW 服务提供机构都是真实存在、从事关键工作的组织，欢迎直接支持它们。
- 地图数据 © [OpenStreetMap](https://www.openstreetmap.org/copyright) 贡献者。
- 翻译模型 © Google，依据 ML Kit 条款分发。

