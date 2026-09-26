package com.example.gymsharktest.core.html

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlDescriptionParserTest {

    @Test
    fun `null html produces empty rich text`() {
        val result = HtmlDescriptionParser.parse(null)

        assertTrue(result.isEmpty)
        assertTrue(result.spans.isEmpty())
    }

    @Test
    fun `blank html produces empty rich text`() {
        assertTrue(HtmlDescriptionParser.parse("").isEmpty)
        assertTrue(HtmlDescriptionParser.parse("   \n  ").isEmpty)
    }

    @Test
    fun `plain paragraph keeps its text and drops the markup`() {
        val result = HtmlDescriptionParser.parse("<p>Run with it</p>")

        assertEquals("Run with it", result.text)
    }

    @Test
    fun `strong becomes a bold span covering exactly that text`() {
        val result = HtmlDescriptionParser.parse("<p><strong>RUN WITH IT</strong></p>")

        assertEquals("RUN WITH IT", result.text)
        assertEquals(listOf(RichTextSpan(RichTextStyle.Bold, 0, 11)), result.spans)
    }

    @Test
    fun `bold span is positioned correctly when preceded by text`() {
        val result = HtmlDescriptionParser.parse("<p>Fit: <b>compressive</b></p>")

        assertEquals("Fit: compressive", result.text)
        assertEquals(listOf(RichTextSpan(RichTextStyle.Bold, 5, 16)), result.spans)
        assertEquals("compressive", result.text.substring(5, 16))
    }

    @Test
    fun `em and i both map to italic, u maps to underline`() {
        val result = HtmlDescriptionParser.parse("<p><em>a</em><i>b</i><u>c</u></p>")

        assertEquals("abc", result.text)
        assertEquals(
            listOf(
                RichTextSpan(RichTextStyle.Italic, 0, 1),
                RichTextSpan(RichTextStyle.Italic, 1, 2),
                RichTextSpan(RichTextStyle.Underline, 2, 3),
            ),
            result.spans,
        )
    }

    @Test
    fun `br becomes a single newline`() {
        val result = HtmlDescriptionParser.parse("<p>High-waisted<br>Compressive fit</p>")

        assertEquals("High-waisted\nCompressive fit", result.text)
    }

    @Test
    fun `separate paragraphs are divided by a blank line`() {
        val result = HtmlDescriptionParser.parse("<p>One</p><p>Two</p>")

        assertEquals("One\n\nTwo", result.text)
    }

    @Test
    fun `empty paragraphs do not stack blank lines`() {
        val result = HtmlDescriptionParser.parse("<p>One</p><p> </p><p><br></p><p>Two</p>")

        assertEquals("One\n\nTwo", result.text)
    }

    @Test
    fun `list items are rendered as bullets on their own lines`() {
        val result = HtmlDescriptionParser.parse("<ul><li>One</li><li>Two</li></ul>")

        assertEquals("\u2022 One\n\u2022 Two", result.text)
    }

    @Test
    fun `meta tags inside the body are discarded`() {
        val result = HtmlDescriptionParser.parse("""<meta charset="utf-8">
<p><strong>RUN WITH IT</strong></p>""")

        assertEquals("RUN WITH IT", result.text)
        assertEquals(listOf(RichTextSpan(RichTextStyle.Bold, 0, 11)), result.spans)
    }

    @Test
    fun `script content is dropped entirely rather than rendered as text`() {
        val result = HtmlDescriptionParser.parse("<p>Hi</p><script>alert('xss')</script>")

        assertEquals("Hi", result.text)
        assertFalse(result.text.contains("alert"))
    }

    @Test
    fun `style content is dropped entirely`() {
        val result = HtmlDescriptionParser.parse("<style>.a{color:red}</style><p>Hi</p>")

        assertEquals("Hi", result.text)
    }

    @Test
    fun `the google translate widget left behind by the cms is removed`() {
        val result = HtmlDescriptionParser.parse(
            """<p>Text</p>
<div id="gtx-trans" style="position: absolute; left: 44px;">
<div class="gtx-trans-icon"></div>
</div>""",
        )

        assertEquals("Text", result.text)
    }

    @Test
    fun `unknown elements contribute their text but no styling`() {
        val result = HtmlDescriptionParser.parse(
            """<p>Model is <span class="TextRun SCXP103297068 BCX0"><span class="NormalTextRun">5'3" and wears a size M</span></span></p>""",
        )

        assertEquals("""Model is 5'3" and wears a size M""", result.text)
        assertTrue(result.spans.isEmpty())
    }

    @Test
    fun `html entities and non breaking spaces are decoded and collapsed`() {
        val result = HtmlDescriptionParser.parse("<p>Tom &amp; Jerry&nbsp;run</p>")

        assertEquals("Tom & Jerry run", result.text)
    }

    @Test
    fun `runs of whitespace collapse to a single space`() {
        val result = HtmlDescriptionParser.parse("<p>Lots\n\n   of     space</p>")

        assertEquals("Lots of space", result.text)
    }

    @Test
    fun `unclosed tags are tolerated`() {
        val result = HtmlDescriptionParser.parse("<p><strong>Bold")

        assertEquals("Bold", result.text)
        assertEquals(listOf(RichTextSpan(RichTextStyle.Bold, 0, 4)), result.spans)
    }

    @Test
    fun `text without any markup is returned as-is`() {
        val result = HtmlDescriptionParser.parse("Just text")

        assertEquals("Just text", result.text)
    }

    @Test
    fun `every span stays within the bounds of the final text`() {
        val result = HtmlDescriptionParser.parse(REAL_DESCRIPTION)

        result.spans.forEach { span ->
            assertTrue("span start out of bounds: $span", span.start in 0..result.text.length)
            assertTrue("span end out of bounds: $span", span.end in 0..result.text.length)
            assertTrue("empty span: $span", span.end > span.start)
        }
    }

    @Test
    fun `a real payload description renders as readable text with no markup left over`() {
        val result = HtmlDescriptionParser.parse(REAL_DESCRIPTION)

        assertFalse("raw markup leaked into the output", result.text.contains('<'))
        assertFalse("raw markup leaked into the output", result.text.contains('>'))
        assertFalse("editor noise leaked into the output", result.text.contains("data-mce"))
        assertTrue(result.text.startsWith("RUN WITH IT"))
        assertTrue(result.text.contains("- Full length legging"))
        assertTrue(result.text.contains("""5'3" and wears a size M"""))
        assertTrue(result.text.contains("- SKU: B3A3E-UBCY"))
        assertEquals(listOf(RichTextSpan(RichTextStyle.Bold, 0, 11)), result.spans)
    }

    private companion object {
        /** Trimmed from the Speed Leggings entry in the live payload, editor noise intact. */
        val REAL_DESCRIPTION = """<meta charset="utf-8">
<p data-mce-fragment="1"><strong data-mce-fragment="1">RUN WITH IT</strong></p>
<p data-mce-fragment="1"><br data-mce-fragment="1">Your run requires enduring comfort and support.</p>
<p data-mce-fragment="1"> <br data-mce-fragment="1"></p>
<p data-mce-fragment="1"><br data-mce-fragment="1">- Full length legging<br data-mce-fragment="1">- High-waisted<br data-mce-fragment="1">- Model is <meta charset="utf-8"><span data-mce-fragment="1" lang="EN-GB" class="TextRun SCXP103297068 BCX0"><span data-mce-fragment="1" class="NormalTextRun SCXP103297068 BCX0">5'3" and wears a size M</span></span><br data-mce-fragment="1">- SKU: B3A3E-UBCY</p>"""
    }
}
