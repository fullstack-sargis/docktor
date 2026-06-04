# Docktor

Docktor is a Java 21 CLI prototype for diagnosing and eventually managing a local Docker development VM on macOS through Lima and QEMU.

Docktor means Docker + Doctor.

## Commands

```bash
docktor doctor
docktor status
docktor docker check
```

The following commands are wired as first-milestone placeholders:

```bash
docktor start
docktor stop
docktor ssh
```

## Build

```bash
gradle test
gradle installDist
```

After `installDist`, the CLI script is generated at:

```bash
build/install/docktor/bin/docktor
```

## Scope

The first milestone implements diagnostics only. VM creation, Docker Engine provisioning inside the VM, and Docker context wiring are represented by TODO service methods so the command structure and service boundaries are ready for later implementation.
