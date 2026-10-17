# FlipMirror 🖥️📱

> **16:9 Screen Mirroring Optimizer for Samsung Galaxy Z Flip**  
> *갤럭시 Z 플립 전용 16:9 외부 모니터 미러링 최적화 앱*

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku-E65100?style=flat)](https://shizuku.rikka.app/)

---

[English](#english) | [한국어](#한국어)

---

<a name="english"></a>
## English

### 📌 Overview
The Samsung Galaxy Z Flip series (Flip 5, etc.) features an ultra-tall **22:9 aspect ratio (1080 × 2640)** and does not officially support Samsung DeX. When connecting your Flip to an external 16:9 TV or monitor via a USB-C to HDMI cable, standard screen mirroring results in massive black letterboxes/pillarboxes.

**FlipMirror** solves this problem by dynamically overriding the display resolution to **16:9 (1080 × 1920 or 1920 × 1080)** and tuning the DPI using the **Shizuku API**—**completely on-device without needing a computer connection**.

---

### ✨ Key Features

* **⚡ One-Tap 16:9 Ratio Presets:**
  * **16:9 Portrait Monitor Fit (FHD):** `1080 × 1920` (Default 240 DPI) — matches monitor height and eliminates black side bars.
  * **16:9 Landscape Fullscreen Mode:** `1920 × 1080` (Default 240 DPI) — fills your 16:9 monitor in landscape.
  * **16:9 QHD High-Res Mode:** `1440 × 2560` (Default 240 DPI) — for 2K/QHD displays.
  * **🔄 Instant Restore (Reset):** `wm size reset` & `wm density reset` — instantly restores your phone to its native 22:9 (1080 × 2640) display.
* **🎛️ Real-Time DPI Controller (100 ~ 400):**
  * Default density is set to **240 DPI** for desktop-like proportions.
  * Adjust UI size on-the-fly with a slider, stepper buttons (`[-10]`, `[+10]`), and quick chips (`120`, `160`, `200`, `240★`, `280`, `320`, `360`, `400`).
  * Tap **[Apply DPI to Current Screen]** to preview changes immediately.
* **📲 Quick Settings Tile:**
  * Toggle 16:9 mode on or off straight from your Samsung Quick Panel / notification shade without launching the app.
* **🛡️ Safe Ongoing Notification:**
  * When 16:9 mode is active, an ongoing notification displays the active resolution and provides a `[Restore Native Screen]` action button for quick recovery.
* **🌐 Multi-Language Support:**
  * English (Default) and Korean (한국어) with instant in-app switching (via top bar 🌐 icon).
* **🔌 External Display Detection:**
  * Real-time monitoring of USB-C to HDMI connections.

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
2. Connect your Galaxy Z Flip to your monitor using a USB-C to HDMI cable or hub.
3. Tap **[16:9 Portrait Monitor Fit]** or **[16:9 Landscape Mode]** — your monitor display will immediately expand to full 16:9!
4. When finished, tap **[🔄 Restore Flip Native Screen]** (or tap the Quick Settings Tile) to revert.

---

### 💡 Tip: Preventing Landscape Shift (Camera Notch Padding)
When rotating into landscape, One UI by default reserves a 94px margin for the front camera cutout, which can shift UI slightly right on an external monitor.
* **Fix via One UI Settings:** Go to **Settings > Display > Camera cutout** (or *Full screen apps*), find your app (e.g., Chrome, YouTube), and change it from *Auto* to **Show camera cutout**.
* **Fix via Good Lock:** In **Good Lock > MultiStar > I ♡ Galaxy Foldable**, turn on **"Always expand to camera hole"**.

---

<a name="한국어"></a>
## 한국어

### 📌 개요
갤럭시 Z 플립 시리즈(플립5 등)는 화면 비율이 **22:9 (1080 × 2640)**로 길쭉하며, 삼성 DeX가 공식적으로 탑재되어 있지 않습니다. C to HDMI 케이블로 일반 16:9 모니터나 TV에 유선 미러링을 연결하면 좌우에 거대한 블랙박스(레터박스)가 생겨 화면을 제대로 활용하기 어렵습니다.

**FlipMirror**는 **Shizuku API**를 활용하여 **PC 연결 없이 스마트폰 자체에서 해상도를 16:9(`1080 × 1920` 또는 `1920 × 1080`) 및 최적 DPI로 전환**해 모니터에 꽉 찬 화면을 제공합니다.

---

### ✨ 주요 기능

* **⚡ 원터치 16:9 화면비율 프리셋:**
  * **16:9 세로 모니터 맞춤 (권장 FHD):** `1080 × 1920` (기본 240 DPI) — 모니터 높이에 맞추고 좌우 여백을 제거.
  * **16:9 가로 전체화면 모드:** `1920 × 1080` (기본 240 DPI) — 가로 방향 16:9 화면 전체 채움.
  * **16:9 QHD 고해상도 모드:** `1440 × 2560` (기본 240 DPI) — QHD 고해상도 모니터 전용.
  * **🔄 순정 화면 복구 (Reset):** 버튼 터치 한 번으로 플립5 기본 22:9(`1080 × 2640`) 화면으로 즉시 원복.
* **🎛️ 실시간 DPI 조절 (100 ~ 400):**
  * 모니터 작업에 최적화된 **240 DPI**가 디폴트로 적용됩니다.
  * 슬라이더, `[-10]` / `[+10]` 미세 조절 버튼, 퀵 칩(`120`, `160`, `200`, `240★`, `280`, `320`, `360`, `400`)으로 자유롭게 크기 변경 가능.
  * **[현재 화면에 DPI 즉시 적용]** 버튼으로 화면 비율 재설정 없이 DPI만 실시간 튜닝.
* **📲 상단바 빠른 설정(타일) 지원:**
  * 앱을 켜지 않고도 알림창을 내려 **[미러링 16:9]** 타일 터치 한 번으로 토글 가능.
* **🛡️ 안전 복구 상주 알림:**
  * 16:9 모드 실행 중 알림창에 `[🔄 기본 화면 복구]` 버튼이 상주하여 언제든 안전하게 원복 가능.
* **🌐 다국어 지원:**
  * 영어(기본값) 및 한국어 지원 (상단바 🌐 아이콘으로 원터치 전환).
* **🔌 외부 모니터 연결 감지:**
  * C to HDMI 케이블 연결 상태 실시간 감지.

---

### 🚀 사용 방법

1. **사전 준비 (루팅 X, PC 불필요):**
   * Play 스토어에서 **[Shizuku]** 앱을 설치합니다.
   * 플립5 `설정 > 개발자 옵션 > 무선 디버깅`을 켭니다.
   * Shizuku 앱을 열고 무선 디버깅 페어링 코드를 입력하여 서비스를 시작합니다. *(폰 재부팅 시에만 1회 필요)*
2. **FlipMirror 실행:**
   * 앱 실행 후 **[Shizuku 권한 허용]**을 누릅니다.
   * C to HDMI 케이블로 모니터와 플립을 연결합니다.
   * **[16:9 세로 모니터 맞춤]** 또는 **[16:9 가로 모드]**를 누르면 모니터에 꽉 찬 화면이 출력됩니다.
   * 사용이 끝나면 **[🔄 플립 기본 화면(22:9)으로 복구]**를 누릅니다.

---

### 💡 팁: 가로 모드 시 카메라 펀치홀 여백(쏠림) 제거 방법
가로 모드로 돌렸을 때 왼쪽 카메라 펀치홀 보호 마진(94px)으로 인해 모니터 UI가 오른쪽으로 치우쳐 보일 경우:
* **One UI 설정:** `설정 > 디스플레이 > 카메라 영역`(또는 전체 화면 비율로 사용할 앱)에서 해당 앱(Chrome, YouTube 등)을 **[카메라 영역 표시]**로 변경합니다.
* **Good Lock 설정:** Galaxy Store에서 **Good Lock > MultiStar** 설치 후 **[웬만하면 카메라 홀까지 넓혀드림]**을 켜면 모든 앱이 모니터 끝까지 꽉 차게 출력됩니다.

---

### 🛠️ Tech Stack
* **Language:** Kotlin 2.2.10
* **UI:** Jetpack Compose (Material 3)
* **Privileged Execution:** Shizuku API (`dev.rikka.shizuku:api:13.1.5`)
* **Architecture:** StateFlow, Coroutines, TileService, BroadcastReceiver

---

### 📄 License
This project is open-source. Choose the license that fits your needs (e.g., [MIT License](https://opensource.org/licenses/MIT)).
