package com.pawno.studio.data.compiler

enum class CompilerVersion(
    val id: String,
    val libName: String,
    val displayName: String,
    val description: String,
    val isDefault: Boolean = false
) {
    ZEEX_3_10_11(
        id = "3.10.11",
        libName = "pawnc31011",
        displayName = "Pawn 3.10.11",
        description = "Modern Community Standard (Bugfixes & Optimizations)",
        isDefault = true
    ),
    ZEEX_3_10_7(
        id = "3.10.7",
        libName = "pawnc3107",
        displayName = "Pawn 3.10.7",
        description = "Community Classic Compiler",
        isDefault = false
    );

    companion object {
        val PAWN_3_10_11 = ZEEX_3_10_11
        val PAWN_3_10_7 = ZEEX_3_10_7

        fun fromId(id: String?): CompilerVersion {
            return entries.find { it.id == id } ?: ZEEX_3_10_11
        }
    }
}
