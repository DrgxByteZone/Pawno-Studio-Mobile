package com.pawno.studio.editor

import android.graphics.Color
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Engineered Precision Flat 2D Minimalist ColorScheme for Sora Editor.
 * Zero neon, zero gradient, zero blur. Swiss structural discipline.
 * Author: By M.B.A & AXEL - Blackpanther Company
 */
class PawnColorScheme : EditorColorScheme() {

    override fun applyDefault() {
        super.applyDefault()

        // Surface canvas & selection
        setColor(WHOLE_BACKGROUND, Color.parseColor("#0E1117"))
        setColor(CURRENT_LINE, Color.parseColor("#161B22"))
        setColor(SELECTION_INSERT, Color.parseColor("#238636"))
        setColor(SELECTION_HANDLE, Color.parseColor("#238636"))
        setColor(SELECTED_TEXT_BACKGROUND, Color.parseColor("#21262D"))

        // Gutter line numbers
        setColor(LINE_NUMBER_BACKGROUND, Color.parseColor("#0E1117"))
        setColor(LINE_NUMBER, Color.parseColor("#8B949E"))
        setColor(LINE_NUMBER_CURRENT, Color.parseColor("#F0F6FC"))
        setColor(LINE_DIVIDER, Color.parseColor("#30363D"))

        // Cursor & Text
        setColor(SELECTION_INSERT, Color.parseColor("#7BDB80"))
        setColor(TEXT_NORMAL, Color.parseColor("#F0F6FC"))

        // Syntax Highlighting Tokens
        setColor(KEYWORD, Color.parseColor("#7BDB80"))      // Primary green (public, new, stock, return)
        setColor(FUNCTION_NAME, Color.parseColor("#A2C9FF")) // Tertiary blue (callbacks & functions)
        setColor(IDENTIFIER_NAME, Color.parseColor("#F0F6FC")) // White-gray identifier
        setColor(LITERAL, Color.parseColor("#AFC6FF"))       // Secondary blue strings & numbers
        setColor(COMMENT, Color.parseColor("#8B949E"))       // Muted gray comments
        setColor(OPERATOR, Color.parseColor("#F0F6FC"))      // Clean white operators
        setColor(BLOCK_LINE, Color.parseColor("#30363D"))    // 1px structural indent guides
        setColor(BLOCK_LINE_CURRENT, Color.parseColor("#58A6FF")) // Active scope guide
    }
}
