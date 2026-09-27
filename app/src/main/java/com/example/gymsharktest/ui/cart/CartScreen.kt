package com.example.gymsharktest.ui.cart

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymsharktest.R
import com.example.gymsharktest.model.BasketQuantity
import com.example.gymsharktest.model.CartLine
import com.example.gymsharktest.ui.components.MessageState
import com.example.gymsharktest.ui.components.ProductImageView
import com.example.gymsharktest.ui.components.rememberPriceFormatter
import com.example.gymsharktest.ui.detail.BackButton

private val CardShape = RoundedCornerShape(24.dp)
private val ImageShape = RoundedCornerShape(18.dp)
private val BarShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onProductClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    CartScreen(
        lines = lines,
        onBackClick = onBackClick,
        onProductClick = onProductClick,
        onQuantityChange = viewModel::setQuantity,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    lines: List<CartLine>,
    onBackClick: () -> Unit,
    onProductClick: (Long) -> Unit,
    onQuantityChange: (Long, String?, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = rememberPriceFormatter()
    val itemCount = lines.sumOf { it.quantity }
    val subtotal = lines.sumOf { line -> line.product.price.amountMinorUnits * line.quantity }
    var checkoutOpen by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.cart_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (lines.isNotEmpty()) {
                CheckoutBar(
                    itemCount = itemCount,
                    subtotal = formatter.format(subtotal),
                    onCheckout = { checkoutOpen = true },
                )
            }
        },
    ) { innerPadding ->
        if (lines.isEmpty()) {
            MessageState(
                title = stringResource(R.string.cart_empty_title),
                body = stringResource(R.string.cart_empty_body),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = lines,
                    key = { line -> "${line.product.id}:${line.size.orEmpty()}" },
                ) { line ->
                    DismissibleCartLine(
                        line = line,
                        lineTotal = formatter.format(line.product.price.amountMinorUnits * line.quantity),
                        onClick = { onProductClick(line.product.id) },
                        onDecrease = {
                            onQuantityChange(line.product.id, line.size, line.quantity - 1)
                        },
                        onIncrease = {
                            onQuantityChange(line.product.id, line.size, line.quantity + 1)
                        },
                        onRemove = { onQuantityChange(line.product.id, line.size, 0) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    if (checkoutOpen) {
        ModalBottomSheet(
            onDismissRequest = { checkoutOpen = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            CheckoutSheet(
                lines = lines,
                total = formatter.format(subtotal),
                onClose = { checkoutOpen = false },
            )
        }
    }
}

@Composable
private fun CheckoutBar(
    itemCount: Int,
    subtotal: String,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BarShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(R.plurals.cart_items, itemCount, itemCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.cart_subtotal),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = subtotal,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        CheckoutButton(
            label = stringResource(R.string.action_checkout),
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    }
}

@Composable
private fun CheckoutSheet(
    lines: List<CartLine>,
    total: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.checkout_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = total,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            lines.forEach { line ->
                CheckoutLine(line = line)
            }
        }
        Text(
            text = stringResource(R.string.checkout_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CheckoutButton(
            label = stringResource(R.string.action_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CheckoutLine(line: CartLine, modifier: Modifier = Modifier) {
    val detail = line.size?.takeIf { it.isNotBlank() }?.let { size ->
        stringResource(R.string.checkout_line_size, size, line.quantity)
    } ?: stringResource(R.string.checkout_line_qty, line.quantity)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = line.product.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun CheckoutButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleCartLine(
    line: CartLine,
    lineTotal: String,
    onClick: () -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            val dismissed = value == SwipeToDismissBoxValue.EndToStart
            if (dismissed) onRemove()
            dismissed
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = stringResource(R.string.action_remove),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        },
    ) {
        CartLineCard(
            line = line,
            lineTotal = lineTotal,
            onClick = onClick,
            onDecrease = onDecrease,
            onIncrease = onIncrease,
            onRemove = onRemove,
        )
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    lineTotal: String,
    onClick: () -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ProductImageView(
            image = line.product.featuredImage,
            contentDescription = null,
            fallbackLabel = line.product.title,
            modifier = Modifier
                .width(96.dp)
                .height(128.dp)
                .clip(ImageShape)
                .clickable(onClick = onClick),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = line.product.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onClick),
            )
            val colour = line.product.colour
            if (!colour.isNullOrBlank()) {
                Text(
                    text = colour,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val size = line.size
                if (!size.isNullOrBlank()) {
                    Text(
                        text = size,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                CartStepper(
                    quantity = line.quantity,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val removeDescription = stringResource(R.string.action_remove_from_basket)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onRemove)
                        .clearAndSetSemantics {
                            role = Role.Button
                            contentDescription = removeDescription
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                AnimatedContent(
                    targetState = lineTotal,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "line-total",
                ) { total ->
                    Text(
                        text = total,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@Composable
private fun CartStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CartStep(
            symbol = "\u2212",
            contentDescription = stringResource(R.string.action_decrease_quantity),
            enabled = quantity > BasketQuantity.MIN,
            onClick = onDecrease,
        )
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(28.dp),
        )
        CartStep(
            symbol = "+",
            contentDescription = stringResource(R.string.action_increase_quantity),
            enabled = quantity < BasketQuantity.MAX,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun CartStep(
    symbol: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
