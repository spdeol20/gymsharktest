package com.example.gymsharktest.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.gymsharktest.core.format.PriceFormatter
import com.example.gymsharktest.model.Price

@Composable
fun rememberPriceFormatter(): PriceFormatter = remember { PriceFormatter() }

/**
 * Current price, plus the struck-through original when the product is discounted. The compare-at
 * price is hidden from accessibility because a screen reader announcing two prices in sequence is
 * more confusing than useful; the current price carries the meaning.
 */
@Composable
fun PriceRow(
    price: Price,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val formatter = rememberPriceFormatter()
    val compareAt = formatter.formatCompareAt(price)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatter.format(price),
            style = style,
            fontWeight = FontWeight.SemiBold,
            color = if (price.isDiscounted) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        if (compareAt != null) {
            Text(
                text = compareAt,
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}
