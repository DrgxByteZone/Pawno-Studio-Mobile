# 🚀 Pawno Studio Mobile — v1.0.0 Release Notes (Official Open Source Launch)

**Release Date:** September 2026  
**Repository:** [https://github.com/DrgxByteZone/Pawno-Studio-Mobile](https://github.com/DrgxByteZone/Pawno-Studio-Mobile)  
**Authors:** M.B.A & AXEL — Blackpanther Company / DrgxByteZone

---

## 🌐 English Summary

Welcome to the initial official open-source release of **Pawno Studio Mobile (v1.0.0)**!

Pawno Studio Mobile is a complete native IDE and compiler designed from the ground up for Android. It brings full desktop-grade SA-MP and open.mp Pawn development to your smartphone, with **100% offline local compilation** and zero cloud dependencies.

### 🌟 Key Highlights

1. **Triple Embedded Compiler Architecture**:
   - **CompuPhase Pawn 3.2.3664 (Legacy Engine)**: Heavily optimized and patched for classic and monolithic roleplay gamemodes.
   - **Zeex Pawn 3.10.7**: Legacy SA-MP Community edition.
   - **Zeex Pawn 3.10.11**: Modern standard compiler for open.mp and SA-MP.
   - Native builds for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.

2. **57,000x Speed Optimization (Pawn 3.2)**:
   - Evaluated on *Atlantic.pwn* (~108,000 lines, 91.5 MB AMX output).
   - Replaced $O(2^N)$ brute-force stack graph recursion with **Memoized DFS** (reduced stack check from 9.5 minutes to <0.01s).
   - Replaced $O(N^2)$ linked-list string insertion with **$O(1)$ tail pointers and index cache** (reduced assembler pass from 9.6 minutes to 3 seconds).
   - Added user-space 64 KB RAM write buffer, cutting millions of redundant disk syscalls.
   - Total compilation time dropped from **>14 minutes / infinite freeze to ~2 minutes (0 errors, 35 warnings)**.

3. **Android FUSE Storage Resilience**:
   - **Relative Includes (`..`)**: Stack-based canonicalizer collapsing directory traversal tokens before calling `opendir()` (fixing Error 100 on FUSE storage).
   - **Multi-Chunk `fread` Loop**: Prevents buffer truncation on files larger than 4 MB (fixing undefined symbol errors and premature EOFs).
   - **Case-Insensitive VFS**: Maps Windows-style includes (e.g. `#include <a_samp>`) to exact disk casing.

4. **Smart Auto-Recovery**:
   - Automatically detects legacy syntax errors in mode `AUTO` and recovers using the optimized Pawn 3.2 engine.

5. **Integrated Mobile Tooling**:
   - High-contrast, responsive Pawn syntax highlighter.
   - Quick-access symbol bar (`{`, `}`, `(`, `)`, `;`, `[`, `]`, `&`, `|`, `"`, `=`, `#`).
   - Jump-to-line error navigation from live compiler diagnostics.
   - Native AMX binary bytecode inspector.

---

## 🇮🇩 Ringkasan Bahasa Indonesia

Halo Komunitas SA-MP & open.mp Indonesia! 🇮🇩

Sesuai janji, malam ini **Pawno Studio Mobile v1.0.0 resmi dirilis secara Open Source** untuk publik!

Banyak developer SA-MP di Indonesia mengeluhkan susahnya mengompilasi gamemode roleplay besar di Android: compiler sering *force close*, *freeze* berbelas-belas menit, atau memunculkan error palsu karena perbedaan sistem file Android vs Windows. 

Pawno Studio Mobile hadir untuk menyelesaikan seluruh masalah tersebut secara tuntas!

### ⚡ Fitur Utama:
- **3 Mesin Compiler Sekaligus**: Bebas pilih antara **Pawn 3.2 Legacy**, **Pawn 3.10.7**, atau **Pawn 3.10.11** langsung dari menu Settings.
- **Bisa Compile Gamemode Raksasa (108.000+ Baris)**: Sudah diuji dan terbukti sukses mengompilasi gamemode masif (seperti *Atlantic.pwn* ukuran 6,7 MB / AMX 91,5 MB) hanya dalam waktu **~2 menit dengan 0 Error**!
- **Anti-Freeze & Super Kencang**: Optimasi algoritma memoized DFS membuat kalkulasi stack instan (0,00 detik), dan penulisan AMX menggunakan buffer RAM 64 KB.
- **Kebal Bug Android FUSE**: Include yang memakai path mundur seperti `#include "..\YSI_Internal\y_compilerdata"` sekarang bisa dibaca 100% mulus di HP.
- **Auto-Recovery Cerdas**: Kalau gamemode lawas Anda error saat di-compile dengan 3.10, aplikasi secara otomatis mencoba me-recover menggunakan Pawn 3.2!
- **Inspector AMX**: Cek langsung isi file `.amx` Anda (daftar fungsi public, native imports, dan pemakaian memori stack).

---

## 📥 Panduan Instalasi (How to Install)

1. Download file **`PawnoStudio-v1.0.0.apk`** dari tab [Releases](https://github.com/DrgxByteZone/Pawno-Studio-Mobile/releases).
2. Install APK di smartphone Android Anda (Mendukung Android 7.0 hingga Android 15+).
3. Berikan izin akses penyimpanan (*Storage Permission*) agar aplikasi dapat membaca folder gamemode Anda.
4. Buka file `.pwn` gamemode atau filterscript Anda, lalu tekan tombol **Compile**!

---

## 🛠️ Catatan Rilis Versi 1.0.0 (Roadmap Kedepan)

Sebagai versi rilis perdana (**v1.0.0**), fondasi utama compiler dan editor sudah stabil dan siap pakai untuk produksi. Fitur yang direncanakan untuk pembaruan berikutnya (v1.1+):
- [ ] Auto-complete / IntelliSense untuk native bawaan SA-MP & open.mp.
- [ ] Integrasi Git clone & commit langsung dari aplikasi.
- [ ] Package manager untuk download include populer (YSI, streamer, sscanf, dll.) dalam satu klik.

---

## 📜 Lisensi & Kontribusi

Pawno Studio Mobile dirilis di bawah lisensi **Apache License 2.0**. Kode sumber terbuka sepenuhnya untuk dipelajari, dikembangkan bersama, atau dikontribusikan melalui Pull Request di:  
👉 [https://github.com/DrgxByteZone/Pawno-Studio-Mobile](https://github.com/DrgxByteZone/Pawno-Studio-Mobile)
