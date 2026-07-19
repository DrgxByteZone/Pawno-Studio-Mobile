package com.pawno.studio.editor

data class PawnToken(
    val type: PawnTokenType,
    val startIndex: Int,
    val endIndex: Int
) {
    val startOffset: Int get() = startIndex
    val endOffset: Int get() = endIndex
}
