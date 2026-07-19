package com.pawno.studio.editor

import io.github.rosemoe.sora.lang.completion.CompletionHelper
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference

class PawnAutoCompleteProvider {

    fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher
    ) {
        val prefix = CompletionHelper.computePrefix(content, position) { c ->
            Character.isJavaIdentifierPart(c) || c == '#'
        }

        if (prefix.isBlank()) return

        val pLower = prefix.lowercase()

        // 1. Directives if starts with #
        if (prefix.startsWith("#")) {
            for (dir in PawnLexer.DIRECTIVES) {
                if (dir.lowercase().startsWith(pLower)) {
                    publisher.addItem(
                        SimpleCompletionItem(dir, "Directive", prefix.length, dir)
                    )
                }
            }
            return
        }

        // 2. Keywords
        for (kw in PawnLexer.KEYWORDS) {
            if (kw.lowercase().startsWith(pLower)) {
                publisher.addItem(
                    SimpleCompletionItem(kw, "Keyword", prefix.length, kw)
                )
            }
        }

        // 3. SA-MP Callbacks
        for (cb in SampCallbacks.findMatching(prefix)) {
            publisher.addItem(
                SimpleCompletionItem(cb.name, "Callback", prefix.length, cb.name)
            )
        }

        // 4. SA-MP Natives
        for (nativeFunc in SampNatives.findMatching(prefix)) {
            val commitText = "${nativeFunc.name}("
            publisher.addItem(
                SimpleCompletionItem(
                    nativeFunc.name,
                    nativeFunc.signature,
                    prefix.length,
                    commitText
                )
            )
        }
    }
}
