# FlipMirror 🖥️📱

> **Universal 16:9 Screen Mirroring Optimizer for All Android Smartphones**  
> *(Works on ANY screen-mirroring capable Android phone: Galaxy Z Flip/Fold, Galaxy S/A, Pixel, Xiaomi, etc.)*  
> *미러링이 가능한 모든 안드로이드 스마트폰을 위한 범용 16:9 외부 화면 최적화 도구 (갤럭시 Z 플립/폴드, 갤럭시 S/A, 픽셀 등 전 기종 지원)*

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku-E65100?style=flat)](https://shizuku.rikka.app/)
[![Latest Release](https://img.shields.io/github/v/release/pk1313/flipmirror?color=success&logo=github)](https://github.com/pk1313/flipmirror/releases/latest)
[![Download APK](https://img.shields.io/badge/Download-APK%20(v1.0.1)-brightgreen?logo=android)](https://github.com/pk1313/flipmirror/releases/download/v1.0.1/FlipMirror-v1.0.1.apk)

---

[English](#english) | [한국어](#한국어)

---

<a name="english"></a>
## English

### 📌 Overview
Almost all modern Android smartphones use ultra-tall aspect ratios ranging from **19.5:9 to 22:9** (e.g., Galaxy Z Flip, Fold, Galaxy S/A series, Google Pixel, Xiaomi, OnePlus).

When mirroring your smartphone to a standard 16:9 TV, external monitor, or projector—whether via a **USB-C to HDMI cable** or **wireless screencast**—the mismatch in aspect ratios creates **massive black letterboxes (pillarboxes)** on the sides, wasting half of your monitor's screen real estate.

> **💡 Is FlipMirror only for the Galaxy Z Flip?**  
> **NO! FlipMirror works universally on ANY Android smartphone that supports screen mirroring.**  
> While the project was inspired by the extreme 22:9 ratio and lack of desktop mode on the Galaxy Z Flip series, the underlying technology uses universal Android window manager commands (`wm size` & `wm density`) via Shizuku. Any Android phone with a tall display can use FlipMirror to eliminate black bars and fill a 16:9 screen!

---

### 📱 Supported Devices & Compatibility

* **Compatible Phones:**
  * **Samsung Galaxy:** Z Flip series, Z Fold series, Galaxy S series (S20 ~ S25), Galaxy Note, Galaxy A series
  * **Google:** Pixel series (Pixel 6, 7, 8, 9, Fold, etc.)
  * **Other Brands:** Xiaomi, OnePlus, Sony Xperia, Motorola, LG, and any Android smartphone running Android 8.0 or later
* **Mirroring Methods Supported:**
  * **Wired Mirroring:** USB-C to HDMI cable, USB-C hub, or DisplayPort Alt Mode
  * **Wireless Mirroring:** Smart View, Miracast, Chromecast, Google Cast
* **System Requirements:**
  * Android 8.0 (Oreo / API 26) or higher
  * [Shizuku](https://shizuku.rikka.app/) (No PC or Root required — runs purely on-device via Wireless Debugging)

---

### ✨ Key Features

* **⚡ Universal 16:9 Ratio Presets:**
  * **16:9 Portrait Monitor Fit (FHD):** `1080 × 1920` (Default 240 DPI) — matches standard monitor height and completely eliminates side black bars.
  * **16:9 Landscape Fullscreen Mode:** `1920 × 1080` (Default 240 DPI) — fills your 16:9 monitor in landscape without distortion.
  * **16:9 QHD High-Res Mode:** `1440 × 2560` (Default 240 DPI) — for 2K/QHD monitors and TVs.
  * **🔄 Instant Restore (Reset):** `wm size reset` & `wm density reset` — instantly restores your phone to its native display ratio and resolution.
* **🎛️ Real-Time DPI Controller (100 ~ 400):**
  * Default density is set to **240 DPI** for desktop-optimized proportions.
  * Adjust UI scaling on-the-fly with a smooth slider, stepper buttons (`[-10]`, `[+10]`), and quick chips (`120`, `160`, `200`, `240★`, `280`, `320`, `360`, `400`).
  * Tap **[Apply DPI to Current Screen]** to preview density adjustments instantly.
* **📲 Quick Settings Tile:**
  * Toggle 16:9 mode on or off straight from your Android notification panel without opening the app.
* **🛡️ Safe Ongoing Notification:**
  * When 16:9 mode is active, an ongoing notification displays the current resolution and provides a `[Restore Native Screen]` action button for quick recovery.
* **🌐 Multi-Language Support:**
  * English (Default) and Korean (한국어) with instant in-app switching (via top bar 🌐 icon).
* **🔌 External Display Detection & Auto-Restore:**
  * Real-time monitoring of USB-C to HDMI cable connections.
  * **Auto-Restore on Disconnect:** Unplugging the HDMI cable automatically reverts your display resolution and DPI back to native proportions and shuts down the service.

---

### 🚀 Getting Started

#### 1. Prerequisites (No Root / No PC needed)
FlipMirror requires elevated shell permissions to change display size. It uses **[Shizuku](https://shizuku.rikka.app/)** over Android's built-in Wireless Debugging:
1. Install **Shizuku** from Google Play Store.
2. Go to phone **Settings > Developer Options > Wireless Debugging** and toggle it **ON**.
3. Open Shizuku, tap **[Pairing]**, and enter the 6-digit code shown in the notification.
4. Tap **[Start]** in Shizuku. *(Needs to be done once per phone restart).*

#### 2. Using FlipMirror
1. Open **FlipMirror** and tap **[Grant]** to allow Shizuku permission.
2. Connect your phone to your monitor/TV using a USB-C to HDMI cable or start wireless screencast.
3. Tap **[16:9 Portrait Monitor Fit]** or **[16:9 Landscape Mode]** — your monitor display will immediately expand to full 16:9!
4. When finished, tap **[🔄 Restore Native Screen]** (or tap the Quick Settings Tile) to revert.

---

### 💡 Tip: Preventing Landscape Shift (Camera Notch Padding)
When rotating into landscape, Android/One UI by default reserves a margin for the front camera cutout, which can shift UI slightly on an external monitor.
* **Fix via Android / One UI Settings:** Go to **Settings > Display > Camera cutout** (or *Full screen apps*), find your app (e.g., Chrome, YouTube), and change it from *Auto* to **Show camera cutout**.
* **Fix via Good Lock (Samsung):** In **Good Lock > MultiStar**, turn on **"Always expand to camera hole"**.

---

<a name="한국어"></a>
## 한국어

### 📌 개요
최신 안드로이드 스마트폰(갤럭시 Z 플립/폴드, 갤럭시 S/A 시리즈, 구글 픽셀, 샤오미 등)은 대부분 **19.5:9 ~ 22:9의 위아래로 긴 와이드 화면 비율**을 채택하고 있습니다.

이 때문에 스마트폰을 C to HDMI 유선 케이블이나 무선 미러링(스마트뷰 등)으로 일반 16:9 모니터, TV, 빔프로젝터에 연결하면, **좌우에 거대한 블랙박스(레터박스)가 생겨 화면의 절반만 쓰게 되는 낭비**가 발생합니다.

> **💡 꼭 갤럭시 Z 플립 전용인가요?**  
> **전혀 아닙니다! FlipMirror는 화면 미러링이 가능한 모든 안드로이드 스마트폰에서 완벽하게 동작합니다.**  
> 화면이 유독 길고(22:9) DeX가 지원되지 않는 갤럭시 Z 플립의 불편함을 해소하고자 시작된 프로젝트이지만, 내부 구동 원리는 안드로이드 표준 화면 관리 명령어(`wm size` & `wm density`)를 사용합니다. 따라서 **기종에 관계없이 긴 화면비를 가진 모든 안드로이드 스마트폰에서 레터박스를 없애고 16:9 모니터를 꽉 채울 수 있습니다.**

---

### 📱 호환 기기 및 환경

* **지원 스마트폰:**
  * **삼성 갤럭시:** 갤럭시 Z 플립 시리즈, Z 폴드 시리즈, 갤럭시 S 시리즈 (S20 ~ S25 등), 갤럭시 Note, 갤럭시 A 시리즈
  * **구글:** 픽셀 시리즈 (Pixel 6, 7, 8, 9, Fold 등)
  * **기타 브랜드:** 샤오미, 원플러스, 소니 엑스페리아, 모토로라, LG 등 Android 8.0 이상 탑재 기기 전체
* **지원 미러링 방식:**
  * **유선 미러링:** C to HDMI 미러링 케이블, USB-C 멀티허브, DP Alt Mode 지원 C타입 기기
  * **무선 미러링:** 삼성 Smart View, Miracast, 크롬캐스트(Google Cast) 등
* **시스템 요구사항:**
  * Android 8.0 (오레오 / API 26) 이상
  * [Shizuku](https://shizuku.rikka.app/) 앱 설치 (PC 연결 및 루팅 불필요, 무선 디버깅으로 스마트폰 단독 실행)

---

### ✨ 주요 기능

* **⚡ 범용 16:9 화면비율 프리셋:**
  * **16:9 세로 모니터 맞춤 (권장 FHD):** `1080 × 1920` (기본 240 DPI) — 모니터 높이에 완벽히 일치시키고 좌우 레터박스 완전 제거.
  * **16:9 가로 전체화면 모드:** `1920 × 1080` (기본 240 DPI) — 가로 방향 16:9 화면 전체 채움.
  * **16:9 QHD 고해상도 모드:** `1440 × 2560` (기본 240 DPI) — QHD(2K) 고해상도 모니터 및 TV 전용.
  * **🔄 순정 화면 복구 (Reset):** 버튼 터치 한 번으로 스마트폰 원래 기본 해상도로 즉시 원복.
* **🎛️ 실시간 DPI 조절 (100 ~ 400):**
  * 모니터 화면 크기에 가장 알맞은 **240 DPI**가 기본값으로 적용됩니다.
  * 슬라이더, `[-10]` / `[+10]` 미세 조절 버튼, 퀵 칩(`120`, `160`, `200`, `240★`, `280`, `320`, `360`, `400`)으로 손쉽게 UI 배율 조절 가능.
  * **[현재 화면에 DPI 즉시 적용]** 버튼으로 화면 비율 변경 없이 DPI만 실시간 튜닝.
* **📲 상단바 빠른 설정(타일) 지원:**
  * 앱을 매번 실행하지 않아도 상단 알림창을 내려 **[미러링 16:9]** 타일 터치 한 번으로 모드 켜기/끄기 가능.
* **🛡️ 안전 복구 상주 알림:**
  * 16:9 모드 실행 중 알림창에 `[🔄 기본 화면 복구]` 버튼이 상주하여 언제든 안전하게 원래 화면으로 복구 가능.
* **🌐 다국어 지원:**
  * 영어(기본값) 및 한국어 지원 (상단바 🌐 아이콘으로 원터치 전환).
* **🔌 외부 모니터 연결 감지 & 케이블 분리 시 자동 복구:**
  * C to HDMI 케이블 연결 상태 실시간 감지.
  * **분리 시 자동 복구:** 외부 모니터 케이블을 빼면 시스템이 즉시 감지하여 스마트폰 화면을 원래 기본 비율 및 해상도로 자동 원복하고 서비스를 종료합니다 (앱 설정에서 켜기/끄기 가능).

---

### 🚀 사용 방법

1. **사전 준비 (루팅 X, PC 불필요):**
   * Google Play 스토어에서 **[Shizuku]** 앱을 설치합니다.
   * 스마트폰 `설정 > 개발자 옵션 > 무선 디버깅`을 켭니다.
   * Shizuku 앱을 열고 무선 디버깅 페어링 코드를 입력하여 서비스를 시작합니다. *(스마트폰 재부팅 시에만 1회 필요)*
2. **FlipMirror 실행:**
   * 앱 실행 후 **[Shizuku 권한 허용]**을 누릅니다.
   * C to HDMI 케이블로 모니터와 스마트폰을 연결하거나 무선 미러링을 켭니다.
   * **[16:9 세로 모니터 맞춤]** 또는 **[16:9 가로 모드]**를 누르면 모니터에 꽉 찬 화면이 출력됩니다.
   * 사용이 끝나면 **[🔄 기본 화면으로 복구]**를 누릅니다.

---

### 💡 팁: 가로 모드 시 카메라 펀치홀 여백(쏠림) 제거 방법
가로 모드로 돌렸을 때 전면 카메라 보호 마진으로 인해 모니터 UI가 한쪽으로 치우쳐 보일 경우:
* **One UI 설정:** `설정 > 디스플레이 > 카메라 영역`(또는 전체 화면 비율로 사용할 앱)에서 해당 앱(Chrome, YouTube 등)을 **[카메라 영역 표시]**로 변경합니다.
* **Good Lock 설정 (삼성):** Galaxy Store에서 **Good Lock > MultiStar** 설치 후 **[웬만하면 카메라 홀까지 넓혀드림]**을 켜면 모든 앱이 모니터 끝까지 꽉 차게 출력됩니다.

---

### 🛠️ Tech Stack
* **Language:** Kotlin 2.2.10
* **UI:** Jetpack Compose (Material 3)
* **Privileged Execution:** Shizuku API (`dev.rikka.shizuku:api:13.1.5`)
* **Architecture:** StateFlow, Coroutines, TileService, BroadcastReceiver

---

### 📄 License
This project is open-source under the [MIT License](LICENSE).
