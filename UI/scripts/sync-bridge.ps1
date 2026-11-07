param(
    [string]$BridgeRoot = (Join-Path (Split-Path -Parent $PSScriptRoot) "bridge")
)

$ErrorActionPreference = "Stop"
$UiRoot = Split-Path -Parent $PSScriptRoot
$BridgeAndroid = Join-Path $BridgeRoot "android\app"
$AppMain = Join-Path $UiRoot "app\src\main"

if (-not (Test-Path $BridgeAndroid)) {
    throw "Bridge Android app not found at $BridgeAndroid"
}

Write-Host "Syncing Rust core, UniFFI bindings, models and AARs from $BridgeRoot"

# 1. Native Rust libraries (whisper.cpp + bridge-core) and libc++.
New-Item -ItemType Directory -Force -Path `
    (Join-Path $AppMain "jniLibs\arm64-v8a"), `
    (Join-Path $AppMain "jniLibs\x86_64") | Out-Null
Copy-Item (Join-Path $BridgeAndroid "src\main\jniLibs\arm64-v8a\*") `
    (Join-Path $AppMain "jniLibs\arm64-v8a") -Force
Copy-Item (Join-Path $BridgeAndroid "src\main\jniLibs\x86_64\*") `
    (Join-Path $AppMain "jniLibs\x86_64") -Force

# 2. Models: ggml-base.bin + sherpa-onnx diarization.
New-Item -ItemType Directory -Force -Path `
    (Join-Path $AppMain "assets\diarization") | Out-Null
Copy-Item (Join-Path $BridgeAndroid "src\main\assets\ggml-base.bin") `
    (Join-Path $AppMain "assets\ggml-base.bin") -Force
Copy-Item (Join-Path $BridgeAndroid "src\main\assets\diarization\*.onnx") `
    (Join-Path $AppMain "assets\diarization") -Force

# 3. Generated UniFFI Kotlin bindings.
$BindingDir = Join-Path $AppMain "java\uniffi\bridge_core"
New-Item -ItemType Directory -Force -Path $BindingDir | Out-Null
Copy-Item (Join-Path $BridgeAndroid "src\main\java\uniffi\bridge_core\bridge_core.kt") `
    (Join-Path $BindingDir "bridge_core.kt") -Force

# 4. sherpa-onnx AAR (diarization) and JNA AAR (UniFFI transport).
$Libs = Join-Path $UiRoot "app\libs"
New-Item -ItemType Directory -Force -Path $Libs | Out-Null
Copy-Item (Join-Path $BridgeAndroid "libs\sherpa-onnx-2.0.1.aar") `
    (Join-Path $Libs "sherpa-onnx-2.0.1.aar") -Force

$Jna = Join-Path $Libs "jna-5.17.0.aar"
if (-not (Test-Path $Jna)) {
    Invoke-WebRequest `
        -Uri "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.17.0/jna-5.17.0.aar" `
        -OutFile $Jna -UseBasicParsing
}

Write-Host "Bridge artifacts synced."
Write-Host "Run: .\gradlew.bat assembleDebug"
