package com.pawno.studio.data.compiler

enum class CompilerVersion(
    val id: String,
    val libName: String,
    val displayName: String,
    val description: String,
    val isDefault: Boolean = false
) {
    AUTO(
        id = "auto",
        libName = "auto",
        displayName = "Auto-Detect (Smart)",
        description = "Otomatis mendeteksi compiler bawaan GM & auto-recovery",
        isDefault = true
    ),
    ZEEX_3_10_11(
        id = "3.10.11",
        libName = "pawnc31011",
        displayName = "Pawn 3.10.11 (Zeex)",
        description = "Modern Community Standard (open.mp, samp-stdlib, YSI 5)",
        isDefault = false
    ),
    ZEEX_3_10_7(
        id = "3.10.7",
        libName = "pawnc3107",
        displayName = "Pawn 3.10.7 (Zeex Classic)",
        description = "Community Classic SA-MP 0.3.7 Compiler",
        isDefault = false
    ),
    COMPUPHASE_3_2(
        id = "3.2.3664",
        libName = "pawnc32",
        displayName = "Pawn 3.2.3664 (Legacy)",
        description = "Classic SA-MP CompuPhase (Inferno, Native & Gamemode Jadul)",
        isDefault = false
    );

    companion object {
        val PAWN_3_10_11 = ZEEX_3_10_11
        val PAWN_3_10_7 = ZEEX_3_10_7
        val PAWN_3_2 = COMPUPHASE_3_2

        fun fromId(id: String?): CompilerVersion {
            return entries.find { it.id == id } ?: AUTO
        }
    }
}
