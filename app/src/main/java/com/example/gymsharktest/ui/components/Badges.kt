package com.example.gymsharktest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymsharktest.R
import com.example.gymsharktest.model.ProductLabel

private val BadgeShape = RoundedCornerShape(4.dp)

@Composable
fun Badge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(BadgeShape)
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
fun LabelBadge(label: ProductLabel, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    // Sale is the only label that earns the error colour; the rest stay monochrome so a product
    // carrying several labels does not turn into a traffic light.
    val container = if (label is ProductLabel.Sale) scheme.error else scheme.onSurface
    val content = if (label is ProductLabel.Sale) scheme.onError else scheme.surface

    Badge(
        text = label.displayText(),
        containerColor = container,
        contentColor = content,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelBadgeRow(
    labels: List<ProductLabel>,
    modifier: Modifier = Modifier,
    maxLabels: Int = Int.MAX_VALUE,
) {
    if (labels.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        labels.take(maxLabels).forEach { label -> LabelBadge(label) }
    }
}

/**
 * Unknown labels render their own text because the vocabulary is an open set; the known ones use
 * translated strings.
 */
@Composable
fun ProductLabel.displayText(): String = when (this) {
    ProductLabel.New -> stringResource(R.string.label_new)
    ProductLabel.ComingSoon -> stringResource(R.string.label_coming_soon)
    ProductLabel.BackInStock -> stringResource(R.string.label_back_in_stock)
    ProductLabel.Sale -> stringResource(R.string.label_sale)
    ProductLabel.GoingFast -> stringResource(R.string.label_going_fast)
    ProductLabel.LimitedEdition -> stringResource(R.string.label_limited_edition)
    ProductLabel.Popular -> stringResource(R.string.label_popular)
    ProductLabel.RecycledNylon -> stringResource(R.string.label_recycled_nylon)
    ProductLabel.RecycledPolyester -> stringResource(R.string.label_recycled_polyester)
    is ProductLabel.Unknown -> text
}
