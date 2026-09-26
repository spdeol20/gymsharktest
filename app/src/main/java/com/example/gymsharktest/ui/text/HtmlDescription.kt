package com.example.gymsharktest.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

/**
 * Renders the CMS-authored HTML product descriptions the catalogue returns.
 *
 * Two libraries, no bespoke parsing: Jsoup reduces the markup to an allowlist of formatting tags,
 * then Compose's own `AnnotatedString.fromHtml` turns that into styled text.
 *
 * The sanitising pass is load-bearing, not decoration. `android.text.Html`, which backs
 * `fromHtml`, emits the *contents* of `<script>` and `<style>` elements as visible characters, so
 * without it a stylesheet in a description would render as a wall of CSS. It also drops the editor
 * residue this payload is full of — `data-mce-*` attributes, `<meta charset>` mid-paragraph, the
 * trailing `<div id="gtx-trans">` translate widget — and restricts `href` values to safe
 * protocols, so a `javascript:` link cannot reach a clickable annotation.
 */
object HtmlDescription {

    /**
     * Allows the inline formatting and list tags these descriptions use, plus links with
     * protocol-checked `href`s. Everything else contributes text but no markup.
     */
    private val safelist: Safelist = Safelist.basic()

    /** Guards against a pathological payload driving unbounded parsing work. */
    private const val MAX_INPUT_LENGTH = 32_000

    private val NON_BREAKING_SPACE = Regex("\u00A0")
    private val HORIZONTAL_RUNS = Regex("[ \\t]{2,}")
    private val AROUND_NEWLINE = Regex("[ \\t]*\\n[ \\t]*")
    private val BLANK_LINES = Regex("\\n{3,}")

    /**
     * Whitespace is normalised rather than trusted: this payload is littered with filler blocks
     * like `<p> <br data-mce-fragment="1"></p>`, which would otherwise render as gaps of empty
     * lines part-way through a description.
     */
    fun render(html: String?): AnnotatedString {
        if (html.isNullOrBlank()) return AnnotatedString("")

        val sanitised = Jsoup.clean(html.take(MAX_INPUT_LENGTH), safelist)
        if (sanitised.isBlank()) return AnnotatedString("")

        return AnnotatedString.fromHtml(sanitised)
            .replaceAll(NON_BREAKING_SPACE, " ")
            .replaceAll(HORIZONTAL_RUNS, " ")
            .replaceAll(AROUND_NEWLINE, "\n")
            .replaceAll(BLANK_LINES, "\n\n")
            .trimWhitespaceEdges()
    }
}

/** Caches the parsed description per source string so scrolling does not re-parse it. */
@Composable
fun rememberHtmlDescription(html: String?): AnnotatedString =
    remember(html) { HtmlDescription.render(html) }

/**
 * Regex replacement that keeps styles attached to the right characters: rebuilding through
 * [buildAnnotatedString] re-offsets every span and link annotation as the text shifts.
 */
private fun AnnotatedString.replaceAll(pattern: Regex, replacement: String): AnnotatedString {
    val source = this
    val matches = pattern.findAll(source.text).toList()
    if (matches.isEmpty()) return source

    return buildAnnotatedString {
        var cursor = 0
        matches.forEach { match ->
            if (match.range.first > cursor) {
                append(source.subSequence(cursor, match.range.first))
            }
            append(replacement)
            cursor = match.range.last + 1
        }
        if (cursor < source.length) {
            append(source.subSequence(cursor, source.length))
        }
    }
}

private fun AnnotatedString.trimWhitespaceEdges(): AnnotatedString {
    var start = 0
    var end = length
    while (start < end && text[start].isWhitespace()) start++
    while (end > start && text[end - 1].isWhitespace()) end--
    return if (start == 0 && end == length) this else subSequence(start, end)
}
