# Anchor - Zero-Friction AI To-Do & Notes

Anchor is a modern Android productivity application built with Jetpack Compose, Room Database, and Firebase AI Logic (Gemini API). It combines frictionless capture, intelligent categorization, automatic duplicate detection, daily morning/evening reviews, and cognitive focus assistance.

## Features

- **Zero-Friction Quick Capture**: Instant task and note creation with smart parsing and natural language scheduling.
- **Intelligent AI Categorization**: Powered by Gemini AI via Firebase AI Logic to automatically categorize items into Work, Personal, Urgent, Ideas, and Daily habits.
- **Duplicate & Similar Task Guard**: Detects redundant captures and helps merge or triage them.
- **Daily Rhythm**:
  - **Morning Brief**: Highlights key anchors and focus goals for the day.
  - **Evening Review**: Reflect on completed tasks, celebrate wins, and reschedule unfinished items.
- **Smart Views**:
  - **Today**: High-priority focal items and active anchors.
  - **Inbox**: Unsorted captures ready for fast triage.
  - **Upcoming**: Scheduled deadlines and recurring commitments.
  - **Notes**: Free-form markdown notes with tag indexing.
  - **Done**: Archive of completed tasks with completion analytics.
- **Focus & Snooze Coach**: Gentle nudges when tasks are repeatedly postponed to help break them down into actionable micro-steps.
- **Offline First**: All data is securely stored locally using Room Database with Flow reactivity.

## Architecture

- **UI**: 100% Jetpack Compose with Material Design 3, custom haptics, and dynamic theming.
- **State Management**: Android Jetpack ViewModel with Kotlin Coroutines and StateFlow.
- **Persistence**: Room Database (`AnchorDatabase`, `AnchorDao`).
- **AI Integration**: Firebase AI Logic (`GeminiService`) with schema validation and fallback heuristics.
- **Notifications & Alarms**: Exact alarm scheduling with AlarmManager and boot completion receiver.

## Building the Project

Run with Gradle:
```bash
gradle assembleDebug
```

Run tests:
```bash
gradle :app:testDebugUnitTest
```
