# Riset & Optimasi Engine Pawn 3.2 pada Pawno Studio Mobile

> **Studi Kasus:** Mengatasi Infinite Freeze (>14 Menit), Crash I/O FUSE, dan Limitasi Compiler pada Gamemode Monolitik *Atlantic.pwn* (~108.000 Baris) hingga Siap Dikompilasi dalam ~2 Menit di Android.  
> **Platform & Arsitektur:** Android NDK (ARM64-v8a, ARMeabi-v7a, x86_64).  
> **Author:** Axel (@DrgxByteZone) & M.B.A (Blackpanther Company).

---

## 1. Latar Belakang & Akar Masalah

Compiler **Pawn 3.2.3664 (CompuPhase Legacy)** adalah standar yang digunakan oleh mayoritas server SA-MP klasik hingga roleplay Indonesia modern. Gamemode besar seperti *Atlantic.pwn* (6,74 MB / 108.381 baris) dibangun dengan pola penulisan yang sangat terikat pada semantik Pawn 3.2, sehingga langsung menghasilkan puluhan error jika dipaksa di-compile menggunakan Zeex Pawn 3.10.

Ketika mem-porting engine Pawn 3.2 native (C) ke Android untuk kebutuhan Pawno Studio Mobile, kami menemukan sejumlah hambatan kritis pada script berskala besar:

1. **Infinite Hang / Freeze (>14 Menit)**: Compiler berhenti merespons saat proses perhitungan alokasi stack dan saat menulis informasi debug simbolik.
2. **Crash & Hambatan Filesystem Android FUSE**:
   - `Error 100 (cannot read file)`: Gagal resolve include yang menggunakan relative path mundur (seperti `..\YSI_Internal\y_compilerdata`).
   - `Short-Read Truncation`: Pembacaan file sumber berukuran >4 MB terpotong di tengah jalan pada driver FUSE, memicu puluhan *undefined symbol error* palsu dan compiler abort.
3. **Inkompatibilitas Sintaks & Scope Simbol**:
   - Gagal mengurai token stringize (`#`) pada library/macro umum (`mxINI`, `sqlitei`).
   - Muncul error scope palsu (`symbol already defined: "i"`) pada array lokal bertingkat.

---

## 2. File yang Dimodifikasi

Berikut rincian file sumber yang dirombak untuk menyelesaikan isu-isu di atas:

### Engine Native C / C++ (`app/src/main/cpp/`)
* **`compilers/pawnc-3.2/source/compiler/sc1.c`**:
  - Mengganti algoritma rekursi `max_stacksize_recurse` dengan Memoized DFS & cycle detector.
  - Perbaikan inisialisasi compound level array lokal pada `declloc`.
  - Integrasi normalisasi path `..` pada `case_insensitive_fopen`.
* **`compilers/pawnc-3.2/source/compiler/sclist.c`**:
  - Penambahan pointer `tail` ($O(1)$ append) dan sequential index cache ($O(1)$ read) pada struktur data string list.
* **`compilers/pawnc-3.2/source/compiler/sc6.c`**:
  - Penambahan memory write-buffer 64 KB (`bin_buf`) untuk pemangkasan syscall `fwrite` saat perakitan bytecode AMX.
* **`compilers/pawnc-3.2/source/compiler/sc2.c`**:
  - Implementasi handler token `#` (stringize macro).
  - Perbaikan traversal pembersihan scope simbol lokal di `delete_symbols`.
* **`compilers/pawnc-3.2/source/compiler/scmemfil.c`**:
  - Pemanfaatan `memchr` hardware-accelerated pada fungsi pembacaan baris in-memory `mfgets`.
* **`compiler_jni.cpp`**:
  - Implementasi loop pembacaan mutlak multi-chunk `fread` pada `pc_opensrc`.
  - Canonicalization path berbasis stack untuk menghapus relative traversal `..`.
  - JNI bridge khusus versi 3.2 (`compileNative32`) dan alokasi pthread stack 8 MB.
* **`CMakeLists.txt` & `hide_symbols.lds`**:
  - Konfigurasi target compile terisolasi untuk `libpawnc32.so`, `libpawnc3107.so`, dan `libpawnc.so` menggunakan flag `-O3 -flto` serta pembatasan visibility simbol global.

### Lapisan Android & UI (`app/src/main/java/com/pawno/studio/`)
* **`data/compiler/CompilerDetector.kt`**: Heuristik pendeteksi karakteristik gamemode (Pawn 3.2 vs 3.10).
* **`data/compiler/PawnCompilerEngine.kt`**: Engine orchestrator, sanitasi argumen CLI, auto-recovery fallback jika kompilasi 3.10 gagal pada gamemode legacy.
* **`ui/screens/`**: UI selector compiler version, real-time diagnostic console, serta jump-to-line navigation.

