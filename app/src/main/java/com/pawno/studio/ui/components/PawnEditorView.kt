package com.pawno.studio.ui.components

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pawno.studio.editor.PawnColorScheme
import com.pawno.studio.editor.PawnLanguage
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun PawnEditorView(
    modifier: Modifier = Modifier,
    initialContent: String = "",
    onEditorCreated: (CodeEditor) -> Unit = {},
    onEditorReady: (CodeEditor) -> Unit = onEditorCreated,
    onContentChange: (String) -> Unit = {},
    onCursorChange: (Int, Int) -> Unit = { _, _ -> }
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            CodeEditor(context).apply {
                // Typography & Monospace
                typefaceText = Typeface.MONOSPACE
                typefaceLineNumber = Typeface.MONOSPACE
                setTextSize(13f)

                // Flat minimal palette
                colorScheme = PawnColorScheme()

                // Pawn language & lexer
                setEditorLanguage(PawnLanguage())

                // Editor behaviors
                isLineNumberEnabled = true
                tabWidth = 4
                isCursorAnimationEnabled = true
                isWordwrap = false

                setText(initialContent)

                // Listen for text edits
                subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
                    onContentChange(text.toString())
                }

                // Listen for cursor position updates
                subscribeEvent(SelectionChangeEvent::class.java) { _, _ ->
                    onCursorChange(cursor.leftLine + 1, cursor.leftColumn + 1)
                }

                onEditorReady(this)
                onEditorCreated(this)
            }
        },
        update = { editor ->
            // Prevent resetting if content matches
            if (editor.text.toString() != initialContent && initialContent.isNotEmpty()) {
                val cursor = editor.cursor
                val line = cursor.leftLine
                val col = cursor.leftColumn
                editor.setText(initialContent)
                try {
                    editor.setSelection(line, col)
                } catch (_: Exception) {}
            }
        }
    )
}
