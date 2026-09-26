package com.example.gymsharktest.ui.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.gymsharktest.core.html.RichText
import com.example.gymsharktest.core.html.RichTextStyle

/** Adapts the framework-free [RichText] produced by the HTML parser to Compose. */
fun RichText.toAnnotatedString(): AnnotatedString = AnnotatedString(
    text = text,
    spanStyles = spans.map { AnnotatedString.Range(it.style.toSpanStyle(), it.start, it.end) },
)

private fun RichTextStyle.toSpanStyle(): SpanStyle = when (this) {
    RichTextStyle.Bold -> SpanStyle(fontWeight = FontWeight.Bold)
    RichTextStyle.Italic -> SpanStyle(fontStyle = FontStyle.Italic)
    RichTextStyle.Underline -> SpanStyle(textDecoration = TextDecoration.Underline)
}
