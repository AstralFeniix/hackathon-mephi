# Bridge

Rust-ядро для локальной расшифровки диалогов: **whisper.cpp** (ASR) + **sherpa-onnx** (диаризация на стороне Android). Kotlin вызывает Rust через [UniFFI](https://mozilla.github.io/uniffi-rs/).

> Этот каталог — исходный bridge-проект. Интеграция в основной UI, реальный прогон и инструкции по сборке APK описаны в [`../README.md`](../README.md).
> Диаризация выполняется Android-классом `SpeakerLabeler` на sherpa-onnx: pyannote-сегментация + speaker embeddings. Rust-сторона по-прежнему умеет работать с `SingleSpeaker` как fallback, а `labelSpeakers()` переносит готовые `SpeakerTurn` на реплики.

```
bridge/
├── Cargo.toml                    # Rust workspace
├── crates/bridge-core/           # Rust: ASR + diarization + merge, exported via UniFFI
│   └── src/
│       ├── lib.rs                # uniffi scaffolding + exports
│       ├── engine.rs             # Engine object: transcribe() = ASR + diarization + merge
│       ├── asr.rs                # AsrBackend trait, WhisperBackend (feature `whisper`)
│       ├── diarization.rs        # Diarizer trait, SingleSpeaker placeholder
│       └── error.rs              # BridgeError
├── scripts/build-android.ps1     # host build -> Kotlin bindings -> cargo-ndk -> .so files
└── android/                      # Gradle project (Kotlin, Compose)
    └── app/src/main/java/com/example/bridge/
        ├── MainActivity.kt       # UI: record / stop / show transcript
        └── AudioCapture.kt       # AudioRecord -> 16 kHz f32 samples
```

## Design notes

- **Speech-to-text:** `faster-whisper` is a Python wrapper around CTranslate2 and doesn't run on Android. The equivalent is **whisper.cpp**, which is wrapped by the `whisper-rs` crate (feature `whisper`). Models are GGML files such as `ggml-base.bin` or `ggml-small.bin`.
- **Diarization:** `pyannote` is Python/PyTorch. The Rust side has a `Diarizer` trait, and the current implementation is a placeholder that labels everything as speaker 1. See the roadmap below.
- **Interface:** `Engine` (model), `Session` (live or file transcription with timestamps), and `transcriptToJson()`. Kotlin gets generated bindings for all three.
- **Live:** `Session.pushAudio()` buffers audio and cuts it at pauses (400 ms of silence, after at least 3 s; hard cut at 15 s). Each piece is transcribed once, and the finished segments are returned right away.
- **Files:** `Session.pushAudioFile(path)` decodes mp3 and wav with Symphonia, resamples to 16 kHz mono, and runs the same pipeline as live audio.
- **Speakers:** diarization is still a placeholder, so every segment is `speaker_1` for now. The `Diarizer` trait is where the real pyannote-style model plugs in.

## Output format

`Session.finish()` returns a transcript, and `transcriptToJson()` serializes it as:

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

## Using Bridge from other Android apps

Bridge exposes two intents. The result comes back as JSON.

**1. Transcription intent.** Start `com.example.bridge.action.TRANSCRIBE` for a result, with the audio as the data URI:

```kotlin
val intent = Intent("com.example.bridge.action.TRANSCRIBE")
    .setDataAndType(audioUri, "audio/*")               // content:// URI of an mp3 or wav
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
startActivityForResult(intent, REQ_TRANSCRIBE)

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQ_TRANSCRIBE && resultCode == RESULT_OK) {
        val json = data?.getStringExtra("com.example.bridge.extra.TRANSCRIPT_JSON")
    }
}
```

**2. Share.** Any app that shares audio (`ACTION_SEND`, `audio/*`) can send it to Bridge. The transcript is shown in the app.

Note: the activity is exported, so any installed app can call it. Add a signature-level permission before shipping.

## Prerequisites (Windows)

1. **Rust** with Android targets:
   ```powershell
   rustup target add aarch64-linux-android x86_64-linux-android
   cargo install cargo-ndk
   ```
2. **Android NDK**, installed through Android Studio's SDK Manager. Set `ANDROID_NDK_HOME` to its path, for example `C:\Users\Astral\AppData\Local\Android\Sdk\ndk\<version>`.
3. **cmake and clang**, needed to build whisper.cpp. Both are included with Visual Studio Build Tools.
4. **JDK 17** for Gradle. The machine's default `java` is 1.8, so set `JAVA_HOME` to a JDK 17 install or let Android Studio use its bundled JBR.

## Build and run

```powershell
# 1. Build the Rust core for Android and regenerate Kotlin bindings
.\scripts\build-android.ps1

# 2. Put a Whisper model on the device (the app reads filesDir/ggml-base.bin)
#    Download e.g. ggml-base.bin from https://huggingface.co/ggerganov/whisper.cpp
adb push ggml-base.bin /data/local/tmp/
adb shell run-as com.example.bridge cp /data/local/tmp/ggml-base.bin files/

# 3. Open android/ in Android Studio and run on a device (arm64) or emulator (x86_64)
```

The Gradle wrapper isn't checked in. Open the `android/` folder in Android Studio, which syncs it automatically, or run `gradle wrapper` inside `android/` if you have Gradle installed.

For a quick Rust-only check without the Whisper toolchain, run `cargo check --workspace`. This builds the default feature set, where `Engine::transcribe` returns an error saying the `whisper` feature is missing.

## Roadmap

- [ ] Verify the whisper-rs build on Android (the `whisper` feature needs the NDK toolchain).
- [ ] Diarization: port the pyannote pipeline to ONNX Runtime (`ort` crate). Use `pyannote/segmentation-3.0` for voice activity and speaker-change segmentation, and a speaker-embedding model (WeSpeaker or 3D-Speaker) for clustering (agglomerative or spectral). Implement it as a `Diarizer` impl in `diarization.rs`.
  - Alternative: [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) ships Android builds with Whisper-family ASR and offline speaker diarization. It could replace the whole Rust core.
- [ ] Long recordings: chunk audio with VAD and run ASR in the background. Right now `transcribe` is one blocking call.
- [ ] Model download and management in the app instead of manual `adb push`.
- [ ] Optional: async UniFFI methods so Kotlin can use suspend functions directly.
