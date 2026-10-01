# SYNC

"BRO WHEN ARE WE MEETING??????" You never need to text that again, because SYNC shows when your friends, clubs and coworkers are free. Grab lunch, plan a study session or pick an office day, and more!

Built for the **RevenueCat Shipaton 2026** (Next Gen / Student category).

## Features

**Spaces.** Switch between community and workplace spaces, each with its own circles or teams, status, and settings.

**Now.** Set a live status (with a note, place, and duration) so the people in your space know what you're doing. Ghost Mode hides you temporarily, and Focus Hours let you block out distraction-free time.

**Pulses.** Send a quick plan ("coffee in 30?") to a circle with a minimum headcount, a cap, and a decide-by time. Friends tap in or out, and the plan happens only if enough people join.

**Week.** Plan your week with schedule blocks, mark in-office or remote days, and sync your calendar to find overlapping free time.

**Circles / Teams.** Create circles, invite members, join with a code, and discover new circles to request access to.

**Mixer.** Get matched with someone new in your space for a casual meetup. Admins can run a mix on demand from the Admin Console.

**SYNC Pro and Workplace plans.** Monetization is handled through RevenueCat. Pro unlocks extra circles and features, and workplace spaces can upgrade their seat limits.

## Tech Stack

- **Language / UI:** Kotlin, Jetpack Compose, Material 3
- **Local storage:** Room
- **Auth:** Firebase Auth with Google Sign-In (Credential Manager), Firebase App Check
- **AI:** Firebase AI
- **Networking:** Retrofit, OkHttp, Moshi
- **Monetization:** RevenueCat Purchases SDK and RevenueCat Paywalls UI
- **Async:** Kotlin Coroutines and Flow

## Requirements

- Android Studio (latest stable)
- JDK 11+
- A device or emulator running API 24 or higher

## Getting Started

1. **Clone the repo**
```bash
   git clone https://github.com/TheBestProEver/sync-app-shipaton.git
   cd sync-app-shipaton
```

2. **Add your RevenueCat key.** Copy `.env.example` to `.env` and set your public SDK key:
