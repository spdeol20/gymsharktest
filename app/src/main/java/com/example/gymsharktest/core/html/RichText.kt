package com.example.gymsharktest.core.html

/**
 * Plain-text rendering of an HTML fragment plus the formatting that should be applied to it.
 *
 * Kept free of Compose and Android types so the parser that produces it can be tested as an
 * ordinary JVM unit test; `RichText.toAnnotatedString()` adapts it at the UI boundary.
 */
data class RichText(
    val text: String,
    val spans: List<RichTextSpan> = emptyList(),
) {
    val isEmpty: Boolean get() = text.isEmpty()

    companion object {
        val Empty = RichText("")
    }
}

data class RichTextSpan(
    val style: RichTextStyle,
    val start: Int,
    val end: Int,
)

enum class RichTextStyle {
    Bold,
    Italic,
    Underline,
}
