$repoRoot = Split-Path -Parent $PSScriptRoot
$androidSdk = Join-Path $repoRoot ".android-sdk"
$androidUserHome = Join-Path $repoRoot ".android"
$gradleUserHome = Join-Path $repoRoot ".gradle"

if (-not (Test-Path -LiteralPath $androidSdk)) {
    throw "Android SDK not found at $androidSdk. Run the Milestone 0 setup first."
}

New-Item -ItemType Directory -Force -Path $androidUserHome | Out-Null
New-Item -ItemType Directory -Force -Path $gradleUserHome | Out-Null

$env:ANDROID_HOME = $androidSdk
$env:ANDROID_SDK_ROOT = $androidSdk
$env:ANDROID_USER_HOME = $androidUserHome
$env:GRADLE_USER_HOME = $gradleUserHome

$platformTools = Join-Path $androidSdk "platform-tools"
$cmdlineTools = Join-Path $androidSdk "cmdline-tools\latest\bin"
$env:PATH = "$platformTools;$cmdlineTools;$env:PATH"

Write-Host "ANDROID_HOME=$env:ANDROID_HOME"
Write-Host "ANDROID_USER_HOME=$env:ANDROID_USER_HOME"
Write-Host "GRADLE_USER_HOME=$env:GRADLE_USER_HOME"
