# Releasing

Docktor publishes macOS native binaries from GitHub Actions when a version tag is pushed.

After the GitHub Release is created, the workflow also updates the Homebrew formula in:

```text
fullstack-sargis/homebrew-docktor
```

## Create A Release

```bash
git tag v0.1.0
git push origin v0.1.0
```

The release workflow builds and publishes:

```text
docktor-v0.1.0-macos-arm64.tar.gz
docktor-v0.1.0-macos-arm64.sha256
docktor-v0.1.0-macos-x64.tar.gz
docktor-v0.1.0-macos-x64.sha256
```

Then it updates the Homebrew formula with:

- release version
- arm64 archive URL
- arm64 SHA256
- x64 archive URL
- x64 SHA256

You can also run the workflow manually from the GitHub Actions UI and provide an existing version tag, such as `v0.1.0`.

## Homebrew Installation

After the workflow completes, users can install the released version with:

```bash
brew tap fullstack-sargis/docktor
brew install docktor
```

To upgrade an existing installation:

```bash
brew update
brew upgrade docktor
```

## Download And Run Manually

Download the archive for your Mac from the GitHub Release page, then run:

```bash
tar -xzf docktor-v0.1.0-macos-arm64.tar.gz
chmod +x docktor
./docktor doctor
```

Use `docktor-v0.1.0-macos-arm64.tar.gz` on Apple Silicon Macs.

Use `docktor-v0.1.0-macos-x64.tar.gz` on Intel Macs.

To determine your Mac architecture:

```bash
uname -m
```

Expected values:

```text
arm64  -> Apple Silicon
x86_64 -> Intel Mac
```

## Required Secret

The release workflow requires this repository secret:

```text
HOMEBREW_TAP_TOKEN
```

It must have write access to:

```text
fullstack-sargis/homebrew-docktor
```