# RealTimeStream-Android
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](http://opensource.org/licenses/MIT)


无人机（车、艇）地面站视频解决方案，为地面站提供高性能、易用、可靠的视频接入能力

## 特性


* 100ms左右低延迟，实时观看无人机拍摄的画面，不会错过任何精彩瞬间。
* 支持多种视频格式，包括h264和h265，无需担心视频兼容性问题。
* 支持硬解码和硬解码失败时自动降级软解码，可以根据你的设备性能和需求选择合适的解码方式，节省电量。
* 支持buffer视频裸流和RTSP视频源，多种视频源轻松接入。
* 支持对视频流拍照和录像，可以随时保存和分享你的无人机视角。
* 支持最多五路视频同时解码渲染，可以同时观看多个无人机的画面，实现多任务管理。
* 简易的API，只需几行代码，就可以实现视频的解码和渲染，无需复杂的配置和调试。
* 完备的demo，提供了详细的使用说明和示例代码，快速上手和学习。
* 提供ffmpeg、OpenGL和MediaCodec等多种解码渲染模式，可以根据你的视频源和设备性能选择最合适的模式。
* 持续的后续升级，我们会不断改进和优化我们的方案，提供更多的功能和更好的体验。

## 截图和视频
点击截图看视频
#### Buffr 裸流解码
[![sample](/art/sample.jpg)](https://youtu.be/G3Jrwr5MDks)
#### Buffr 裸流解码提供多种模式
[![sample](/art/sample1.jpg)](https://youtu.be/G3Jrwr5MDks)
#### RTSP 拉流
[![sample](/art/sample2.jpg)](https://youtu.be/G3Jrwr5MDks)
#### RTSP 拉流提供多种模式
[![sample](/art/sample3.jpg)](https://youtu.be/G3Jrwr5MDks)
#### 多路
[![sample](/art/sample4.jpg)](https://youtu.be/G3Jrwr5MDks)

## 下载体验
[下载apk](https://github.com/hzw1199/RealTimeStream-Android/releases)

## 混淆
无需配置混淆规则

## Tip

* 若对你有帮助请加星

## About Me

* [Home Page](https://zongheng.pro)
* [Blog](https://blog.zongheng.pro)

## License

```
The MIT License (MIT)

Copyright (c) 2023 RealTimeStream-Android

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