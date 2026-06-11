<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Min%20SDK-21-704FFE?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Target%20SDK-34-704FFE?style=for-the-badge"/>
<img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge"/>

# 📅 CalendarApp

**A fully native Android calendar in pure Java — no Flutter, no Kotlin, no third-party view libraries.**

Real device calendar integration · Custom-drawn views · Material Design 3 dark theme

</div>

---

## ✨ Features

| View | Description |
|------|-------------|
| 📆 **Year View** | 3-column mini-month grid for the full year, event dots, tap-to-navigate |
| 🗓️ **Month View** | Custom-drawn month grid with today highlight, event dots per day, day event list |
| 📋 **Week View** | Hourly timeline grid with real event blocks at correct pixel positions + now-line |
| 📄 **Agenda View** | Scrollable date-grouped event list, 90-day look-ahead |
| ➕ **New Event** | Native date/time pickers, all-day toggle, calendar account selector |
| ℹ️ **Event Detail** | Full event info — location, description, organizer, delete & edit |
| ⚙️ **Settings** | Week number toggle, start-week-on preference, delete overdue events |

---

## 🏗️ Architecture

```
CalendarApp/
├── data/
│   ├── CalendarRepository.java   # ContentResolver → CalendarContract.Instances
│   ├── CalendarEvent.java        # Event model
│   └── CalendarInfo.java         # Calendar account model
│
├── ui/
│   ├── MainActivity.java         # Fragment host + permission handling
│   ├── year/
│   │   ├── YearFragment.java
│   │   └── MiniMonthGridView.java   # Custom canvas-drawn mini month
│   ├── month/
│   │   ├── MonthFragment.java
│   │   └── MonthGridView.java       # Custom canvas-drawn month grid
│   ├── week/
│   │   ├── WeekFragment.java
│   │   └── WeekGridView.java        # Custom canvas-drawn timeline
│   ├── agenda/
│   │   └── AgendaFragment.java
│   ├── event/
│   │   ├── EventDetailActivity.java
│   │   └── NewEventActivity.java
│   └── settings/
│       └── SettingsActivity.java
│
├── adapter/
│   ├── YearMonthAdapter.java
│   ├── EventTileAdapter.java
│   └── AgendaAdapter.java
│
└── utils/
    ├── DateUtils.java
    ├── ColorUtils.java
    └── Prefs.java
```

### Key design decisions

- **`CalendarContract.Instances`** — used instead of `Events` table so recurring events are correctly expanded into individual occurrences
- **Background `ExecutorService`** — all `ContentResolver` queries run off the UI thread; results posted back via `runOnUiThread()`
- **Custom `View` subclasses** — `MonthGridView`, `MiniMonthGridView`, `WeekGridView` all draw directly onto `Canvas` for full visual control with zero external dependencies
- **Fragment keep-alive pattern** — all 4 view fragments are added once and shown/hidden (not replaced), preserving scroll position and loaded data across tab switches

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build & Run

```bash
git clone https://github.com/Willykez/CalendarApp.git
cd CalendarApp
```

Open in Android Studio → **Run ▶**

On first launch, grant the **Read/Write Calendar** permission when prompted. Your real device calendar events will then appear across all views.

### Permissions required

```xml
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
```

---

## 📦 Dependencies

All from Android/Google — no third-party view or image libraries:

```gradle
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
implementation 'androidx.fragment:fragment:1.6.2'
```

---

## 🖼️ Screenshots

> _Dark Material 3 theme · Purple accent · East Africa Time zone default_

| Year | Month | Week | Agenda |
|------|-------|------|--------|
| Mini-month 3×4 grid | Day grid + event list | Hourly timeline | Date-grouped list |

---

## 🔧 Customisation

| What | Where |
|------|-------|
| Accent color | `res/values/colors.xml` → `accent_purple` |
| Default look-ahead (Agenda) | `AgendaFragment.java` → `LOOK_AHEAD_DAYS` |
| Hour height (Week view) | `WeekGridView.java` → `HOUR_HEIGHT_DP` |
| Cell height (Month view) | `MonthGridView.java` → `CELL_HEIGHT_DP` |

---

## 📋 Roadmap

- [ ] Search events
- [ ] Repeat / recurrence rule editor
- [ ] Reminder/notification scheduling
- [ ] Widget (home screen month strip)
- [ ] Import/export ICS files
- [ ] Swipe gestures between months/weeks

---

## 👤 Author

**Elia Mwenda (Willykez)**
GitHub: [@Willykez](https://github.com/Willykez)

---

## 📄 License

```
MIT License — free to use, modify and distribute.
```

---

<div align="center">
Built with ❤️ for East African Android users · Pure Java · No Flutter
</div>
