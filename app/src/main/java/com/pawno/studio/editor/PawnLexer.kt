package com.pawno.studio.editor

class PawnLexer {

    companion object {
        val KEYWORDS: Set<String> = setOf(
            "new", "public", "stock", "forward", "native", "enum", "switch", "case",
            "default", "if", "else", "while", "for", "do", "return", "break", "continue",
            "goto", "static", "const", "sizeof", "tagof", "state", "sleep", "assert",
            "true", "false"
        )

        val DIRECTIVES: Set<String> = setOf(
            "#include", "#define", "#pragma", "#if", "#else", "#elif", "#endif",
            "#tryinclude", "#undef", "#error", "#warning", "#line", "#endinput"
        )

        val BUILTIN_TAGS: Set<String> = setOf(
            "Float", "bool", "File", "Text", "Text3D", "Menu", "PlayerText", "PlayerText3D"
        )
    }

    fun interface TokenCallback {
        fun onToken(type: PawnTokenType, start: Int, end: Int)
    }

    fun tokenize(text: CharSequence, callback: TokenCallback) {
        var i = 0
        val len = text.length

        while (i < len) {
            val c = text[i]

            // 1. Whitespace
            if (c.isWhitespace()) {
                val start = i
                while (i < len && text[i].isWhitespace()) i++
                callback.onToken(PawnTokenType.WHITESPACE, start, i)
                continue
            }

            // 2. Comments
            if (c == '/' && i + 1 < len) {
                if (text[i + 1] == '/') {
                    val start = i
                    while (i < len && text[i] != '\n') i++
                    callback.onToken(PawnTokenType.COMMENT, start, i)
                    continue
                } else if (text[i + 1] == '*') {
                    val start = i
                    i += 2
                    while (i + 1 < len && !(text[i] == '*' && text[i + 1] == '/')) {
                        i++
                    }
                    if (i + 1 < len) i += 2 // Skip closing */
                    callback.onToken(PawnTokenType.COMMENT, start, i)
                    continue
                }
            }

            // 3. Preprocessor Directives
            if (c == '#') {
                val start = i
                i++
                while (i < len && text[i].isLetter()) i++
                callback.onToken(PawnTokenType.DIRECTIVE, start, i)
                continue
            }

            // 4. Strings
            if (c == '"') {
                val start = i
                i++
                var escape = false
                while (i < len) {
                    val sc = text[i]
                    if (escape) {
                        escape = false
                    } else if (sc == '\\') {
                        escape = true
                    } else if (sc == '"') {
                        i++
                        break
                    } else if (sc == '\n') {
                        break
                    }
                    i++
                }
                callback.onToken(PawnTokenType.STRING, start, i)
                continue
            }

            // 5. Character literals
            if (c == '\'') {
                val start = i
                i++
                var escape = false
                while (i < len) {
                    val sc = text[i]
                    if (escape) {
                        escape = false
                    } else if (sc == '\\') {
                        escape = true
                    } else if (sc == '\'') {
                        i++
                        break
                    } else if (sc == '\n') {
                        break
                    }
                    i++
                }
                callback.onToken(PawnTokenType.CHARACTER, start, i)
                continue
            }

            // 6. Numbers (decimal, hex, float)
            if (c.isDigit() || (c == '.' && i + 1 < len && text[i + 1].isDigit())) {
                val start = i
                if (c == '0' && i + 1 < len && (text[i + 1] == 'x' || text[i + 1] == 'X')) {
                    i += 2
                    while (i < len && (text[i].isDigit() || text[i] in 'a'..'f' || text[i] in 'A'..'F')) i++
                } else {
                    var hasDot = c == '.'
                    while (i < len && (text[i].isDigit() || (!hasDot && text[i] == '.'))) {
                        if (text[i] == '.') hasDot = true
                        i++
                    }
                }
                callback.onToken(PawnTokenType.NUMBER, start, i)
                continue
            }

            // 7. Identifiers, Keywords, Natives & Callbacks
            if (c.isLetter() || c == '_') {
                val start = i
                while (i < len && (text[i].isLetterOrDigit() || text[i] == '_')) i++
                val word = text.subSequence(start, i).toString()

                val type = when {
                    KEYWORDS.contains(word) -> PawnTokenType.KEYWORD
                    BUILTIN_TAGS.contains(word) -> PawnTokenType.KEYWORD
                    SampCallbacks.isCallback(word) -> PawnTokenType.CALLBACK
                    SampNatives.isNative(word) -> PawnTokenType.NATIVE
                    else -> PawnTokenType.IDENTIFIER
                }
                callback.onToken(type, start, i)
                continue
            }

            // 8. Operators
            if ("+-*/%=!<>|&^~?:".contains(c)) {
                val start = i
                i++
                // Multi-char operators like ==, !=, <=, >=, &&, ||, ++, --, +=, -=, *=, /=, ::
                if (i < len && "+-*/%=!<>|&:".contains(text[i])) {
                    i++
                }
                callback.onToken(PawnTokenType.OPERATOR, start, i)
                continue
            }

            // 9. Symbols & Punctuation
            if (";,(){}[]".contains(c)) {
                callback.onToken(PawnTokenType.SYMBOL, i, i + 1)
                i++
                continue
            }

            // Default
            callback.onToken(PawnTokenType.UNKNOWN, i, i + 1)
            i++
        }
    }

    fun tokenize(text: CharSequence): List<PawnToken> {
        val tokens = mutableListOf<PawnToken>()
        tokenize(text) { type, start, end ->
            tokens.add(PawnToken(type, start, end))
        }
        return tokens
    }
}
