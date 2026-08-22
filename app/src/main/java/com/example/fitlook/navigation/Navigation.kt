package com.example.fitlook.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitlook.ui.screens.AddOutfitScreen
import com.example.fitlook.ui.screens.HomeScreen
import com.example.fitlook.ui.screens.OutfitDetailScreen
import com.example.fitlook.ui.screens.ProfileScreen
import com.example.fitlook.ui.screens.SavedScreen
import com.example.fitlook.ui.screens.SplashScreen
import com.example.fitlook.ui.screens.TrendingScreen
import com.example.fitlook.ui.theme.CoralAccent
import com.example.fitlook.ui.theme.DarkCard
import com.example.fitlook.ui.theme.DeepBlack
import com.example.fitlook.ui.theme.LightGray
import com.example.fitlook.ui.theme.RoseGold

object Routes {
    const val SPLASH = "splash"
    const val MAIN = "main"
    const val DISCOVER = "discover"
    const val TRENDING = "trending"
    const val SAVED = "saved"
    const val PROFILE = "profile"
    const val ADD_OUTFIT = "add_outfit"
    const val OUTFIT_DETAIL = "detail/{outfitId}"

    fun outfitDetail(outfitId: String) = "detail/$outfitId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
    val selectedIconRes: Int? = null,
    val unselectedIconRes: Int? = null
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.DISCOVER, "Discover", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home),
    BottomNavItem(Routes.TRENDING, "Trending", selectedIconRes = com.example.fitlook.R.drawable.ic_fire_filled, unselectedIconRes = com.example.fitlook.R.drawable.ic_fire),
    BottomNavItem(Routes.SAVED, "Saved", selectedIcon = Icons.Filled.Favorite, unselectedIcon = Icons.Outlined.FavoriteBorder),
    BottomNavItem(Routes.PROFILE, "Profile", selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
)

@Composable
fun FitLookNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) }
    ) {
        // Splash
        composable(
            route = Routes.SPLASH,
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(500)) }
        ) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Main screen with bottom nav
        composable(route = Routes.MAIN) {
            MainScreenWithBottomNav(
                onOutfitClick = { outfitId ->
                    navController.navigate(Routes.outfitDetail(outfitId))
                },
                onAddOutfitClick = {
                    navController.navigate(Routes.ADD_OUTFIT)
                }
            )
        }

        // Add Outfit screen
        composable(
            route = Routes.ADD_OUTFIT,
            enterTransition = {
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) +
                    fadeIn(tween(400))
            },
            popExitTransition = {
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(350)) +
                    fadeOut(tween(200))
            }
        ) {
            AddOutfitScreen(
                onBackClick = { navController.popBackStack() },
                onOutfitAdded = {
                    navController.popBackStack()
                }
            )
        }

        // Outfit detail
        composable(
            route = Routes.OUTFIT_DETAIL,
            arguments = listOf(navArgument("outfitId") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350)) +
                    fadeIn(tween(350))
            },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350)) +
                    fadeOut(tween(200))
            }
        ) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getString("outfitId") ?: ""
            OutfitDetailScreen(
                outfitId = outfitId,
                onBackClick = { navController.popBackStack() },
                onOutfitClick = { similarId ->
                    navController.navigate(Routes.outfitDetail(similarId))
                }
            )
        }
    }
}

@Composable
private fun MainScreenWithBottomNav(
    onOutfitClick: (String) -> Unit,
    onAddOutfitClick: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DeepBlack,
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val iconRes = if (selected) item.selectedIconRes else item.unselectedIconRes
                            val iconVector = if (selected) item.selectedIcon else item.unselectedIcon
                            if (iconRes != null) {
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = item.label
                                )
                            } else if (iconVector != null) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = item.label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoseGold,
                            selectedTextColor = RoseGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray,
                            indicatorColor = DarkCard
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddOutfitClick,
                shape = CircleShape,
                containerColor = RoseGold,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Outfit",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.DISCOVER,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(150)) }
        ) {
            composable(Routes.DISCOVER) {
                HomeScreen(onOutfitClick = onOutfitClick)
            }
            composable(Routes.TRENDING) {
                TrendingScreen(onOutfitClick = onOutfitClick)
            }
            composable(Routes.SAVED) {
                SavedScreen(onOutfitClick = onOutfitClick)
            }
            composable(Routes.PROFILE) {
                ProfileScreen()
            }
        }
    }
}
