package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppThemeMode
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.finance.FinanceScreen
import com.example.ui.navigation.AppScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.pockets.PocketsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val themeMode by viewModel.selectedThemeMode.collectAsStateWithLifecycle()
            val systemIsDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> systemIsDark
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: AppViewModel = viewModel()
) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.DASHBOARD) }

    // Handle back button to return to Dashboard if on other screens
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive layout for tablet/desktop with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AppScreen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_rail_${screen.name.lowercase()}")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    ScreenContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        onNavigateToTab = { currentScreen = it }
                    )
                }
            }
        } else {
            // Mobile layout with NavigationBar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("main_bottom_nav"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        AppScreen.entries.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.testTag("nav_bar_item_${screen.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        onNavigateToTab = { currentScreen = it }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    currentScreen: AppScreen,
    viewModel: AppViewModel,
    onNavigateToTab: (AppScreen) -> Unit
) {
    when (currentScreen) {
        AppScreen.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToTab = onNavigateToTab
        )
        AppScreen.FINANCE -> FinanceScreen(
            viewModel = viewModel
        )
        AppScreen.POCKETS -> PocketsScreen(
            viewModel = viewModel
        )
        AppScreen.NOTES -> NotesScreen(
            viewModel = viewModel
        )
        AppScreen.SETTINGS -> SettingsScreen(
            viewModel = viewModel
        )
    }
}
