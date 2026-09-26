package com.pawno.studio.editor

import android.os.Bundle
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.analysis.SimpleAnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.lang.styling.MappedSpans
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.widget.SymbolPairMatch
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class PawnLanguage : Language {

    private val autoCompleteProvider = PawnAutoCompleteProvider()
    private val lexer = PawnLexer()

    private val analyzeManager = object : SimpleAnalyzeManager<Any?>() {
        override fun analyze(
            content: StringBuilder,
            delegate: Delegate<Any?>
        ): Styles {
            try {
                val builder = MappedSpans.Builder()
                builder.addNormalIfNull()

                val len = content.length
                var lineStart = 0
                var lineIndex = 0
                var inBlockComment = false

                while (lineStart < len) {
                    if (delegate.isCancelled) {
                        return Styles(builder.build())
                    }

                    var lineEnd = content.indexOf('\n', lineStart)
                    if (lineEnd == -1) lineEnd = len

                    var actualLineEnd = lineEnd
                    if (actualLineEnd > lineStart && content[actualLineEnd - 1] == '\r') {
                        actualLineEnd--
                    }

                    val lineSub = content.subSequence(lineStart, actualLineEnd)
                    inBlockComment = lexer.tokenize(lineSub, inBlockComment) { type, start, _ ->
                        val colorId = when (type) {
                            PawnTokenType.KEYWORD, PawnTokenType.DIRECTIVE -> EditorColorScheme.KEYWORD
                            PawnTokenType.NATIVE, PawnTokenType.CALLBACK -> EditorColorScheme.FUNCTION_NAME
                            PawnTokenType.OPERATOR -> EditorColorScheme.OPERATOR
                            PawnTokenType.STRING, PawnTokenType.CHARACTER, PawnTokenType.NUMBER -> EditorColorScheme.LITERAL
                            PawnTokenType.COMMENT -> EditorColorScheme.COMMENT
                            else -> EditorColorScheme.TEXT_NORMAL
                        }
                        val style = TextStyle.makeStyle(colorId)
                        builder.addIfNeeded(lineIndex, start, style)
                    }
                    builder.determine(lineIndex)

                    lineStart = lineEnd + 1
                    lineIndex++
                }

                if (lineStart == len && len > 0 && content[len - 1] == '\n') {
                    builder.determine(lineIndex)
                }

                return Styles(builder.build())
            } catch (e: Exception) {
                android.util.Log.e("PawnLanguage", "Error during syntax analysis", e)
                val fallbackBuilder = MappedSpans.Builder()
                fallbackBuilder.addNormalIfNull()
                return Styles(fallbackBuilder.build())
            }
        }
    }

    override fun getAnalyzeManager(): AnalyzeManager = analyzeManager

    override fun getInterruptionLevel(): Int = Language.INTERRUPTION_LEVEL_NONE

    override fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        extraArguments: Bundle
    ) {
        autoCompleteProvider.requireAutoComplete(content, position, publisher)
    }

    override fun getIndentAdvance(content: ContentReference, line: Int, column: Int): Int {
        val lineText = content.getLine(line).toString().trim()
        return if (lineText.endsWith("{")) 4 else 0
    }

    override fun useTab(): Boolean = false

    override fun getFormatter(): Formatter = EmptyLanguage.EmptyFormatter.INSTANCE

    override fun getSymbolPairs(): SymbolPairMatch {
        val pairs = SymbolPairMatch()
        pairs.putPair('{', SymbolPairMatch.SymbolPair("{", "}"))
        pairs.putPair('(', SymbolPairMatch.SymbolPair("(", ")"))
        pairs.putPair('[', SymbolPairMatch.SymbolPair("[", "]"))
        pairs.putPair('"', SymbolPairMatch.SymbolPair("\"", "\""))
        pairs.putPair('\'', SymbolPairMatch.SymbolPair("'", "'"))
        return pairs
    }

    override fun getNewlineHandlers(): Array<NewlineHandler> {
        return emptyArray()
    }

    override fun destroy() {
        analyzeManager.destroy()
    }
}
