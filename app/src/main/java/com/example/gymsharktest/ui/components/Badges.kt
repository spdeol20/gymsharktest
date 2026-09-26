package com.example.gymsharktest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymsharktest.R
import com.example.gymsharktest.model.ProductLabel

@Composable
fun Badge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (outlined) {
                    Modifier.border(width = 1.dp, color = contentColor, shape = CircleShape)
                } else {
                    Modifier.background(containerColor)
                },
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
fun LabelBadge(
    label: ProductLabel,
    modifier: Modifier = Modifier,
    filled: Boolean = label.prefersFilledPill,
) {
    val scheme = MaterialTheme.colorScheme
    val sale = label is ProductLabel.Sale
    Badge(
        text = label.displayText(),
        containerColor = if (sale) scheme.error else scheme.primary,
        contentColor = when {
            !filled -> scheme.onSurface
            sale -> scheme.onError
            else -> scheme.onPrimary
        },
        outlined = !filled,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelBadgeRow(
    labels: List<ProductLabel>,
    modifier: Modifier = Modifier,
    maxLabels: Int = Int.MAX_VALUE,
    filled: Boolean = false,
) {
    if (labels.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.take(maxLabels).forEach { label ->
            LabelBadge(
                label = label,
                filled = filled || label.prefersFilledPill,
            )
        }
    }
}

/** New and Sale read as the primary mark; everything else stays an outline on a light surface. */
private val ProductLabel.prefersFilledPill: Boolean
    get() = this is ProductLabel.New || this is ProductLabel.Sale

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
