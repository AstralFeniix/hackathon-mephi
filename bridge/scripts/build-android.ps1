param(
    [string[]]$Targets = @("arm64-v8a", "x86_64"),
    [int]$ApiLevel = 26
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$RepoRoot = Split-Path -Parent $Root
$Android = Join-Path $RepoRoot "Android"
$JniLibs = Join-Path $Android "app\src\main\jniLibs"
$KotlinOut = Join-Path $Android "app\src\main\java"

$Sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA "Android\Sdk" }
$Ndk = if ($env:ANDROID_NDK_HOME) {
    $env:ANDROID_NDK_HOME
} else {
    (Get-ChildItem (Join-Path $Sdk "ndk") -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
}
$NdkBin = Join-Path $Ndk "toolchains\llvm\prebuilt\windows-x86_64\bin"
$CMakeBin = (Get-ChildItem (Join-Path $Sdk "cmake") -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
$CMakeBin = Join-Path $CMakeBin "bin"
$Ninja = Join-Path $CMakeBin "ninja.exe"

# ABI -> LLVM triple and Rust/CMake env-var suffixes
$Abis = @{
    "arm64-v8a" = @{ Triple = "aarch64-linux-android"; RustSuffix = "aarch64_linux_android" }
    "x86_64"    = @{ Triple = "x86_64-linux-android";  RustSuffix = "x86_64_linux_android" }
}

# Forward slashes: CMake treats backslashes in these values as escape sequences.
function ConvertTo-CMakePath([string]$p) { $p.Replace('\', '/') }

Push-Location $Root
try {
    # 1. Host build: only used to generate Kotlin bindings from the library's metadata
    cargo build -p bridge-core
    if ($LASTEXITCODE -ne 0) { throw "host build failed" }

    cargo run -q -p bridge-core --bin uniffi-bindgen -- `
        generate --library target\debug\bridge_core.dll `
        --language kotlin --out-dir $KotlinOut
    if ($LASTEXITCODE -ne 0) { throw "uniffi binding generation failed" }

    # 2. Cross-compile whisper.cpp + the Rust core for each Android ABI.
    #    whisper-rs-sys runs CMake, so each ABI needs its own compiler and CMake settings.
    foreach ($abi in $Targets) {
        $cfg = $Abis[$abi]
        if (-not $cfg) { throw "unsupported ABI: $abi" }
        $triple = $cfg.Triple

        Set-Item "Env:CC_$triple"  (Join-Path $NdkBin "$triple$ApiLevel-clang.cmd")
        Set-Item "Env:CXX_$triple" (Join-Path $NdkBin "$triple$ApiLevel-clang++.cmd")
        Set-Item "Env:AR_$triple"  (Join-Path $NdkBin "llvm-ar.exe")
        Set-Item "Env:CMAKE_GENERATOR_$($cfg.RustSuffix)" "Ninja"

        $env:CMAKE_SYSTEM_NAME     = "Android"
        $env:CMAKE_SYSTEM_VERSION  = "$ApiLevel"
        $env:CMAKE_ANDROID_ARCH_ABI = $abi
        $env:CMAKE_ANDROID_NDK     = ConvertTo-CMakePath $Ndk
        $env:CMAKE_ANDROID_API     = "$ApiLevel"
        $env:CMAKE_MAKE_PROGRAM    = ConvertTo-CMakePath $Ninja
        $env:ANDROID_NDK_HOME      = $Ndk
        $env:ANDROID_NDK_ROOT      = $Ndk

        cargo ndk -t $abi -o $JniLibs build -p bridge-core --release --features whisper
        if ($LASTEXITCODE -ne 0) { throw "android cross-compilation failed for $abi" }

        # whisper.cpp links against the C++ runtime, which must ship inside the APK
        $cxxShared = Join-Path $NdkBin "..\sysroot\usr\lib\$triple\libc++_shared.so"
        Copy-Item $cxxShared (Join-Path $JniLibs "$abi\libc++_shared.so") -Force
    }
}
finally {
    Pop-Location
}

Write-Host "Done. Native libs in $JniLibs, Kotlin bindings in $KotlinOut"
