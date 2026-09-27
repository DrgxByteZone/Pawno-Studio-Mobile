# Whitepaper Teknis: Membedah & Mengoptimasi Engine Compiler Pawn 3.2.3664 pada Arsitektur Android (ARM64/x86_64)

> **Studi Kasus:** Mengatasi Infinite Freeze (>14 Menit), Crash I/O Driver FUSE, dan Limitasi Compiler pada Gamemode Monolitik *Atlantic.pwn* (108.381 Baris, AMX Output 91.5 MB) hingga Siap Dikompilasi dalam ~2 Menit di Perangkat Android.  
> **Platform & Arsitektur Target:** Android NDK (ARM64-v8a, ARMeabi-v7a, x86_64, x86) — Android 7.0 (API 24) s/d Android 15+.  
> **Penyusun & Peneliti:** Axel (@DrgxByteZone) & M.B.A (Blackpanther Company).  
> **Proyek:** Pawno Studio Mobile (Core Native Engine).  
> **Tanggal Publikasi Riset:** September 2026.

---

## 📑 Daftar Isi

1. [Abstrak & Latar Belakang Masalah](#1-abstrak--latar-belakang-masalah)
   - 1.1 [Dilema Ekosistem SA-MP: Mengapa Pawn 3.2 Menolak Mati?](#11-dilema-ekosistem-sa-mp-mengapa-pawn-32-menolak-mati)
   - 1.2 [Inkompatibilitas Zeex Pawn 3.10 pada Gamemode Monolitik](#12-inkompatibilitas-zeex-pawn-310-pada-gamemode-monolitik)
   - 1.3 [Karakteristik Subjek Uji: Atlantic.pwn (108.381 Baris)](#13-karakteristik-subjek-uji-atlanticpwn-108381-baris)
2. [Kronologi Kegagalan & Metodologi Investigasi](#2-kronologi-kegagalan--metodologi-investigasi)
   - 2.1 [Fase Porting Awal & Gejala Infinite Freeze](#21-fase-porting-awal--gejala-infinite-freeze)
   - 2.2 [Instrumentasi Profiling Stopwatch pada Core C](#22-instrumentasi-profiling-stopwatch-pada-core-c)
   - 2.3 [Tabel Breakdown Waktu Asli (Baseline Sebelum Optimasi)](#23-tabel-breakdown-waktu-asli-baseline-sebelum-optimasi)
3. [Bedah Solusi Algoritmik pada Core Engine C](#3-bedah-solusi-algoritmik-pada-core-engine-c)
   - 3.1 [Bottleneck #1: Ledakan Rekursi Call-Graph Stack ($O(2^N) \rightarrow O(V+E)$)](#31-bottleneck-1-ledakan-rekursi-call-graph-stack-o2n-rightarrow-ove)
   - 3.2 [Bottleneck #2: Linked List Traversal Kuadratik ($O(N^2) \rightarrow O(1)$)](#32-bottleneck-2-linked-list-traversal-kuadratik-on2-rightarrow-o1)
   - 3.3 [Bottleneck #3: I/O Syscall Flooding saat Penulisan AMX 91.5 MB](#33-bottleneck-3-io-syscall-flooding-saat-penulisan-amx-915-mb)
   - 3.4 [Bottleneck #4: Hardware SIMD In-Memory Line Scanning](#34-bottleneck-4-hardware-simd-in-memory-line-scanning)
4. [Perbaikan Kompatibilitas Semantik & Preprocessor](#4-perbaikan-kompatibilitas-semantik--preprocessor)
   - 4.1 [Implementasi Operator Macro Stringize (`#`)](#41-implementasi-operator-macro-stringize-)
   - 4.2 [Bug Premature Scope Pruning pada Array Multidimensi](#42-bug-premature-scope-pruning-pada-array-multidimensi)
5. [Penyelidikan Kritis: Jebakan Filesystem Android FUSE](#5-penyelidikan-kritis-jebakan-filesystem-android-fuse)
   - 5.1 [Trap #1: Kegagalan Resolusi Relative Path Mundur (`..`) & Error 100](#51-trap-1-kegagalan-resolusi-relative-path-mundur--dan-error-100)
   - 5.2 [Trap #2: Bencana Short-Read Truncation pada File >4 MB](#52-trap-2-bencana-short-read-truncation-pada-file-4-mb)
6. [Arsitektur Coexistence Multi-Engine & ABI Hardening](#6-arsitektur-coexistence-multi-engine--abi-hardening)
   - 6.1 [Pemisahan Shared Object (`libpawnc32.so`, `libpawnc3107.so`, `libpawnc.so`)](#61-pemisahan-shared-object-libpawnc32so-libpawnc3107so-libpawncso)
   - 6.2 [Linker Script Isolasi Simbol Global (`hide_symbols.lds`)](#62-linker-script-isolasi-simbol-global-hide_symbolslds)
   - 6.3 [Manajemen Memori Stack Thread Pthread 8 MB](#63-manajemen-memori-stack-thread-pthread-8-mb)
7. [Lapisan Aplikasi Android & Otomasi Engine Kotlin](#7-lapisan-aplikasi-android--otomasi-engine-kotlin)
   - 7.1 [Sanitasi Argumen CLI Pawn 3.2](#71-sanitasi-argumen-cli-pawn-32)
   - 7.2 [Injeksi Default Pawno Flags](#72-injeksi-default-pawno-flags)
   - 7.3 [Mekanisme Smart Auto-Recovery Fallback](#73-mekanisme-smart-auto-recovery-fallback)
8. [Tabel Komparasi & Hasil Benchmark Lapangan](#8-tabel-komparasi--hasil-benchmark-lapangan)
9. [Kesimpulan & Catatan Pasca-Riset (Engineering Reflections)](#9-kesimpulan--catatan-pasca-riset-engineering-reflections)

---

## 1. Abstrak & Latar Belakang Masalah

### 1.1 Dilema Ekosistem SA-MP: Mengapa Pawn 3.2 Menolak Mati?

San Andreas Multiplayer (SA-MP) dirilis pertama kali pada pertengahan 2006, mengadopsi compiler **Pawn 3.2.3664** yang dikembangkan oleh ITB CompuPhase. Selama dua dekade, ribuan server multiplayer dibangun di atas pondasi ini. Komunitas modding telah memproduksi jutaan baris kode script (`.pwn`) menggunakan compiler bawaan aplikasi Windows klasik *Pawno*.

Pada tahun 2011–2016, Zeex mengembangkan versi fork modern yaitu **Pawn 3.10 (Community Compiler)** yang membawa fitur-fitur modern: optimizer emit yang lebih baik, sistem warning yang ketat, arsitektur 64-bit cell, serta pelaporan diagnostik yang akurat. 

Namun fakta di lapangan menunjukkan paradoks besar: **Mayoritas gamemode skala besar di Indonesia dan komunitas internasional masih terjebak pada Pawn 3.2**. Mereka tidak dapat berpindah ke compiler modern bukan karena malas, melainkan karena restriksi teknis arsitektur script mereka sendiri.

### 1.2 Inkompatibilitas Zeex Pawn 3.10 pada Gamemode Monolitik

Ketika kami menguji gamemode roleplay besar di compiler Zeex 3.10, proses kompilasi langsung digagalkan oleh ratusan error fatal:
- **Strict Tag Checking**: Pawn 3.2 memperlakukan tag (seperti `Text3D:`, `Menu:`, `Float:`, `bool:`) secara sangat longgar. Memasukkan integer ke dalam enum atau tag khusus hanya menghasilkan peringatan ringan (atau diabaikan) di 3.2, tetapi memicu `error 035: argument type mismatch` pada 3.10.
- **Strict Array Return & Dimensions**: Di Pawn 3.2, passing array multidimensi tanpa ukuran spesifik pada dimensi kedua diperbolehkan dalam kondisi tertentu. Di 3.10 hal ini memicu error fatal.
- **Macro Preprocessor Quirks**: Trik macro lawas (seperti nested include guards, `#pragma dynamic` overriding, dan format string manual) dirancang khusus mengeksploitasi bug preprocessor CompuPhase 3.2. Memperbaiki script 100.000 baris agar *clean* di 3.10 membutuhkan waktu berminggu-minggu dan berisiko merusak logika runtime server.

Oleh karena itu, satu-satunya jalan agar pengembang SA-MP mobile dapat mengompilasi gamemode mereka langsung di HP Android tanpa mengubah satu baris pun kode gamemode adalah: **Mem-porting engine CompuPhase Pawn 3.2.3664 langsung ke Android NDK**.

### 1.3 Karakteristik Subjek Uji: Atlantic.pwn (108.381 Baris)

Sebagai tolok ukur pengujian ekstrem (*stress testing*), kami menggunakan gamemode roleplay nyata bernama **`Atlantic.pwn`**:
- **Ukuran File Sumber**: 6,74 MB (7.070.000+ byte teks mentah).
- **Jumlah Baris Kode**: 108.381 baris (termasuk puluhan dependensi include lokal).
- **Kompleksitas Simbol**: Lebih dari 5.200 fungsi, 1.400 enum, 3.800 variabel global, dan jutaan token ekspresi.
- **Ukuran Binary Output AMX**: **91.580.724 Bytes (~91.5 MB)** dengan 35 warning dan 0 error jika di-compile pada Windows Pawno original.

Script raksasa ini adalah "monster" penguji yang ideal. Jika engine mobile kami sanggup menaklukkan *Atlantic.pwn*, maka script gamemode SA-MP lain di dunia dipastikan akan dapat dikompilasi tanpa hambatan.

---

## 2. Kronologi Kegagalan & Metodologi Investigasi

### 2.1 Fase Porting Awal & Gejala Infinite Freeze

Porting awal Pawn 3.2 ke Android NDK menggunakan Clang C++20 awalnya tampak sukses. Kami mengompilasi source code C CompuPhase (`sc1.c` s/d `scmemfil.c`), membuat JNI wrapper sederhana, dan mengujinya dengan file `test.pwn` (200 baris). Hasilnya memuaskan: output AMX terbentuk dalam 0,15 detik.

Namun ketika *Atlantic.pwn* diumpankan ke dalam engine:
1. **Layar Android Hang / ANR**: Aplikasi langsung freeze. Logcat Android tidak mengeluarkan error, CPU load naik ke 100% pada satu core.
2. **Laptop Ikut Freeze saat Replikasi**: Kami mencoba menjalankan binary native hasil cross-compile di Linux desktop untuk memudahkan debugging gdb. Laptop pengembang mengalami freeze selama lebih dari 14 menit, kipas berputar maksimal, dan proses harus di-kill secara paksa (`SIGKILL`).
3. **Tidak Ada Pesan Error**: Compiler tidak crash dengan segfault, melainkan terjebak dalam *infinite computational loop* di dalam kode C compiler itu sendiri.

### 2.2 Instrumentasi Profiling Stopwatch pada Core C

Karena profiling standar seperti `gprof` sulit membaca siklus fungsi recursive call graph yang macet, kami menginjeksi instrumentasi waktu manual resolusi mikrodetik ke dalam pipeline utama compiler di `sc1.c`:

```c
#include <time.h>

static double get_time_sec(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (double)ts.tv_sec + (double)ts.tv_nsec / 1e9;
}

/* Logging di setiap titik kritis pipeline sc1.c: */
double t0 = get_time_sec();
/* ... eksekusi fase compiler ... */
fprintf(stderr, "[BENCHMARK] Fase X selesai dalam: %.4f detik\n", get_time_sec() - t0);
```

### 2.3 Tabel Breakdown Waktu Asli (Baseline Sebelum Optimasi)

Berikut adalah data profiling resmi yang kami peroleh saat membedah kompilasi *Atlantic.pwn* sebelum perbaikan dilakukan:

| Urutan | Fase Eksekusi Compiler (`sc1.c` / `sc6.c`) | File C Terkait | Durasi Waktu Asli | Status Diagnostik |
| :---: | :--- | :--- | :---: | :--- |
| 1 | Pass 1: Preprocessing & Parsing Awal | `sc1.c`, `sc2.c` | 18,24 detik | Normal |
| 2 | Pass 2: Parsing Ekspresi & Symbol Resolution | `sc1.c`, `sc3.c` | 175,41 detik | Cukup berat |
| 3 | **Kalkulasi Batas Kebutuhan Stack (`#pragma dynamic`)** | `sc1.c` (`max_stacksize`) | **570,82 detik (9,5 Menit!)** | 🔴 **CRITICAL BOTTLENECK #1** |
| 4 | **Perakitan Bytecode Assembler & Debug Info List** | `sc6.c`, `sclist.c` | **576,14 detik (9,6 Menit!)** | 🔴 **CRITICAL BOTTLENECK #2** |
| 5 | **Penulisan Output Binary AMX ke Disk (91.5 MB)** | `sc6.c` (`write_encoded`) | **34,22 detik** | 🟠 **I/O BOTTLENECK #3** |
| - | **Total Waktu Kompilasi (Tanpa Interupsi Crash)** | - | **> 23 Menit (1.374 Detik)** | **TIDAK LAYAK PAKAI** |

Data di atas membongkar realitas yang mengejutkan: **Lebih dari 83% waktu kompilasi dihabiskan hanya di dua fungsi C yang tidak efisien**, yaitu kalkulasi ukuran stack (`max_stacksize_recurse`) dan penambahan linked list informasi debug (`insert_string` / `get_string`).

---

## 3. Bedah Solusi Algoritmik pada Core Engine C

### 3.1 Bottleneck #1: Ledakan Rekursi Call-Graph Stack ($O(2^N) \rightarrow O(V+E)$)

#### Masalah Mendalam pada Kode Asli CompuPhase
Directive `#pragma dynamic` pada Pawn digunakan untuk menentukan batas alokasi memori stack/heap saat runtime. Untuk mengetahui batas aman alokasi stack yang dibutuhkan program, compiler Pawn menjalankan fungsi `max_stacksize(symbol *root, int *recursion)` di akhir Pass 2.

Fungsi ini memanggil pembantu rekursif `max_stacksize_recurse(symbol *sym, ...)` yang menelusuri seluruh *call graph* fungsi: fungsi A memanggil B dan C; fungsi B memanggil D dan E; dan seterusnya.

Berikut implementasi asli dari CompuPhase (`sc1.c`):
```c
/* KODE ASLI COMPUPHASE (UNOPTIMIZED - O(2^N) BRUTE FORCE) */
static long max_stacksize_recurse(symbol *sym, long basesize, int *pubfuncparams, int *recursion)
{
  long size, maxsize;
  int i;

  assert(sym != NULL);
  assert(sym->ident == iFUNCTN);
  maxsize = sym->x.stacksize;

  for (i = 0; i < sym->numrefers; i++) {
    if (sym->refer[i] != NULL) {
      /* Panggilan rekursif tanpa memoization */
      size = max_stacksize_recurse(sym->refer[i], sym->x.stacksize, pubfuncparams, recursion);
      if (maxsize < size)
        maxsize = size;
    }
  }
  return maxsize + basesize;
}
```

Pada script kecil dengan 20 fungsi, kode di atas berjalan dalam hitungan milidetik. Tetapi pada *Atlantic.pwn* dengan **5.200+ fungsi**, fungsi-fungsi utilitas umum (seperti `SendClientMessage`, `format`, `PlayerDataSave`, `GetPlayerPos`) dipanggil oleh ribuan fungsi lain. 

Struktur graf pemanggilan fungsi membentuk pohon eksponensial bercabang lebat ($O(2^N)$ atau $O(k^d)$). Compiler menghitung ulang fungsi yang sama secara berulang-ulang hingga **puluhan juta kali**, menyebabkan CPU berputar tanpa henti selama 570 detik (9,5 menit).

#### Solusi: Memoized DFS & Cycle Detection Menggunakan Field Compound
Kami merombak algoritma traversal ini menjadi **Depth-First Search Ter-Memoize (Dynamic Programming)** dengan kompleksitas linier terhadap simpul dan sisi graf ($O(V + E)$).

Tantangan teknisnya: struct `symbol` di Pawn 3.2 tidak memiliki field boolean ekstra untuk caching tanpa mengubah layout memori header compiler. Kami melakukan *field hijacking* yang aman: pada fase akhir kompilasi, field `sym->compound` (yang awalnya digunakan sebagai indikator level scope variabel lokal) sudah tidak lagi dipakai untuk simbol fungsi publik/global.

Kami menetapkan state machine pada `sym->compound`:
- `sym->compound == -1`: Simbol sedang aktif dalam rantai penelusuran call-stack saat ini. Jika fungsi ini terpanggil kembali oleh anak cabangnya, berarti terdeteksi **rekursi siklik** (*circular recursion*). Rekursi langsung diputus dan flag `recursion = 1` diaktifkan.
- `sym->compound > 0`: Nilai kebutuhan stack maksimum untuk sub-pohon fungsi ini sudah selesai dihitung sebelumnya. Langsung return nilainya dalam $O(1)$!

```c
/* KODE KUSTOM KAMI DI sc1.c (MEMOIZED DFS DENGAN DETEKSI SIKLUS) */
#if !defined SC_LIGHT
static long max_stacksize_recurse(symbol *sym, long basesize, int *pubfuncparams, int *recursion)
{
  long size, maxsize;
  int i;

  assert(sym != NULL);
  assert(sym->ident == iFUNCTN);
  assert((sym->usage & uNATIVE) == 0);
  assert(recursion != NULL);

  /* FAST PATH: Jika fungsi ini sudah pernah dihitung, langsung ambil dari cache! */
  if (sym->compound > 0)
    return sym->compound + basesize;

  /* CYCLE DETECTION: Mencegah infinite recursion loop */
  if (sym->compound == -1) {
    *recursion = 1;
    return sym->x.stacksize + basesize;
  }

  /* Tandai simpul ini sedang aktif di rantai penelusuran saat ini */
  sym->compound = -1;
  maxsize = sym->x.stacksize;

  for (i = 0; i < sym->numrefers; i++) {
    if (sym->refer[i] != NULL) {
      assert(sym->refer[i]->ident == iFUNCTN);
      assert((sym->refer[i]->usage & uNATIVE) == 0);
      size = max_stacksize_recurse(sym->refer[i], sym->x.stacksize, pubfuncparams, recursion);
      if (maxsize < size)
        maxsize = size;
    }
  }

  if ((sym->usage & uPUBLIC) != 0) {
    arginfo *arg = sym->dim.arglist;
    int count = 0;
    assert(arg != 0);
    while (arg->ident != 0) {
      count++;
      arg++;
    }
    assert(pubfuncparams != 0);
    if (count > *pubfuncparams)
      *pubfuncparams = count;
  }

  /* Simpan hasil kalkulasi (Memoize) ke dalam compound */
  sym->compound = (int)maxsize;
  return maxsize + basesize;
}
```

#### Dampak Pengujian
Waktu eksekusi fungsi `max_stacksize` terpangkas dari **570,82 detik menjadi 0,004 detik (< 5 milidetik)**. Peningkatan kecepatan mencapai **~114.000x lipat** tanpa mengubah hasil alokasi stack runtime AMX sedikit pun.

---

### 3.2 Bottleneck #2: Linked List Traversal Kuadratik ($O(N^2) \rightarrow O(1)$)

#### Masalah pada Tabel Debug Info
Pada saat compiler Pawn membentuk file intermediate assembler (`sc6.c`) dan mengumpulkan metadata debug simbolik (nomor baris file, nama fungsi, posisi lokal), compiler menyimpan setiap baris teks instruksi ke dalam struktur data single-linked list yang dikelola oleh `sclist.c`.

Berikut struktur data asli `sclist.c`:
```c
/* IMPLEMENTASI ASLI SCLIST.C */
typedef struct stringlist {
  struct stringlist *next;
  char *line;
} stringlist;

stringlist *insert_string(stringlist *root, char *string)
{
  stringlist *cur;
  /* ... alokasi malloc ... */
  cur->next = NULL;

  /* Traverse dari root sampai ujung list untuk menaruh elemen baru di akhir! */
  while (root->next != NULL)
    root = root->next;
  root->next = cur;
  return cur;
}
```

Ketika mengompilasi *Atlantic.pwn*, total baris intermediate assembly dan debug metadata mencapai **150.000+ entri string**. 

Karena `insert_string` tidak menyimpan pointer akhir (*tail pointer*), setiap kali satu baris baru ditambahkan, fungsi harus melakukan perulangan `while (root->next != NULL)` menelusuri dari elemen ke-1 sampai elemen ke-$N$.

Secara matematis, akumulasi operasi penelusuran pointer adalah:
$$\sum_{k=1}^{N} k = \frac{N(N - 1)}{2} = \frac{150.000 \times 149.999}{2} \approx 11.249.925.000 \text{ operasi traversal pointer!}$$

Lebih parah lagi, pada fungsi `get_string(stringlist *root, int index)`, fungsi ini dipanggil di dalam 6 loop terpisah saat tahap enkripsi bytecode untuk mengambil baris assembler secara berurutan ($0, 1, 2, 3, \dots, N$). Karena `get_string` juga mengulang dari `root` di setiap indeks, proses assembler berjalan seperti siput selama **576 detik (9,6 menit)**.

#### Solusi: Tail Pointer Cache & Sequential Index Accelerator
Kami mengintegrasikan dua mekanisme akselerasi pada `sclist.c`:
1. **Insert Tail Pointer Cache**: Menyimpan pointer node terakhir (`last_insert_tail`) sehingga penambahan string baru langsung menyambung di ekor dalam waktu konstan **$O(1)$**.
2. **Sequential Read Cache**: Menyimpan posisi iterator terakhir (`last_get_root`, `last_get_cur`, `last_get_index`). Karena compiler membaca baris intermediate secara teratur maju ke depan ($index \ge last\_get\_index$), fungsi pembacaan hanya perlu melangkah dari posisi saat ini tanpa perlu mengulang dari awal list.

```c
/* KODE OPTIMASI KAMI DI sclist.c */
static stringlist *last_insert_root = NULL;
static stringlist *last_insert_tail = NULL;
static stringlist *last_get_root = NULL;
static stringlist *last_get_cur = NULL;
static int last_get_index = -1;

/* Invalidation hook jika ada penghapusan item dari list */
static void invalidate_string_cache(void)
{
  last_insert_root = NULL;
  last_insert_tail = NULL;
  last_get_root = NULL;
  last_get_cur = NULL;
  last_get_index = -1;
}

stringlist *insert_string(stringlist *root, char *string)
{
  stringlist *cur;

  assert(string != NULL);
  if ((cur = (stringlist*)malloc(sizeof(stringlist))) == NULL)
    error(103);
  if ((cur->line = duplicatestring(string)) == NULL)
    error(103);
  cur->next = NULL;

  assert(root != NULL);
  /* O(1) FAST PATH: Sambungkan langsung ke pointer tail jika root sama */
  if (root == last_insert_root && last_insert_tail != NULL && last_insert_tail->next == NULL) {
    last_insert_tail->next = cur;
    last_insert_tail = cur;
  } else {
    /* SLOW FALLBACK: Traversal manual hanya jika cache miss / list baru */
    stringlist *tail = root;
    while (tail->next != NULL)
      tail = tail->next;
    tail->next = cur;
    last_insert_root = root;
    last_insert_tail = cur;
  }
  return cur;
}

char *get_string(stringlist *root, int index)
{
  stringlist *cur;
  int target = index;

  assert(root != NULL);
  if (index < 0) return NULL;

  /* O(1) SEQUENTIAL SCAN: Lanjutkan dari posisi terakhir jika maju sekuensial */
  if (root == last_get_root && last_get_cur != NULL && index >= last_get_index) {
    cur = last_get_cur;
    index -= last_get_index;
  } else {
    cur = root->next;
  }

  while (cur != NULL && index > 0) {
    cur = cur->next;
    index--;
  }

  if (cur != NULL) {
    assert(cur->line != NULL);
    last_get_root = root;
    last_get_cur = cur;
    last_get_index = target;
    return cur->line;
  }

  invalidate_string_cache();
  return NULL;
}
```

#### Dampak Pengujian
Waktu eksekusi perakitan intermediate assembler dan debug list di `sc6.c` anjlok dari **576,14 detik menjadi 3,00 detik**. Penurunan waktu mencapai **~192x lipat**.

---

### 3.3 Bottleneck #3: I/O Syscall Flooding saat Penulisan AMX 91.5 MB

#### Masalah Penulisan Unbuffered Single-Byte
Setelah assembler menyelesaikan parsing bytecode, fungsi `write_encoded` di `sc6.c` bertugas menulis instruksi biner AMX ke dalam media penyimpanan.

Pada implementasi asli CompuPhase, penulisan bytecode terkompresi ditulis ke disk byte demi byte:
```c
/* KODE ASLI COMPUPHASE (sc6.c) */
static void write_encoded(FILE *fbin, const cell *c, int num)
{
  /* Untuk setiap byte hasil kompresi cell: */
  for (i = 0; i < elen; i++)
    pc_writebin(fbin, &t_buf[i], 1); /* Menulis 1 byte ke kernel! */
}
```

Pada file *Atlantic.pwn*, binary AMX yang dihasilkan berukuran **91.580.724 Bytes (~91.5 MB)**.

Menulis 91,5 MB data dengan granularity 1–4 byte per panggilan menghasilkan sekitar **~80 juta kali syscall `sys_write` / `fwrite`**. Pada sistem operasi mobile Android di mana filesystem berjalan di atas layer emulasi storage (FUSE daemon / sdcardfs), setiap syscall memicu context-switching antara user-space compiler, kernel VFS, dan user-space FUSE daemon. Hal ini memicu I/O thrashing parah yang memakan waktu 34 detik dan mempercepat keausan chip flash memory eMMC/UFS.

#### Solusi: 64 KB User-Space RAM Write Buffer
Kami membuat ring buffer sebesar 64 KB (65.536 byte) di dalam memori RAM compiler di `sc6.c`. Seluruh penulisan bytecode ditampung terlebih dahulu di RAM dan hanya disemburkan ke disk menggunakan chunk besar 64 KB:

```c
/* IMPLEMENTASI BUFFER RAM 64 KB DI sc6.c */
static unsigned char bin_buf[65536];
static int bin_buf_pos = 0;

static void bin_buf_flush(FILE *fbin)
{
  if (bin_buf_pos > 0 && fbin != NULL) {
    writeerror |= !pc_writebin(fbin, bin_buf, bin_buf_pos);
    bin_buf_pos = 0;
  }
}

static inline void bin_buf_write(FILE *fbin, const void *data, int len)
{
  const unsigned char *src = (const unsigned char *)data;
  while (len > 0) {
    int avail = (int)sizeof(bin_buf) - bin_buf_pos;
    if (len <= avail) {
      memcpy(bin_buf + bin_buf_pos, src, len);
      bin_buf_pos += len;
      return;
    }
    memcpy(bin_buf + bin_buf_pos, src, avail);
    bin_buf_pos += avail;
    src += avail;
    len -= avail;
    bin_buf_flush(fbin);
  }
}
```

#### Dampak Pengujian
Jumlah syscall `write` berkurang drastis dari **~80.000.000 kali menjadi hanya ~1.400 kali** (pengurangan syscall sebesar **99,98%**). Durasi penulisan AMX ke storage terpangkas dari **34,22 detik menjadi 0,18 detik**.

---

### 3.4 Bottleneck #4: Hardware SIMD In-Memory Line Scanning

Pada modul `scmemfil.c` (fungsi `mfgets`), compiler membaca baris kode dari virtual memory file saat mengompilasi include yang di-load ke RAM.

Implementasi lama menggunakan loop manual byte per byte:
```c
/* KODE LAMA: Karakter demi karakter */
for (idx = 0; idx < size - 1; idx++) {
  c = mfgetc(mf);
  if (c == EOF) break;
  string[idx] = (char)c;
  if (c == '\n') break;
}
```

Kami mengganti loop manual tersebut dengan instruksi libc standar `memchr('\n')`:
```c
/* KODE BARU DI scmemfil.c */
char *mfgets(MEMFILE *mf, char *string, unsigned int size)
{
  unsigned int avail, len;
  char *newline;

  if (mf == NULL || string == NULL || size <= 1)
    return NULL;
  if (mf->offs >= mf->usedoffs)
    return NULL;

  avail = (unsigned int)(mf->usedoffs - mf->offs);
  if (avail > size - 1)
    avail = size - 1;

  /* Memanfaatkan instruksi SIMD hardware CPU (NEON / AVX2) melalui libc memchr */
  newline = (char *)memchr(mf->base + mf->offs, '\n', avail);
  if (newline != NULL) {
    len = (unsigned int)(newline - (mf->base + mf->offs)) + 1;
  } else {
    len = avail;
  }

  memcpy(string, mf->base + mf->offs, len);
  string[len] = '\0';
  mf->offs += len;
  return string;
}
```
Instruksi `memchr` pada Bionic Libc (Android ARM64) diimplementasikan langsung menggunakan assembly vector registers ARM NEON (memeriksa 16 byte karakter per siklus clock CPU), menghasilkan percepatan pembacaan baris in-memory hingga 400%.

---

## 4. Perbaikan Kompatibilitas Semantik & Preprocessor

### 4.1 Implementasi Operator Macro Stringize (`#`)

#### Akar Masalah pada Include Modul Populer
Banyak include dan library SA-MP populer (seperti `mxINI`, `sqlitei`, `sscanf2`, dan `YSI`) menggunakan macro modern bergaya C99 stringize (`#parameter`) untuk mengubah nama variabel menjadi string literal secara otomatis:

```pawn
/* Contoh pola macro di mxINI: */
#define INI_String(%0,%1,%2) if(!strcmp((%0), #%1, true))
```

Di compiler Zeex 3.10, operator `#` ini didukung penuh. Namun pada core **Pawn 3.2 CompuPhase**, preprocessor tidak memiliki logika untuk mendeteksi karakter `#` di awal argumen macro pengganti. Compiler malah menganggap karakter `#` sebagai karakter sintaks ilegal atau tidak dikenal, memicu error:
```text
error 001: expected token: ";", but found "-identifier-"
error 029: invalid expression, assumed zero
```

#### Solusi Implementasi di `sc2.c`
Kami menambahkan bit flag baru `#define STRINGIZE 4` pada parser makro di `sc2.c`, dan menambahkan logika penanganan stringify baik untuk string standar maupun packed string di dalam fungsi `substpattern_string`:

```c
/* KUTIPAN KODE DI sc2.c */
#define STRINGIZE 4

/* Saat memindai token macro: */
stringflags |= (*lptr == '#' || (*lptr == sc_ctrlchar && *(lptr + 1) == '#')) ? STRINGIZE : 0;

/* Saat melakukan token expansion penggantian parameter: */
if (flags & STRINGIZE) {
  /* Sisipkan tanda petik ganda pembuka */
  *dest++ = '"';
  while (*src != '\0') {
    if (*src == '"') *dest++ = '\\'; /* Escape petik ganda internal */
    *dest++ = *src++;
  }
  /* Sisipkan tanda petik ganda penutup */
  *dest++ = '"';
}
```
Dengan patch ini, ribuan macro pada `mxINI` dan modul database modern dapat berjalan mulus tanpa menimbulkan satupun error kompilasi.

---

### 4.2 Bug Premature Scope Pruning pada Array Multidimensi

#### Gejala Anomali: "Symbol already defined: 'i'"
Saat mengompilasi *Atlantic.pwn*, kami menemukan anomali aneh pada baris **81.699**:
```text
Atlantic.pwn(81699) : error 021: symbol already defined: "i"
Atlantic.pwn(81705) : error 021: symbol already defined: "bizz"
```
Padahal, variabel `new i;` di baris 81.699 tersebut berada di dalam fungsi mandiri yang sama sekali tidak berhubungan dengan fungsi sebelumnya, dan semua scope lokal di fungsi sebelumnya seharusnya sudah ditutup menggunakan kurung kurawal `}`.

#### Pelacakan Root Cause: Fatal Flaw pada `delete_symbols()`
Kami menelusuri bagaimana compiler Pawn menghapus simbol lokal dari linked list `loctab` ketika sebuah scope/fungsi selesai di-parse:

```c
/* KODE CACAT ASLI COMPUPHASE (sc2.c:delete_symbols) */
SC_FUNC void delete_symbols(symbol *root, int level, int delete_labels, int delete_functions)
{
  symbol *sym;
  while (root->next != NULL) {
    sym = root->next;
    if (sym->compound < level)
      break; /* <-- FATAL FLAW TERSEMBUNYI! */
    /* ... proses hapus simbol ... */
  }
}
```

Pawn mengasumsikan bahwa seluruh simbol lokal di `loctab` selalu tersusun terurut rapi berdasarkan level compound (`sym->compound >= level`).

Namun pada script besar dengan deklarasi array multidimensi lokal bertingkat:
```pawn
new bizz[MAX_BIZZES][E_BIZZ_DATA];
```
Fungsi `declloc` di `sc1.c` mengalokasikan sub-simbol untuk dimensi array kedua dengan mengisi `sym->compound = 0`!

Akibatnya:
1. Ketika fungsi selesai dan `delete_symbols(..., level=1)` dipanggil, fungsi menelusuri linked list.
2. Begitu fungsi menabrak sub-simbol array yang memiliki `sym->compound == 0` (< 1), kondisi `if (sym->compound < level)` terpenuhi.
3. Compiler mengeksekusi perintah **`break;`**!
4. **Traversal langsung dihentikan seketika**. Ratusan variabel lokal lain (seperti variabel iterator loop `i`, `idx`, `j`) yang terletak di belakang sub-simbol array tersebut **gagal dibersihkan dari memori** dan tertinggal di `loctab`.
5. Ketika fungsi berikutnya di baris 81.699 mendeklarasikan `new i;`, compiler mencari ke `loctab`, menemukan simbol `i` lama yang belum terhapus, dan memuntahkan `error 021: symbol already defined`!

#### Solusi Perbaikan
Kami memperbaiki dua titik:
1. **Di `sc2.c`**: Mengubah instruksi `break;` menjadi `{ root = sym; continue; }` agar proses pembersihan tidak berhenti mendadak dan terus mencari simbol lain di seluruh rantai node.
2. **Di `sc1.c` (`declloc`)**: Memastikan saat sub-array lokal dialokasikan, nilai compound mewarisi level nesting yang valid (`sym->compound = nestlevel`).

```c
/* PERBAIKAN DI sc2.c:delete_symbols */
  while (root->next != NULL) {
    sym = root->next;
    if (sym->compound < level) {
      root = sym;
      continue; /* Tetap lanjutkan traversal ke node berikutnya! */
    }
    /* ... hapus simbol lokal secara tuntas ... */
  }
```
Hasil: Error palsu di baris 81.699 langsung lenyap 100%.

---

## 5. Penyelidikan Kritis: Jebakan Filesystem Android FUSE

Android menggunakan lapisan arsitektur filesystem yang sangat unik dibandingkan distribusi Linux standar. Partisi penyimpanan publik (`/storage/emulated/0/`) dikelola oleh user-space daemon **FUSE (Filesystem in Userspace)** yang terintegrasi dengan Android MediaProvider. 

Ketika engine C native memanggil fungsi POSIX standar (`fopen`, `fread`, `opendir`, `stat`), syscall tersebut tidak langsung menuju ke driver disk ext4/f2fs, melainkan dicegat terlebih dahulu oleh kernel FUSE wrapper. Di sinilah dua bencana besar terjadi.

---

### 5.1 Trap #1: Kegagalan Resolusi Relative Path Mundur (`..`) & Error 100

#### Gejala Error di Logcat Android
Saat mengompilasi gamemode yang menyertakan framework library YSI, kompilasi langsung berhenti dengan pesan:
```text
include/YSI_Data/y_iterate.inc(107) : fatal error 100: cannot read from file: "..\YSI_Internal\y_compilerdata"
```

#### Analisis Root Cause
Di Windows Pawno, pemanggilan path relatif `#include "..\YSI_Internal\y_compilerdata"` secara otomatis dinormalisasi oleh Win32 API.

Di Android, engine case-insensitive file resolver kami mencoba mencari file dengan menelusuri folder per-komponen. Karena string include mengandung `..`, path yang terbentuk adalah:
`/storage/emulated/0/Pawno/include/YSI_Data/..`

Ketika kode memanggil `opendir("/storage/emulated/0/.../YSI_Data/..")`, **kernel FUSE Android menolak mentah-mentah panggilan ini dengan error code `ENOENT` atau `EACCES`**! Kebijakan keamanan sandbox Android FUSE melarang resolusi path direktori yang menyisakan token `..` di ujung traversal direktori virtual. Akibatnya, file `y_compilerdata.inc` dilaporkan tidak ditemukan dan memicu Fatal Error 100.

#### Solusi: Algoritma Canonicalization Berbasis Stack di Memori
Sebelum path diserahkan ke syscall filesystem kernel Android (`access`, `opendir`, `stat`), string path dinormalisasi terlebih dahulu di memori menggunakan struktur data stack token:

```cpp
/* IMPLEMENTASI DI compiler_jni.cpp & sc1.c */
static std::string resolve_case_insensitive_path(const std::string& inputPath) {
    if (inputPath.empty()) return inputPath;
    if (access(inputPath.c_str(), F_OK) == 0) return inputPath;

    // 1. Normalisasi backslash Windows (\ -> /)
    std::string norm = inputPath;
    for (char& c : norm) if (c == '\\') c = '/';

    bool isAbsolute = (!norm.empty() && norm[0] == '/');
    std::stringstream ss(norm);
    std::string token;
    std::vector<std::string> rawTokens;
    while (std::getline(ss, token, '/')) {
        if (!token.empty()) rawTokens.push_back(token);
    }

    // 2. Collapsing stack: Hapus pasangan folder dengan ".."
    std::vector<std::string> stack;
    for (const auto& part : rawTokens) {
        if (part == ".") {
            continue;
        } else if (part == "..") {
            if (!stack.empty() && stack.back() != "..") {
                stack.pop_back(); /* Pop folder induk sebelumnya! */
            } else if (!isAbsolute) {
                stack.push_back("..");
            }
        } else {
            stack.push_back(part);
        }
    }

    // 3. Rekonstruksi path kanonikal yang bersih dari ".."
    std::string canonical = isAbsolute ? "/" : "";
    for (size_t i = 0; i < stack.size(); ++i) {
        canonical += stack[i];
        if (i + 1 < stack.size()) canonical += "/";
    }

    // Path kini bersih: /storage/emulated/0/Pawno/include/YSI_Internal/y_compilerdata.inc
    // Aman dipanggil oleh opendir / fopen di Android FUSE!
    return canonical;
}
```
Hasil: Seluruh include bersarang (*nested relative includes*) pada YSI dan library kompleks lainnya terbaca 100% tanpa Error 100.

---

### 5.2 Trap #2: Bencana Short-Read Truncation pada File >4 MB

#### Misteri 26 Undefined Symbol & "Compilation Aborted"
Setelah isu stack dan linked list teratasi, kami melakukan tes kompilasi penuh *Atlantic.pwn* di Android. Tiba-tiba layar diagnostik memuntahkan **26 error undefined symbol berturut-turut** dan diakhiri dengan pesan frustasi:
```text
Atlantic.pwn(44130) : error 017: undefined symbol "TogglePlayerControllableEx"
Atlantic.pwn(44145) : error 017: undefined symbol "CreateDynamicObject"
Atlantic.pwn(44150) : error 017: undefined symbol "SendClientMessageToAllEx"
...
Atlantic.pwn(44200) : fatal error 107: too many error messages on one line
Compilation aborted.
Pawn compiler 3.2.3664 Copyright (c) 1997-2006, ITB CompuPhase
26 Errors.
```

Mengapa compiler mengklaim fungsi `TogglePlayerControllableEx` tidak terdefinisi, padahal fungsi tersebut didefinisikan dengan jelas di bagian bawah file script yang sama? Dan mengapa error selalu dimulai tepat di **baris 44.130**?

#### Penyelidikan Hex / Byte Offset: Truncation di 3,6 MB!
Kami memeriksa ukuran file *Atlantic.pwn*: **6,74 MB (7.070.000 byte)**.
Lalu kami memeriksa implementasi pembacaan file sumber di `compiler_jni.cpp` (`pc_opensrc`):

```cpp
/* KODE ASLI LAMA (CRITICAL BUG TERSEMBUNYI) */
extern "C" void* pc_opensrc(char* filename) {
    FILE* f = fopen(filename, "rb");
    fseek(f, 0, SEEK_END);
    long fsize = ftell(f);
    fseek(f, 0, SEEK_SET);

    std::string content(fsize, '\0');
    /* PEMANGGILAN FREAD TUNGGAL TANPA LOOP */
    size_t bytesRead = fread(&content[0], 1, fsize, f);
    content.resize(bytesRead); /* <-- BENCANA DI SINI! */
    fclose(f);
    return memfile_open(content.data(), content.size());
}
```

Di desktop Linux (glibc dengan disk ext4), pemanggilan `fread(..., 1, 7000000, f)` hampir selalu berhasil menyedot seluruh 7 MB data dalam satu kali eksekusi.

Tetapi pada **Android Bionic Libc di atas layer FUSE Daemon**, batas transfer buffer pipa I/O dibatasi pada ambang tertentu (~3,6 MB). Pemanggilan `fread()` tunggal hanya mengembalikan:
$$\text{bytesRead} = 3.670.016 \text{ byte}$$

Ketika baris berikutnya dieksekusi:
```cpp
content.resize(bytesRead);
```
Fungsi `resize()` memotong paksa sisa 3,4 MB data file! Tepat di byte ke-3.670.016 (yaitu tepat pada kata `TogglePlayerControllableEx` di baris **44.130**), file dianggap EOF (selesai).

Akibatnya: **Baris 44.131 sampai 108.381 (tempat semua fungsi, stock, dan callback utama didefinisikan) tidak pernah dibaca oleh compiler!** Compiler mengira baris-baris tersebut tidak ada, sehingga memuntahkan puluhan error *undefined symbol* dan langsung menghentikan proses kompilasi (*Compilation aborted*).

#### Solusi: Loop Pembacaan Multi-Chunk Mutlak
Kami mengganti panggilan instan tersebut dengan loop multi-chunk terverifikasi yang menjamin seluruh byte terbaca tuntas:

```cpp
/* PERBAIKAN MUTLAK DI compiler_jni.cpp:pc_opensrc */
std::string content(fsize, '\0');
size_t totalBytesRead = 0;

while (totalBytesRead < static_cast<size_t>(fsize)) {
    size_t chunkToRead = static_cast<size_t>(fsize) - totalBytesRead;
    size_t n = fread(&content[totalBytesRead], 1, chunkToRead, f);
    if (n == 0) {
        if (feof(f)) break; /* Akhir file sah tercapai */
        if (ferror(f)) {
            LOGE("I/O Error membaca %s pada offset %zu: %s", 
                 fname.c_str(), totalBytesRead, strerror(errno));
            break;
        }
    }
    totalBytesRead += n;
}

content.resize(totalBytesRead);
```

#### Dampak Pengujian
Seluruh 108.381 baris (6,74 MB) file *Atlantic.pwn* terbaca utuh 100%. Pesan "26 Errors & Compilation Aborted" hilang total, dan compiler melanjutkan proses hingga tahap akhir.

---

## 6. Arsitektur Coexistence Multi-Engine & ABI Hardening

Aplikasi *Pawno Studio Mobile* dirancang untuk mendukung tiga varian engine compiler sekaligus:
1. **CompuPhase Pawn 3.2.3664 (Legacy Engine)**: Untuk gamemode lawas seperti *Atlantic.pwn*.
2. **Zeex Pawn 3.10.7 (Stable Modern)**: Untuk gamemode modern standar.
3. **Zeex Pawn 3.10.11 (Latest Community)**: Untuk project open.mp dan fitur compiler terbaru.

### 6.1 Pemisahan Shared Object
Ketiga engine dibangun menjadi tiga shared library `.so` terpisah di dalam APK:
- `libpawnc32.so`
- `libpawnc3107.so`
- `libpawnc.so`

### 6.2 Linker Script Isolasi Simbol Global (`hide_symbols.lds`)
Masalah arsitektur terbesar dari compiler Pawn berbasis bahasa C adalah **ratusan variabel global non-static** yang dideklarasikan di `scvars.c` (seperti `glbtab`, `loctab`, `inpf`, `curseg`, `fline`, `litidx`).

Jika ketiga library `.so` ini di-load oleh Android Dynamic Linker (`/system/bin/linker64`) ke dalam satu memori address space proses aplikasi tanpa proteksi, simbol global ini akan saling menimpa (*symbol collision*), menyebabkan crash memory corruption acak.

Untuk mengisolasi simbol secara mutlak, kami merancang GNU Linker Version Script ([`hide_symbols.lds`](file:///home/drgxel/Documents/android%20project/Pawno/app/src/main/cpp/hide_symbols.lds)):

```text
{
    global:
        Java_com_pawno_studio_data_compiler_PawnCompilerEngine_*;
        Java_com_pawno_studio_data_amx_AmxInspector_*;
        JNI_OnLoad;
    local:
        *;
};
```

Dan mengonfigurasikannya pada [`CMakeLists.txt`](file:///home/drgxel/Documents/android%20project/Pawno/app/src/main/cpp/CMakeLists.txt):
```cmake
target_link_options(pawnc32 PRIVATE
    "-Wl,-Bsymbolic"
    "-Wl,-Bsymbolic-functions"
    "-Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/hide_symbols.lds"
)
```
Dengan konfigurasi ini, seluruh ratusan simbol global C Pawn berstatus `STV_HIDDEN` (*local*). Hanya entry-point JNI Java yang diekspos secara global. Ketiga engine dapat hidup berdampingan secara damai tanpa saling mengotori memori.

### 6.3 Manajemen Memori Stack Thread Pthread 8 MB
Parser ekspresi Pawn bersifat rekursif mendalam saat mengurai formula matematika atau ternary operator bertingkat. 

Pada Android, thread default Java/Kotlin hanya dialokasikan stack sebesar 1 MB s/d 2 MB. Menjalankan kompilasi gamemode 108.000 baris di thread utama atau thread Coroutine standar kerap memicu **`SIGSEGV (SEGV_ACCERR)`** akibat kehabisan stack (*Stack Overflow*).

Di `compiler_jni.cpp`, proses kompilasi dieksekusi di dalam thread native POSIX terisolasi dengan atribut ukuran stack diperbesar menjadi **8 MegaByte**:

```cpp
pthread_attr_t attr;
pthread_attr_init(&attr);
pthread_attr_setstacksize(&attr, 8 * 1024 * 1024); /* Alokasi 8 MB Stack */
pthread_create(&workerThread, &attr, compiler_worker_entry, params);
pthread_join(workerThread, NULL);
pthread_attr_destroy(&attr);
```

---

## 7. Lapisan Aplikasi Android & Otomasi Engine Kotlin

### 7.1 Sanitasi Argumen CLI Pawn 3.2
Pada compiler modern Zeex 3.10, argumen CLI seperti `-w 203` (warning suppression) dapat ditulis terpisah dengan spasi.

Namun pada Pawn 3.2 CompuPhase, parser argumen membaca token secara primitif. Jika token `-w` diikuti spasi dan angka `203`, token angka `203` disalahartikan sebagai nama file input sumber (`203.p`), memicu error fatal `cannot open file 203.p`.

Di [`PawnCompilerEngine.kt`](file:///home/drgxel/Documents/android%20project/Pawno/app/src/main/java/com/pawno/studio/data/compiler/PawnCompilerEngine.kt), kami menyematkan parser sanitasi cerdas:
```kotlin
/* Menggabungkan flag terpisah menjadi token tunggal untuk Pawn 3.2 */
if (version == CompilerVersion.COMPUPHASE_3_2) {
    sanitizedArgs = mergeSeparatedFlags(rawArgs, setOf("-w", "-d", "-O", "-v"))
    // Menghasilkan "-w203" secara bersih
}
```

### 7.2 Injeksi Default Pawno Flags
Pada Windows original Pawno, aplikasi selalu menyematkan dua flag tersembunyi:
- `-;+`: Mewajibkan penulisan titik koma (*semicolon*) di akhir setiap statement kode.
- `-(+`: Mewajibkan penulisan tanda kurung pada ekspresi logika tanda kurung.

Di `PawnCompilerEngine.kt`, kedua flag esensial ini diinjeksikan secara otomatis ke dalam parameter kompilasi Pawn 3.2 untuk memastikan kepatuhan 100% terhadap perilaku lingkungan Pawno Windows original.

### 7.3 Mekanisme Smart Auto-Recovery Fallback
Jika pengguna menyetel pilihan compiler ke mode **`AUTO`** di aplikasi, sistem pertama kali akan mencoba mengompilasi menggunakan compiler modern Zeex 3.10.11.

Jika kompilasi 3.10 menghasilkan error sintaks khas CompuPhase legacy (misalnya `error 010: invalid function or declaration`, `error 001: expected token: ";"`, atau `warning 208`), sistem secara cerdas melakukan fallback instan ke engine `libpawnc32.so`. Jika kompilasi 3.2 berhasil dengan 0 error, output AMX 3.2 langsung diadopsi dan pengguna diberikan banner notifikasi:
> *"Script Anda berhasil dikompilasi menggunakan Pawn 3.2 Legacy Engine (Auto-Recovered)."*

---

## 8. Tabel Komparasi & Hasil Benchmark Lapangan

### Pengujian Ekstrem pada Subjek: Atlantic.pwn
- **Hardware Penguji**: Xiaomi Poco F3 (Qualcomm Snapdragon 870, 8 GB LPDDR5 RAM, Storage UFS 3.1).
- **Sistem Operasi**: Android 13 (HyperOS / AOSP Kernel 4.19).
- **Ukuran File**: 6,74 MB (108.381 baris).

| Parameter Evaluasi | Sebelum Optimasi | Sesudah Optimasi | Rasio Efisiensi | Status Akhir |
| :--- | :---: | :---: | :---: | :---: |
| **Status Kompilasi Gamemode** | ❌ Infinite Hang / Crash | ✅ **SUKSES (0 Error, 35 Warning)** | - | **100% Teratasi** |
| **Ukuran Output Biner AMX** | 0 Byte (Gagal) | **91.580.724 Bytes (~91.5 MB)** | - | **Valid & Siap Main** |
| **Kalkulasi Stack (`max_stacksize`)** | 570,82 detik (9,5 Menit) | **0,004 detik** | **~142.000x Lebih Cepat** | Instant |
| **Tahap Assembler & Debug (`sc6.c`)** | 576,14 detik (9,6 Menit) | **3,00 detik** | **~192x Lebih Cepat** | Instant |
| **Pass 2 Symbol Resolution (`sc1.c`)** | 175,41 detik | **48,12 detik** | **~3,6x Lebih Cepat** | Sangat Cepat |
| **Penulisan Output AMX ke Disk** | 34,22 detik | **0,18 detik** | **~190x Lebih Cepat** | 64KB RAM Buffer |
| **Penanganan Path Relatif (`..`)** | ❌ Error 100 YSI | ✅ **100% Berhasil Membaca** | - | Stack Collapsing |
| **Integritas File Besar (>4 MB)** | ❌ Terpotong di baris 44.130 | ✅ **108.381 baris terbaca penuh** | - | Chunk Read Loop |
| **Total Waktu Kompilasi Penuh** | **> 23 Menit (Freeze)** | **~2 Menit (129 Detik)** | **~11x Lebih Cepat** | **Standar Industri** |

---

## 9. Kesimpulan & Catatan Pasca-Riset (Engineering Reflections)

Mengembangkan compiler native C/C++ pada arsitektur modern Android bukanlah sekadar urusan "compile dengan NDK lalu panggil via JNI". 

Riset ini membuktikan bahwa:
1. **Algoritma yang dibuat 20 tahun lalu (Pawn 3.2 CompuPhase tahun 2006)** dirancang ketika file script rata-rata hanya berukuran beberapa ratus baris kode. Pada gamemode modern dengan 100.000+ baris, kompleksitas waktu asimtotik yang buruk ($O(2^N)$ pada stack recursion dan $O(N^2)$ pada linked-list) langsung runtuh dan melumpuhkan sistem.
2. **Abstraksi Filesystem Android FUSE** menyimpan jebakan laten: pemanggilan `fread` tunggal yang tidak ter-loop pada file besar dapat memotong data di tengah jalan tanpa menghasilkan kode error `ferror()`, serta penolakan strict terhadap path relatif direktori `..`.
3. Melalui serangkaian optimasi memoization, caching pointer, buffering I/O user-space, dan pembersihan semantik tabel simbol, compiler warisan yang sebelumnya dinilai "usang dan tidak mungkin berjalan di HP" kini berhasil dikompilasi secara stabil, menghasilkan file AMX seukuran 91.5 MB hanya dalam waktu **~2 menit** langsung dari genggaman ponsel Android.

Dokumentasi ini kami dedikasikan untuk seluruh komunitas pengembang SA-MP, reverse engineer, dan penggemar arsitektur sistem compiler.

---
**Penyusun:**  
*Axel (@DrgxByteZone) & M.B.A (Blackpanther Company)*  
*Pawno Studio Mobile Core Team — 2026*
