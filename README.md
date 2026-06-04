# Docktor

Docktor is a lightweight Docker environment for macOS powered by QEMU.

It automatically provisions a Linux VM, installs Docker Engine inside the VM, and configures your local Docker CLI to use it through a dedicated Docker context.

No Docker Desktop required.

The name **Docktor** comes from **Docker + Doctor**.

## Supported Platforms

* macOS Apple Silicon (arm64)
* macOS Intel (x86_64)

## Installation

### Homebrew

```bash
brew tap fullstack-sargis/docktor
brew install docktor
```

Verify the installation:

```bash
docktor --version
```

### From Source

Clone the repository and build Docktor locally:

```bash
git clone https://github.com/fullstack-sargis/docktor.git
cd docktor
./gradlew test
./gradlew installDist
```

The generated CLI script is:

```bash
build/install/docktor/bin/docktor
```

Build a native binary when GraalVM native-image is available:

```bash
./gradlew nativeCompile
```

The generated native binary is:

```bash
build/native/nativeCompile/docktor
```

## Requirements

* macOS host.
* Java 21 to build and run the JVM distribution, or the native `docktor` binary.
* QEMU tools:

    * `qemu-system-aarch64` on Apple Silicon.
    * `qemu-system-x86_64` on Intel Macs.
    * `qemu-img`.
* Docker CLI.
* SSH client.

Install required dependencies with Homebrew:

```bash
brew install qemu
brew install docker
```

Docker Desktop is not required.

## Quick Start

Run a health check:

```bash
docktor doctor
```

Start the VM:

```bash
docktor start
```

Verify Docker connectivity:

```bash
docker run hello-world
```

Check status:

```bash
docktor status
```

Stop the VM:

```bash
docktor stop
```

Example status output:

```text
✅ QEMU VM status
✅ Docker daemon is reachable
✅ Docker context: docktor
```

## Commands

### User Commands

```bash
docktor doctor
docktor start
docktor stop
docktor status
docktor ssh
```

### Advanced Commands

```bash
docktor qemu status
docktor qemu start
docktor qemu stop
docktor qemu ssh
docktor qemu reset

docktor docker check
docktor docker context
```

## How It Works

When started, Docktor:

1. Creates and manages a lightweight Linux VM using QEMU.
2. Boots the VM using cloud-init.
3. Installs and starts Docker Engine inside the VM.
4. Configures a Docker context named `docktor`.
5. Routes local Docker CLI commands to the VM over SSH.

From that point forward, standard Docker commands work normally:

```bash
docker ps
docker images
docker run hello-world
```

## What Docktor Creates

Docktor stores all VM-related files under:

```text
~/.docktor/
```

Important files and directories:

* QEMU working directory:

    * `~/.docktor/qemu/`
* VM disk:

    * `~/.docktor/qemu/docktor.qcow2`
* Cloud-init files:

    * `~/.docktor/qemu/seed/`
    * `~/.docktor/qemu/seed.iso`
    * `~/.docktor/qemu/seed.iso.cdr`
* Logs and runtime files:

    * `~/.docktor/qemu/docktor-qemu.log`
    * `~/.docktor/qemu/docktor-qemu.pid`
    * `~/.docktor/qemu/serial.log`

Docktor also creates:

* Docker context:

    * `docktor`
* SSH config entry:

    * `docktor-vm`

The Docker context uses:

```text
ssh://docktor-vm
```

## Releases

Prebuilt binaries are available on the GitHub Releases page.

Each release includes:

* macOS arm64 native binary
* macOS x64 native binary
* SHA256 checksums

## Troubleshooting

### Docker daemon is not reachable after start

The first boot may take several minutes while cloud-init installs packages and starts Docker inside the VM.

Check progress:

```bash
docktor status
```

Retry after the VM reports that Docker is reachable.

### Docker context is not active

Switch to Docktor's Docker context:

```bash
docker context use docktor
```

You can also recreate and select the context with:

```bash
docktor docker context
```

### SSH host key changed after a VM reset

If the VM was recreated, your SSH client may refuse the connection because the host key changed.

Remove the old key:

```bash
ssh-keygen -R "[127.0.0.1]:2222"
```

Docktor's generated SSH configuration disables strict host-key checking for the `docktor-vm` alias used by the Docker context, but direct SSH commands may still use entries from your local `known_hosts` file.

### QEMU is not installed

Verify QEMU is available:

```bash
qemu-system-aarch64 --version
```

or on Intel Macs:

```bash
qemu-system-x86_64 --version
```

Install QEMU with:

```bash
brew install qemu
```

### Docker CLI is not installed

Verify Docker CLI availability:

```bash
docker --version
```

Install it with:

```bash
brew install docker
```
