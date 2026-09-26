package com.example.gymsharktest.ui.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Runs under Robolectric because `AnnotatedString.fromHtml` delegates to `android.text.Html`,
 * which needs an Android runtime. The SDK is pinned because Robolectric ships one runtime per API
 * level and lags new releases, so it cannot necessarily run against this project's compileSdk.
 *
 * Assertions about line breaks deliberately count non-blank lines rather than matching exact
 * newline runs: the framework's choice of one or two newlines between blocks is an implementation
 * detail, whereas "these two things are on separate lines" is the behaviour that matters.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HtmlDescriptionTest {

    @Test
    fun `null html renders as empty`() {
        assertEquals(0, HtmlDescription.render(null).length)
    }

    @Test
    fun `blank html renders as empty`() {
        assertEquals(0, HtmlDescription.render("").length)
        assertEquals(0, HtmlDescription.render("   \n  ").length)
    }

    @Test
    fun `markup-only html renders as empty`() {
        assertEquals(0, HtmlDescription.render("<p></p><div></div>").length)
    }

    @Test
    fun `a plain paragraph keeps its text and drops the markup`() {
        assertEquals("Run with it", HtmlDescription.render("<p>Run with it</p>").text)
    }

    @Test
    fun `text without any markup is returned as-is`() {
        assertEquals("Just text", HtmlDescription.render("Just text").text)
    }

    @Test
    fun `strong becomes bold over exactly that text`() {
        val result = HtmlDescription.render("<p><strong>RUN WITH IT</strong></p>")

        assertEquals("RUN WITH IT", result.text)
        assertEquals("RUN WITH IT", result.boldText())
    }

    @Test
    fun `bold is applied to the right characters when preceded by other text`() {
        val result = HtmlDescription.render("<p>Fit: <b>compressive</b></p>")

        assertEquals("Fit: compressive", result.text)
        assertEquals("compressive", result.boldText())
    }

    @Test
    fun `em becomes italic and u becomes underline`() {
        val result = HtmlDescription.render("<p><em>slanted</em> and <u>underlined</u></p>")

        assertEquals("slanted and underlined", result.text)
        assertEquals("slanted", result.italicText())
        assertEquals("underlined", result.underlinedText())
    }

    @Test
    fun `br puts the following text on a new line`() {
        val result = HtmlDescription.render("<p>High-waisted<br>Compressive fit</p>")

        assertEquals(listOf("High-waisted", "Compressive fit"), result.nonBlankLines())
    }

    @Test
    fun `separate paragraphs land on separate lines`() {
        val result = HtmlDescription.render("<p>One</p><p>Two</p>")

        assertEquals(listOf("One", "Two"), result.nonBlankLines())
    }

    @Test
    fun `filler paragraphs do not open up gaps of blank lines`() {
        val result = HtmlDescription.render(
            """<p>One</p><p> </p><p><br data-mce-fragment="1"></p><p>Two</p>""",
        )

        assertEquals(listOf("One", "Two"), result.nonBlankLines())
        assertFalse("blank lines were left stacked", result.text.contains("\n\n\n"))
    }

    @Test
    fun `list items land on their own lines`() {
        val result = HtmlDescription.render("<ul><li>One</li><li>Two</li></ul>")

        assertEquals(listOf("One", "Two"), result.nonBlankLines())
    }

    @Test
    fun `script contents are removed rather than rendered as visible text`() {
        val result = HtmlDescription.render("<p>Hi</p><script>alert('xss')</script>")

        assertEquals("Hi", result.text)
        assertFalse(result.text.contains("alert"))
    }

    @Test
    fun `stylesheet contents are removed rather than rendered as visible text`() {
        val result = HtmlDescription.render("<style>.a{color:red}</style><p>Hi</p>")

        assertEquals("Hi", result.text)
        assertFalse(result.text.contains("color"))
    }

    @Test
    fun `meta tags inside the body are discarded`() {
        val result = HtmlDescription.render(
            """<meta charset="utf-8">
<p><strong>RUN WITH IT</strong></p>""",
        )

        assertEquals("RUN WITH IT", result.text)
        assertEquals("RUN WITH IT", result.boldText())
    }

    @Test
    fun `the translate widget left behind by the cms is removed`() {
        val result = HtmlDescription.render(
            """<p>Text</p>
<div id="gtx-trans" style="position: absolute; left: 44px;">
<div class="gtx-trans-icon"></div>
</div>""",
        )

        assertEquals("Text", result.text)
    }

    @Test
    fun `editor noise contributes text but no styling`() {
        val result = HtmlDescription.render(
            """<p>Model is <span class="TextRun SCXP103297068 BCX0"><span class="NormalTextRun">5'3" and wears a size M</span></span></p>""",
        )

        assertEquals("""Model is 5'3" and wears a size M""", result.text)
        assertTrue("unexpected styling: ${result.spanStyles}", result.spanStyles.isEmpty())
    }

    @Test
    fun `entities and non-breaking spaces are decoded and normalised to plain spaces`() {
        val result = HtmlDescription.render("<p>Tom &amp; Jerry&nbsp;run</p>")

        assertEquals("Tom & Jerry run", result.text)
    }

    @Test
    fun `runs of whitespace collapse to a single space`() {
        val result = HtmlDescription.render("<p>Lots\n\n   of     space</p>")

        assertEquals("Lots of space", result.text)
    }

    @Test
    fun `unclosed tags are tolerated`() {
        val result = HtmlDescription.render("<p><strong>Bold")

        assertEquals("Bold", result.text)
        assertEquals("Bold", result.boldText())
    }

    @Test
    fun `a javascript link is stripped instead of becoming clickable`() {
        val result = HtmlDescription.render("""<p><a href="javascript:alert(1)">Tap</a></p>""")

        assertEquals("Tap", result.text)
        assertTrue(result.getLinkAnnotations(0, result.length).isEmpty())
    }

    @Test
    fun `every style range stays within the bounds of the rendered text`() {
        val result = HtmlDescription.render(REAL_DESCRIPTION)

        result.spanStyles.forEach { span ->
            assertTrue("start out of bounds: $span", span.start in 0..result.length)
            assertTrue("end out of bounds: $span", span.end in 0..result.length)
            assertTrue("empty range: $span", span.end > span.start)
        }
    }

    @Test
    fun `a real payload description renders as readable text with no markup left over`() {
        val result = HtmlDescription.render(REAL_DESCRIPTION)

        assertFalse("raw markup leaked into the output", result.text.contains('<'))
        assertFalse("raw markup leaked into the output", result.text.contains('>'))
        assertFalse("editor noise leaked into the output", result.text.contains("data-mce"))
        assertTrue(result.text.startsWith("RUN WITH IT"))
        assertTrue(result.text.contains("- Full length legging"))
        assertTrue(result.text.contains("""5'3" and wears a size M"""))
        assertTrue(result.text.contains("- SKU: B3A3E-UBCY"))
        assertEquals("RUN WITH IT", result.boldText())
    }

    private fun AnnotatedString.nonBlankLines(): List<String> =
        text.lines().map(String::trim).filter(String::isNotEmpty)

    private fun AnnotatedString.boldText(): String? =
        textStyled { it.fontWeight == FontWeight.Bold }

    private fun AnnotatedString.italicText(): String? =
        textStyled { it.fontStyle == FontStyle.Italic }

    private fun AnnotatedString.underlinedText(): String? =
        textStyled { it.textDecoration == TextDecoration.Underline }

    private fun AnnotatedString.textStyled(predicate: (SpanStyle) -> Boolean): String? =
        spanStyles.firstOrNull { predicate(it.item) }?.let { text.substring(it.start, it.end) }

    private companion object {
        /** Trimmed from the Speed Leggings entry in the live payload, editor noise intact. */
        val REAL_DESCRIPTION = """<meta charset="utf-8">
<p data-mce-fragment="1"><strong data-mce-fragment="1">RUN WITH IT</strong></p>
<p data-mce-fragment="1"><br data-mce-fragment="1">Your run requires enduring comfort and support.</p>
<p data-mce-fragment="1"> <br data-mce-fragment="1"></p>
<p data-mce-fragment="1"><br data-mce-fragment="1">- Full length legging<br data-mce-fragment="1">- High-waisted<br data-mce-fragment="1">- Model is <meta charset="utf-8"><span data-mce-fragment="1" lang="EN-GB" class="TextRun SCXP103297068 BCX0"><span data-mce-fragment="1" class="NormalTextRun SCXP103297068 BCX0">5'3" and wears a size M</span></span><br data-mce-fragment="1">- SKU: B3A3E-UBCY</p>"""
    }
}