---

## 3. Optimasi Algoritmik & Performa Engine C

### 1. Eliminasi Bottleneck Rekursi Call-Graph Stack
* **Target:** `sc1.c` (`max_stacksize_recurse`)
* **Masalah:** Fungsi kalkulasi batas kebutuhan stack (`#pragma dynamic`) menelusuri seluruh graf pemanggilan fungsi secara brute-force DFS rekursif. Pada gamemode raksasa dengan ribuan fungsi yang saling memanggil, fungsi yang sama dihitung ulang jutaan kali, menyebabkan compiler macet lebih dari 9 menit.
* **Solusi:** Dirombak menjadi **Memoized Depth-First Search** dengan deteksi siklus via `sym->compound`:
  - Nilai `-1`: Simbol sedang aktif dalam rantai traversal (mencegah infinite loop rekursif).
  - Nilai `> 0`: Nilai stack maksimum fungsi tersebut sudah tersimpan dan langsung dipakai (*cached*).
* **Hasil:** Waktu kalkulasi stack terpangkas drastis dari **570 detik (9,5 menit) menjadi < 0,01 detik**.

### 2. Eliminasi Bottleneck Traversal Linked List Debug Info
* **Target:** `sclist.c` (`insert_string`, `get_string`) dan `sc6.c` (`append_dbginfo`)
* **Masalah:** Penyimpanan informasi debug string (mencapai 150.000+ entri) memakai linked-list konvensional tanpa pointer akhir. Setiap penambahan elemen baru harus menelusuri dari elemen pertama hingga ujung list ($O(N^2)$ traversal secara akumulatif).
* **Solusi:**
  - Menambahkan pointer `tail` langsung pada struct list, sehingga operasi append menjadi $O(1)$.
  - Menambahkan **Sequential Index Cache** (`last_node`, `last_index`) pada fungsi `get_string` agar pembacaan sekuensial berjalan langsung tanpa looping dari awal.
* **Hasil:** Tahap akhir assembler dan debug information generation terpangkas dari **576 detik (9,6 menit) menjadi hanya 3 detik**.

### 3. User-Space RAM Buffer untuk Menekan Disk Syscalls
* **Target:** `sc6.c` (`write_encoded`, `bin_buf`)
* **Masalah:** Pada implementasi aslinya, penulisan output binary AMX memanggil fungsi tulis per-byte tunggal. Untuk file output sebesar 91,5 MB, hal ini menghasilkan jutaan syscall `fwrite` bertubi-tubi ke disk Android.
* **Solusi:** Membuat memory buffer 64 KB di user-space. Aliran data bytecode ditampung terlebih dahulu di RAM dan baru di-flush secara batch ke disk ketika buffer penuh atau proses kompilasi selesai.

### 4. Optimalisasi Stream Parsing
* **Target:** `scmemfil.c` (`mfgets`)
* **Solusi:** Mengganti pencarian newline manual berbasis chunk loop dengan fungsi standar `memchr('\n')`, yang secara native memanfaatkan instruksi SIMD prosesor untuk scanning memori kecepatan tinggi.

---

## 4. Penanganan Kompatibilitas Semantik & Macro

### 1. Dukungan Operator Stringize (`#`)
* **Target:** `sc2.c` (`substpattern_string`)
* **Masalah:** Macro modern seperti `INI_String(%0,%1,%2) if(!strcmp((%0), #%1, true))` gagal diurai di Pawn 3.2 lama karena parser belum mengonversi karakter `#` menjadi string literal.
* **Solusi:** Menambahkan penanganan flag `STRINGIZE` pada alur penggantian macro token stream, baik untuk mode packed maupun unpacked string.

### 2. Perbaikan Pembersihan Scope Array Multidimensi
* **Target:** `sc2.c` (`delete_symbols`) dan `sc1.c` (`declloc`)
* **Masalah:** Muncul error duplikasi simbol palsu (`symbol already defined: "i"`) pada baris 81.000+. Hal ini terjadi karena sub-array lokal multidimensi sempat terinisialisasi dengan `compound = 0`, sehingga proses pembersihan scope lokal terhenti sebelum waktunya.
* **Solusi:** Memastikan sub-array mewarisi level compound yang sesuai saat deklarasi di `declloc`, serta mengubah loop pembersihan di `delete_symbols` agar tetap melanjutkan iterasi symbol table.

---

## 5. Penyelesaian Hambatan Filesystem Android FUSE

