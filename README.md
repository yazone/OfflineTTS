# 离线TTS（OfflineTTS）
本项目将基于PaddleSpeech实现离线TTS（Offline TTS Based [PaddleSpeech](https://github.com/PaddlePaddle/PaddleSpeech) for Linux and Android)，限于移动端内存和性能问题，将使用流式方式进行处理。

相关资源：
- 非流式Android版本参考：[TTSAndroid](https://github.com/yt605155624/TTSAndroid)
- 前端英文G2P(OOV)：[g2pE_mobile](https://github.com/yazone/g2pE_mobile)

# 计划
1、实现Android流式TTS（进行中）
- Andorid版本C++库
- Android版本demo

2、实现Linux流式TTS（已完成）

# 当前进度
- 2023-03-16：C++库编译完成，基本流程拉通，集成进Android Demo中，效果（小新同学音色）如下：

（demo apk网盘下载: https://pan.baidu.com/s/1jfxWKBODNY91bmzAHG5Z1Q?pwd=7nes ）

https://user-images.githubusercontent.com/10195479/225497883-13b5a835-0190-47f4-a821-a264b5ceff0e.mp4

