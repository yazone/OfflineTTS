# 语音合成 Java API Demo 使用指南

在 Android 上实现语音合成功能，此 Demo 有很好的的易用性和开放性，如在 Demo 中跑自己训练好的模型等。

本文主要介绍语音合成 Demo 运行方法。


## 如何运行语音合成 Demo

首先需要下载android_tts_models_230316.tar.gz 解压assets和jniLibs到app/src/main目录下。

下载链接: https://pan.baidu.com/s/1jfxWKBODNY91bmzAHG5Z1Q?pwd=7nes 提取码: 7nes

### 环境准备

1. 在本地环境安装好 Android Studio 工具，详细安装方法请见 [Android Stuido 官网](https://developer.android.com/studio)。
2. 准备一部 Android 手机，并开启 USB 调试模式。开启方法: `手机设置 -> 查找开发者选项 -> 打开开发者选项和 USB 调试模式`。

**注意**：
> 如果您的 Android Studio 尚未配置 NDK ，请根据 Android Studio 用户指南中的[安装及配置 NDK 和 CMake ](https://developer.android.com/studio/projects/install-ndk)内容，预先配置好 NDK 。您可以选择最新的 NDK 版本，或者使用 Paddle Lite 预测库版本一样的 NDK。

### 部署步骤

1. 用 Android Studio 打开 TTSAndroid 工程。
2. 手机连接电脑，打开 USB 调试和文件传输模式，并在 Android Studio 上连接自己的手机设备（手机需要开启允许从 USB 安装软件权限）。

**注意：**
>1. 如果您在导入项目、编译或者运行过程中遇到 NDK 配置错误的提示，请打开 `File > Project Structure > SDK Location`，修改 `Andriod NDK location` 为您本机配置的 NDK 所在路径。
>2. 如果您是通过 Andriod Studio 的 SDK Tools 下载的 NDK (见本章节"环境准备")，可以直接点击下拉框选择默认路径。
>3. 还有一种 NDK 配置方法，你可以在 `AndroidTTS/local.properties` 文件中手动添加 NDK 路径配置 `nkd.dir=/root/android-ndk-r20b`
>4. 如果以上步骤仍旧无法解决 NDK 配置错误，请尝试根据 Andriod Studio 官方文档中的[更新 Android Gradle 插件](https://developer.android.com/studio/releases/gradle-plugin?hl=zh-cn#updating-plugin)章节，尝试更新 Android Gradle plugin 版本。

3. 点击 Run 按钮，自动编译 APP 并安装到手机。(该过程会自动下载 Paddle Lite 预测库和模型，需要联网)