### A. Penanganan Relatif Include Path (..) & Error 100
* **Masalah:** Include kompleks (seperti pada framework YSI) kerap menggunakan path bertingkat mundur, misalnya `#include "..\YSI_Internal\y_compilerdata"`. Pada kernel Android dengan storage FUSE (`sdcardfs` / MediaProvider), pemanggilan `opendir()` atau `stat()` pada path yang menyisakan `/..` akan ditolak oleh sistem (`ENOENT`), memicu `Error 100: cannot read from file`.
* **Solusi:** Mengimplementasikan path canonicalization berbasis stack pada `compiler_jni.cpp` dan `sc1.c`. Setiap kali token `..` ditemukan, folder induk sebelumnya langsung dipotong dari memori sehingga path menjadi bersih dan absolut sebelum diserahkan ke fungsi I/O kernel.

### B. Penanganan Short-Read Truncation pada File Berukuran Besar (>4 MB)
* **Masalah:** Gamemode masif seperti *Atlantic.pwn* berukuran 6,74 MB. Pemanggilan `fread()` tunggal pada file sebesar ini melalui layer FUSE Android tidak menjamin seluruh byte dibaca dalam satu panggilan; pembacaan sempat terpotong di sekitar 3,6 MB. Akibatnya, baris setelah titik potong (baris 44.130 ke atas) hilang, memicu puluhan error `undefined symbol` dan membuat kompilasi dibatalkan (*Compilation aborted*).
* **Solusi:** Mengganti pembacaan instan dengan loop mutlak `fread` yang membaca file secara bertahap hingga seluruh ukuran file terpenuhi atau mencapai status `EOF`/error valid:
  ```cpp
  size_t totalBytesRead = 0;
  while (totalBytesRead < static_cast<size_t>(fsize)) {
      size_t n = fread(&content[totalBytesRead], 1, static_cast<size_t>(fsize) - totalBytesRead, f);
      if (n == 0) {
          if (feof(f) || ferror(f)) break;
      }
      totalBytesRead += n;
  }
  content.resize(totalBytesRead);
  ```

---

## 6. Arsitektur Native Multi-Version & Isolasi Simbol

Agar ketiga engine compiler (**Pawn 3.2 Legacy**, **Zeex 3.10.7**, dan **Zeex 3.10.11**) dapat berjalan stabil berdampingan dalam satu aplikasi tanpa tabrakan simbol memory:

1. **Pemisahan Shared Library**:
   - `libpawnc32.so`: Engine CompuPhase Pawn 3.2.3664 teroptimasi.
   - `libpawnc3107.so`: Engine Zeex Pawn 3.10.7.
   - `libpawnc.so`: Engine Zeex Pawn 3.10.11 standar open.mp.
2. **Isolasi Simbol (`hide_symbols.lds`)**:
   Karena compiler Pawn berbasis C memiliki ratusan variabel global (seperti `glbtab`, `inpf`, `fline`), kami menerapkan version script linker agar simbol internal masing-masing engine berstatus `local`, mencegah *symbol collision* antar engine di runtime Android.
3. **Dedicated 8 MB Stack Thread**:
   Proses kompilasi dieksekusi pada background worker thread tersendiri dengan stack size 8 MB (`pthread_attr_setstacksize`), menghindari crash *SIGSEGV Stack Overflow* saat parser memproses ekspresi rekursif yang sangat dalam.

---

## 7. Hasil Pengujian & Benchmark Nyata

Pengujian dilakukan menggunakan gamemode nyata **Atlantic.pwn (108.381 baris / ukuran 6,74 MB)** langsung pada perangkat Android:

| Parameter Evaluasi | Sebelum Optimasi | Sesudah Optimasi | Status |
| :--- | :---: | :---: | :---: |
| **Hasil Kompilasi Atlantic.pwn** | Freeze >14 Menit / Crash FUSE | **SUKSES (0 Error, 35 Warning)** | **Selesai 100%** |
| **Ukuran Output AMX** | 0 Byte (Gagal) | **91.580.724 Bytes (~91.5 MB)** | **Valid & Playable** |
| **Kalkulasi Batas Stack (`sc1.c`)** | 570 detik (9,5 Menit) | **< 0,01 detik** | **~57.000x Lebih Cepat** |
| **Assembler Pass & Debug (`sc6.c`)**| 576 detik (9,6 Menit) | **3,00 detik** | **~192x Lebih Cepat** |
| **Pass 2 Symbol Resolution (`sc1.c`)**| 175 detik | **48 detik** | **~3,6x Lebih Cepat** |
| **Relative Path Resolution (`..`)** | Error 100 | **100% Terbaca** | Resolved |
| **Pembacaan File Gamemode >4 MB** | Terpotong di baris 44.130 | **108.381 baris terbaca utuh** | Resolved |
| **Total Waktu Build** | **> 14 Menit (Hang)** | **~2 Menit (129 Detik)** | **Stabil Siap Pakai** |

---
*Dokumentasi ini merupakan catatan teknis resmi dari tim pengembang Pawno Studio Mobile.*
