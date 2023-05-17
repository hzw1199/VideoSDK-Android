<h1 align="center">
  <br>
  <a href="https://videosdk.org/"><img src="https://raw.githubusercontent.com/hzw1199/VideoSDK-Android/master/art/RTS.png" alt="RTS" width="200"></a>
  <br>
  <b>VideoSDK-Android</b>
  <br>
</h1>


[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](http://opensource.org/licenses/MIT)


无人机（车、艇）地面站视频解决方案，RTSP 多路低延迟拉流解决方案，提供高性能、易用、可靠的视频接入能力

## 特性


* 100ms左右低延迟。
* 支持多种视频格式，包括h264和h265。
* 支持硬解码和硬解码失败时自动降级软解码。
* 支持buffer视频裸流和RTSP视频源，多种视频源轻松接入。
* 支持对视频流拍照和录像，可以随时保存和分享你的无人机视角。
* 支持最多五路视频同时解码渲染，实现多任务管理。
* 简易的API，只需几行代码，就可以实现视频的解码和渲染，无需复杂的配置和调试。
* 完备的demo，提供了详细的使用说明和示例代码，快速上手和学习。
* 提供ffmpeg、OpenGL和MediaCodec等多种解码渲染模式。
* 持续的后续升级，我们会不断改进和优化我们的方案，提供更多的功能和更好的体验。

## 下载apk体验
[下载apk](https://github.com/hzw1199/VideoSDK-Android/releases)

## 截图和视频
点击图片看视频
#### 低延迟
[![sample](/art/sample5.jpg)](https://youtu.be/mwLuzPclsQM)
#### Buffer 裸流硬解码
[![sample](/art/sample6.jpg)](https://youtu.be/mwLuzPclsQM)
#### RTSP 拉流硬解码
[![sample](/art/sample7.jpg)](https://youtu.be/mwLuzPclsQM)
#### 多路播放
[![sample](/art/sample8.jpg)](https://youtu.be/mwLuzPclsQM)

## 混淆
```
-keep class com.wuadam.fflibrary.** { *; }
-keep class com.wuadam.ff.** { *; }
-keep class com.wuadam.medialibrary.** { *; }
-keep class com.wuadam.media.** { *; }
```

## Tip

* 商业使用，请联系 contact@zongheng.pro
* 若对你有帮助请加星

## About Me

* [Home Page](https://zongheng.pro)
* [Blog](https://blog.zongheng.pro)

## License

```
The MIT License (MIT)

Copyright (c) 2023 VideoSDK-Android

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```