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


UAV (drone, vehicle, boat) ground station video solution, RTSP multi-channel low latency video solution, providing high-performance, easy-to-use and reliable video access capabilities.

[中文看这里](/READMEcn.md)  

# Features

* Very low delay of about 100ms.
* Support multiple video formats, including h264 and h265.
* Support hard decoding and automatically downgrade soft decoding when hard decoding fails.
* Support custom raw buffer streaming and RTSP video source, easy access to multiple video sources.
* Supports taking pictures and recording video streams, and you can save and share your drone perspective at any time.
* Support up to five channels of video decoding and rendering at the same time, you can realize multi-task management.
* Simple API, just a few lines of code, can realize video decoding and rendering, without complex configuration and debugging.
* A complete demo provides detailed instructions and sample codes for quick use and learning.
* Provide multiple decoding and rendering modes such as ffmpeg, OpenGL and MediaCodec.
* Continuous follow-up upgrades, we will continue to improve and optimize our solutions to provide more functions and better experience.

## Download apk
[Download apk](https://github.com/hzw1199/VideoSDK-Android/releases)

## Photos and videos
Click photos to watch video
#### Low latency
[![sample](/art/sample5.jpg)](https://youtu.be/mwLuzPclsQM)
#### Buffer decode with GPU
[![sample](/art/sample6.jpg)](https://youtu.be/mwLuzPclsQM)
#### RTSP played with GPU
[![sample](/art/sample7.jpg)](https://youtu.be/mwLuzPclsQM)
#### Multi channel
[![sample](/art/sample8.jpg)](https://youtu.be/mwLuzPclsQM)

## Proguard
```
-keep class com.wuadam.fflibrary.** { *; }
-keep class com.wuadam.ff.** { *; }
-keep class com.wuadam.medialibrary.** { *; }
-keep class com.wuadam.media.** { *; }
```

## Tip

* For commercial use, please contact me by email contact@zongheng.pro
* If this project helps you, please star me.

## About Me

* [Home Page](https://zongheng.pro/index.html)
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
∑