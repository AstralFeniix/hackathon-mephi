# Bridge

Rust-ядро для локальной расшифровки диалогов: **whisper.cpp** (ASR) + **sherpa-onnx** (диаризация на стороне Android). Kotlin вызывает Rust через [UniFFI](https://mozilla.github.io/uniffi-rs/).

> Этот каталог — исходный bridge-проект. Интеграция в Android-приложение, реальный прогон и инструкции по сборке APK описаны в корневом [`README.md`](../README.md).
> Диаризация выполняется Android-классом `SpeakerLabeler` на sherpa-onnx: pyannote-сегментация + speaker embeddings. Rust-сторона по-прежнему умеет работать с `SingleSpeaker` как fallback, а `labelSpeakers()` переносит готовые `SpeakerTurn` на реплики.

```
bridge/
├── Cargo.toml                    # Rust workspace
├── crates/bridge-core/           # Rust: ASR + диаризация + слияние, экспортируется через UniFFI
│   └── src/
│       ├── lib.rs                # uniffi scaffolding + экспорты
│       ├── engine.rs             # объект Engine: transcribe() = ASR + диаризация + слияние
│       ├── asr.rs                # трейт AsrBackend, WhisperBackend (фича `whisper`)
│       ├── diarization.rs        # трейт Diarizer, заглушка SingleSpeaker
│       └── error.rs              # BridgeError
└── scripts/
    ├── build-android.ps1         # сборка на хосте -> Kotlin-биндинги -> cargo-ndk -> .so в ../Android
    └── fetch-diarization.ps1     # sherpa-onnx AAR + ONNX-модели -> ../Android
```

Gradle-приложение находится в [`../Android`](../Android). Android-проекта в этом каталоге больше нет.

## Заметки по архитектуре

- **Распознавание речи:** `faster-whisper` — это Python-обёртка над CTranslate2, и на Android она не работает. Его аналог — **whisper.cpp**, обёрнутый крейтом `whisper-rs` (фича `whisper`). Модели — GGML-файлы, например `ggml-base.bin` или `ggml-small.bin`.
- **Диаризация:** `pyannote` написан на Python/PyTorch, поэтому сегментация и speaker embeddings выполняются на Android через sherpa-onnx (`SpeakerLabeler`). На стороне Rust есть трейт `Diarizer`; `SingleSpeaker` — его заглушка, используемая для live-фрагментов. Подробности — в дорожной карте ниже.
- **Интерфейс:** `Engine` (модель), `Session` (live- или файловая расшифровка с таймкодами) и `transcriptToJson()`. Kotlin получает сгенерированные биндинги для всех трёх.
- **Live:** `Session.pushAudio()` буферизует аудио и нарезает его по паузам (400 мс тишины, но не раньше чем через 3 с; жёсткая нарезка на 15 с). Каждый фрагмент распознаётся один раз, готовые сегменты возвращаются сразу.
- **Файлы:** `Session.pushAudioFile(path)` декодирует mp3 и wav через Symphonia, ресемплирует в 16 кГц mono и запускает тот же конвейер, что и для live-аудио.
- **Говорящие:** `label_speakers()` объединяет близкие реплики одного говорящего, вливает вставки короче 300 мс в окружающую реплику и разбивает каждый сегмент Whisper в местах смены говорящего, используя таймкоды слов из whisper.cpp. Сегменты без таймкодов слов получают одного доминирующего говорящего.

## Формат вывода

`Session.finish()` возвращает транскрипт, а `transcriptToJson()` сериализует его так:

```json
{
  "transcript": [
    {
      "speaker_id": "speaker_1",
      "start_ms": 1250,
      "end_ms": 4380,
      "text": "Добрый день!"
    }
  ]
}
```

## Требования (Windows)

1. **Rust** с Android-таргетами:
   ```powershell
   rustup target add aarch64-linux-android x86_64-linux-android
   cargo install cargo-ndk
   ```
2. **Android NDK**, установленный через SDK Manager в Android Studio. Укажите `ANDROID_NDK_HOME` на его путь, например `C:\Users\Astral\AppData\Local\Android\Sdk\ndk\<version>`.
3. **cmake и clang**, необходимые для сборки whisper.cpp. Оба входят в Visual Studio Build Tools.
4. **JDK 17** для Gradle. Системный `java` на машине — 1.8, поэтому задайте `JAVA_HOME` на установленный JDK 17 или дайте Android Studio использовать встроенный JBR.

## Сборка и запуск

```powershell
# 1. Скачать AAR sherpa-onnx и модели диаризации в ../Android (один раз)
.\scripts\fetch-diarization.ps1

# 2. Собрать Rust-ядро для Android и перегенерировать Kotlin-биндинги в ../Android
.\scripts\build-android.ps1

# 3. Открыть ../Android в Android Studio и запустить на устройстве (arm64) или эмуляторе (x86_64)
```

Gradle wrapper лежит в `../Android`; модель Whisper поставляется как `../Android/app/src/main/assets/ggml-base.bin`.

Для быстрой проверки только Rust без toolchain для Whisper выполните `cargo check --workspace`. Она собирает набор фич по умолчанию, в котором `Engine::transcribe` возвращает ошибку о том, что фича `whisper` отсутствует.

## Дорожная карта

- [ ] Проверить сборку whisper-rs на Android (для фичи `whisper` нужен NDK toolchain).
- [ ] Диаризация: перенести конвейер pyannote на ONNX Runtime (крейт `ort`). Использовать `pyannote/segmentation-3.0` для определения голосовой активности и сегментации смены говорящего, а модель speaker embeddings (WeSpeaker или 3D-Speaker) — для кластеризации (агломеративной или спектральной). Реализовать как имплементацию `Diarizer` в `diarization.rs`.
  - Альтернатива: [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) поставляет Android-сборки с ASR семейства Whisper и офлайн-диаризацией говорящих. Он может заменить всё Rust-ядро.
- [ ] Долгие записи: нарезать аудио через VAD и запускать ASR в фоне. Сейчас `transcribe` — один блокирующий вызов.
- [ ] Загрузка и управление моделями в приложении вместо ручного `adb push`.
- [ ] Опционально: асинхронные методы UniFFI, чтобы Kotlin мог напрямую использовать suspend-функции.
