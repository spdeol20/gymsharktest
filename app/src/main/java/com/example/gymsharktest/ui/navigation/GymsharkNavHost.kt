package com.example.gymsharktest.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymsharktest.ui.detail.ProductDetailScreen
import com.example.gymsharktest.ui.products.ProductListScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProductListRoute

/**
 * Only the id crosses the navigation boundary. Passing a whole product through arguments would
 * bloat the saved state and let the detail screen render data the repository no longer holds.
 */
@Serializable
data class ProductDetailRoute(val productId: Long)

private const val TRANSITION_MILLIS = 220

@Composable
fun GymsharkNavHost() {
    val navController = rememberNavController()
    val enter = fadeIn(tween(TRANSITION_MILLIS))
    val exit = fadeOut(tween(TRANSITION_MILLIS))

    NavHost(
        navController = navController,
        startDestination = ProductListRoute,
        enterTransition = { enter },
        exitTransition = { exit },
        popEnterTransition = { enter },
        popExitTransition = { exit },
    ) {
        composable<ProductListRoute> {
            ProductListScreen(
                onProductClick = { productId ->
                    navController.navigate(ProductDetailRoute(productId))
                },
            )
        }

        composable<ProductDetailRoute> {
            ProductDetailScreen(onBackClick = navController::navigateUp)
        }
    }
}
