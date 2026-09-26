package com.example.gymsharktest.core.html

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Converts the HTML product descriptions returned by the catalogue into [RichText].
 *
 * Product descriptions are third-party content, so this is an allowlist, not a filter: only the
 * formatting tags in [INLINE_STYLES] and [BLOCK_TAGS] have any effect. Unknown elements contribute
 * their text but no styling, and the elements in [DROPPED_TAGS] are discarded along with their
 * children. Rendering to text rather than to a WebView means no markup, script, or remote content
 * from the payload can ever execute.
 *
 * The real payload is full of editor noise — `<meta charset>` mid-paragraph, nested
 * `<span class="TextRun …">` wrappers, and a trailing `<div id="gtx-trans">` translation widget —
 * so the parser is built to discard that silently rather than surface it as visible text.
 */
object HtmlDescriptionParser {

    private val INLINE_STYLES: Map<String, RichTextStyle> = mapOf(
        "strong" to RichTextStyle.Bold,
        "b" to RichTextStyle.Bold,
        "em" to RichTextStyle.Italic,
        "i" to RichTextStyle.Italic,
        "u" to RichTextStyle.Underline,
    )

    private val BLOCK_TAGS = setOf(
        "p", "div", "ul", "ol", "blockquote", "section", "article",
        "h1", "h2", "h3", "h4", "h5", "h6",
    )

    private val DROPPED_TAGS = setOf(
        "script", "style", "meta", "link", "head", "title",
        "iframe", "object", "embed", "noscript", "svg", "canvas", "form", "input",
    )

    /** The Google Translate widget the CMS leaves behind at the end of some descriptions. */
    private const val TRANSLATE_WIDGET_ID = "gtx-trans"

    /** Guards against a pathological payload driving unbounded parsing work on the main path. */
    private const val MAX_INPUT_LENGTH = 32_000

    private val WHITESPACE = Regex("[\\s\\u00A0\\u200B\\uFEFF]+")

    fun parse(html: String?): RichText {
        if (html.isNullOrBlank()) return RichText.Empty

        val body = Jsoup.parseBodyFragment(html.take(MAX_INPUT_LENGTH)).body()
        return Accumulator().apply { appendChildren(body) }.build()
    }

    private class Accumulator {
        private val text = StringBuilder()
        private val spans = mutableListOf<RichTextSpan>()

        fun appendChildren(node: Node) {
            node.childNodes().forEach(::append)
        }

        fun build(): RichText {
            trimEnd()
            val length = text.length
            val clamped = spans
                .filter { it.start < length }
                .map { if (it.end > length) it.copy(end = length) else it }
                .filter { it.end > it.start }
            return RichText(text.toString(), clamped)
        }

        private fun append(node: Node) {
            when (node) {
                is TextNode -> appendText(node.text())
                is Element -> appendElement(node)
                else -> Unit
            }
        }

        private fun appendElement(element: Element) {
            val tag = element.normalName()
            if (tag in DROPPED_TAGS || element.id() == TRANSLATE_WIDGET_ID) return

            when {
                tag == "br" -> appendNewLines(1)

                tag == "li" -> {
                    appendNewLines(1)
                    appendText("\u2022 ")
                    appendChildren(element)
                }

                tag in BLOCK_TAGS -> {
                    appendNewLines(2)
                    appendChildren(element)
                    appendNewLines(2)
                }

                else -> {
                    val style = INLINE_STYLES[tag]
                    if (style == null) {
                        appendChildren(element)
                    } else {
                        val start = text.length
                        appendChildren(element)
                        if (text.length > start) {
                            spans += RichTextSpan(style, start, text.length)
                        }
                    }
                }
            }
        }

        private fun appendText(raw: String) {
            val collapsed = raw.replace(WHITESPACE, " ")
            if (collapsed.isEmpty()) return

            if (collapsed.isBlank()) {
                appendSeparatorSpace()
                return
            }

            if (collapsed.first() == ' ') appendSeparatorSpace()
            text.append(collapsed.trim())
            if (collapsed.last() == ' ') text.append(' ')
        }

        /** Whitespace between elements still separates words, but never starts a line. */
        private fun appendSeparatorSpace() {
            if (text.isEmpty()) return
            val last = text.last()
            if (last != ' ' && last != '\n') text.append(' ')
        }

        /** Collapses to at most [count] newlines so nested blocks cannot stack blank lines. */
        private fun appendNewLines(count: Int) {
            if (text.isEmpty()) return
            trimTrailingSpaces()
            if (text.isEmpty()) return

            var existing = 0
            var index = text.lastIndex
            while (index >= 0 && text[index] == '\n') {
                existing++
                index--
            }
            repeat((count - existing).coerceAtLeast(0)) { text.append('\n') }
        }

        private fun trimTrailingSpaces() {
            while (text.isNotEmpty() && text.last() == ' ') text.setLength(text.length - 1)
        }

        private fun trimEnd() {
            while (text.isNotEmpty() && (text.last() == ' ' || text.last() == '\n')) {
                text.setLength(text.length - 1)
            }
        }
    }
}
