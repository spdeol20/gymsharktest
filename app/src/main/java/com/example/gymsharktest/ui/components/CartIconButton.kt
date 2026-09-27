package com.example.gymsharktest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gymsharktest.R

private const val BADGE_CAP = 99

@Composable
fun CartIconButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    onPositioned: (Rect) -> Unit = {},
) {
    val description = if (count == 0) {
        stringResource(R.string.action_open_basket)
    } else {
        pluralStringResource(R.plurals.basket_count, count, count)
    }
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge {
                    Text(text = if (count > BADGE_CAP) "99+" else count.toString())
                }
            }
        },
        modifier = modifier
            .padding(end = 12.dp)
            .onGloballyPositioned { onPositioned(it.boundsInWindow()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
