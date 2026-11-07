# Тихо — Android-приложение

Compose-приложение: импорт или запись разговора, локальная расшифровка, диаризация,
таймкоды и экспорт JSON. Вся обработка идёт на устройстве, без `INTERNET`.

Полное описание архитектуры, моделей и ограничений — в [корневом README](../README.md).

## Что внутри

- `app/src/main/java/ru/mephi/tikho/` — UI, ViewModel и мост к Rust.
- `app/src/main/res/font/` — шрифты тем Bloom и Lumen.
- `app/src/main/assets/` — Whisper `ggml-base.bin` и ONNX-модели диаризации.
- `docs/` — скриншоты, реальный JSON-прогон, лицензии шрифтов.
- `scripts/sync-bridge.ps1` — копирует `.so`, модели и UniFFI-биндинги из `../bridge`.

## Сборка

Нужны Android Studio/JDK 17+ и Android SDK 36. Rust/NDK не требуются.

```powershell
.\gradlew.bat assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Проверки:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

Модель хранится в Git LFS: перед сборкой после клонирования выполните
`git lfs install` и `git lfs pull`, иначе `ggml-base.bin` останется указателем.

## Темы

- Светлая — **Bloom**: бумага, терракота, Onest + Lora.
- Тёмная — **Lumen**: фиолетово-чёрный, латунь, Playfair Display + Geist.

Палитра и типографика — в `ui/theme/Color.kt` и `ui/theme/Type.kt`.
