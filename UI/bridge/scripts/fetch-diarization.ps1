$ErrorActionPreference = "Stop"

# Downloads the sherpa-onnx Android library and the speaker diarization models.
# Versions: https://github.com/k2-fsa/sherpa-onnx/releases

$Root = Split-Path -Parent $PSScriptRoot
$Libs = Join-Path $Root "android\app\libs"
$Assets = Join-Path $Root "android\app\src\main\assets\diarization"
$Release = "https://github.com/k2-fsa/sherpa-onnx/releases/download"

New-Item -ItemType Directory -Force -Path $Libs, $Assets | Out-Null

$aar = Join-Path $Libs "sherpa-onnx-2.0.1.aar"
if (-not (Test-Path $aar)) {
    Invoke-WebRequest -Uri "$Release/v2.0.1/sherpa-onnx-2.0.1.aar" -OutFile $aar
}

$segmentation = Join-Path $Assets "segmentation.onnx"
if (-not (Test-Path $segmentation)) {
    $archive = Join-Path $env:TEMP "sherpa-pyannote-segmentation-3-0.tar.bz2"
    Invoke-WebRequest -Uri "$Release/speaker-segmentation-models/sherpa-onnx-pyannote-segmentation-3-0.tar.bz2" -OutFile $archive
    tar -xjf $archive -C $env:TEMP
    if ($LASTEXITCODE -ne 0) { throw "failed to extract segmentation model" }
    Copy-Item (Join-Path $env:TEMP "sherpa-onnx-pyannote-segmentation-3-0\model.onnx") $segmentation -Force
}

$embedding = Join-Path $Assets "embedding.onnx"
if (-not (Test-Path $embedding)) {
    Invoke-WebRequest -Uri "$Release/speaker-recongition-models/3dspeaker_speech_campplus_sv_zh-cn_16k-common.onnx" -OutFile $embedding
}

Write-Host "Diarization files ready in $Libs and $Assets"
