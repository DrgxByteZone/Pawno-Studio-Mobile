# 🤝 Contributing to Pawno Studio Mobile

First off, thank you for considering contributing to **Pawno Studio Mobile**! It's people like you who make the SA-MP and open.mp mobile development ecosystem awesome.

---

## 🧭 Code of Conduct
By participating in this project, you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md).

---

## 🛠️ Development Setup

### Prerequisites
1. **Android Studio**: Ladybug / Meerkat (2024+) or latest canary.
2. **Java Development Kit**: JDK 17 (OpenJDK or Eclipse Temurin).
3. **Android NDK**: Version `26.2.11394342` (or NDK r26+).
4. **CMake**: Version `3.22.1` or newer.

### Getting the Code
```bash
git clone https://github.com/DrgxByteZone/Pawno-Studio-Mobile.git
cd Pawno-Studio-Mobile
```

### Building the Project
From terminal:
```bash
./gradlew assembleDebug
```
Or open the project in Android Studio, sync Gradle, and run on a connected device/emulator.

---

## 📁 Architecture Overview

Pawno Studio Mobile is divided into two primary subsystems:

1. **Kotlin / Jetpack Compose Layer (`app/src/main/java`)**:
   - `core/`: Dispatchers, constants, coroutine runners.
   - `data/compiler/`: `PawnCompilerEngine`, `CompilerVersion`, `CompilerDetector`, `CompileOptions`.
   - `ui/`: Jetpack Compose screens (`ide`, `diagnostics`, `settings`, `amx`, `about`).
   - `editor/`: Pawn lexer, token coloring, fast symbol insertion.

2. **Native C/C++ Engine Layer (`app/src/main/cpp`)**:
   - `compiler_jni.cpp`: JNI bridge, multi-chunk `fread` buffer, stack-based case-insensitive path resolver, thread stack configuration.
   - `amx_inspector.cpp`: Native AMX bytecode header, public function, and native import scanner.
   - `compilers/pawnc-3.2/`: CompuPhase Pawn 3.2.3664 (optimized with memoized stack DFS, $O(1)$ linked-list tail cache, 64KB bytecode write buffer).
   - `compilers/pawnc-3.10.7/`: Zeex Pawn 3.10.7 (Legacy SA-MP compatibility).
   - `compilers/pawnc-3.10.11/`: Zeex Pawn 3.10.11 (Modern SA-MP / open.mp default).
   - `hide_symbols.lds`: Linker script to prevent global symbol pollution between compiler versions.

---

## 🌿 Git Workflow

1. Fork the repo on GitHub.
2. Clone your fork locally:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Follow **Conventional Commits**:
   - `feat(...)`: A new feature
   - `fix(...)`: A bug fix
   - `perf(...)`: A code change that improves performance
   - `docs(...)`: Documentation only changes
   - `refactor(...)`: A code change that neither fixes a bug nor adds a feature
   - `chore(...)`: Changes to the build process or auxiliary tools

4. Ensure your changes compile without warnings:
   ```bash
   ./gradlew assembleDebug
   ```

5. Push to your fork and submit a Pull Request.

---

## 🐛 Reporting Bugs & Compiler Issues
Please use the provided [Issue Templates](https://github.com/DrgxByteZone/Pawno-Studio-Mobile/issues/new/choose) and include:
- The exact gamemode / include structure.
- The raw diagnostic log from the Diagnostics Screen.
- Your Android device model and OS version.
