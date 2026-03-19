# Twenty ·³

A mobile app (Android + Web) implementing the **20-20-20 Rule** to reduce digital eye strain.

## The Rule

Every **20 minutes** of screen time, take a **20-second break** looking at something **20 feet away**.

## Features

- **Session Timer** — Track your screen time with a live session timer
- **Break Reminders** — Automatic prompts every 20 minutes to rest your eyes
- **20-Second Break Countdown** — Animated countdown with a confirmation prompt
- **Break Compliance Tracking** — Track breaks taken vs. skipped with compliance rate
- **Session Summaries** — View session stats and share as an image
- **Recap Dashboard** — Weekly/monthly/yearly analytics with streak calendar
- **Live Notification** — Timer and break status shown in notification bar (Android)
- **Foreground Service** — Timer keeps running when app is backgrounded (Android)
- **Sound & Haptic Feedback** — Optional audio cues and vibration
- **Export Sessions** — Download your session history as JSON

## Project Structure

```
twenty-android/          Native Android app (Kotlin + Jetpack Compose)
src/                     React web app (Vite)
```

## Android App

### Build

```bash
cd twenty-android
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK (signed)
```

### Requirements

- JDK 17+ (JDK 25 causes Gradle issues)
- Android SDK with API level 35
- Android Gradle Plugin 8.x

### Key Files

- `app/src/main/kotlin/com/twenty/app/domain/SessionViewModel.kt` — Core session state management
- `app/src/main/kotlin/com/twenty/app/domain/TimerViewModel.kt` — Timer and break interval logic
- `app/src/main/kotlin/com/twenty/app/ui/screens/HomeScreen.kt` — Main UI + break overlay
- `app/src/main/kotlin/com/twenty/app/platform/NotificationHelper.kt` — Live notification system
- `app/src/main/kotlin/com/twenty/app/platform/TimerForegroundService.kt` — Foreground service

### Break Flow

1. Session starts → 20-minute countdown begins
2. After 20 min → 20-second break countdown
3. Countdown ends → "Did you rest your eyes?" prompt
4. "Yes, I did" → break counted as taken, session continues
5. "No, I didn't" → dialog: "Continue anyway" or "End Session"

## Web App

### Build

```bash
npm install
npm run dev          # Development
npm run build        # Production build
```

### Requirements

- Node.js 18+
- npm 9+

## Version

0.0.1
