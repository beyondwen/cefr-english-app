package com.wenha.cefrenglish.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.wenha.cefrenglish.ui.home.HomeScreen
import com.wenha.cefrenglish.ui.home.HomeViewModel
import com.wenha.cefrenglish.ui.lesson.LessonScreen
import com.wenha.cefrenglish.ui.lesson.LessonViewModel
import com.wenha.cefrenglish.ui.navigation.AppRoute
import com.wenha.cefrenglish.ui.navigation.startDestination
import com.wenha.cefrenglish.ui.placement.PlacementScreen
import com.wenha.cefrenglish.ui.placement.PlacementViewModel
import com.wenha.cefrenglish.ui.progress.ProgressScreen
import com.wenha.cefrenglish.ui.progress.ProgressViewModel
import com.wenha.cefrenglish.ui.common.AppBlue
import com.wenha.cefrenglish.ui.common.AppGreen
import com.wenha.cefrenglish.ui.common.AppInk
import com.wenha.cefrenglish.ui.common.AppLine
import com.wenha.cefrenglish.ui.common.AppMuted
import com.wenha.cefrenglish.ui.common.AppSky

@Composable
fun CefrEnglishApp() {
    val context = LocalContext.current
    val container = remember { AppContainer(context) }
    val navController = rememberNavController()
    var userId by remember { mutableStateOf("") }
    var selectedLessonLevel by remember { mutableStateOf<String?>(null) }
    var selectedLessonModuleIndex by remember { mutableStateOf<Int?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        userId = container.getUserId()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentRoute != AppRoute.Placement.route) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onHome = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Home.route)
                            launchSingleTop = true
                        }
                    },
                    onLesson = {
                        selectedLessonLevel = null
                        selectedLessonModuleIndex = null
                        navController.navigate(AppRoute.Lesson.route) {
                            launchSingleTop = true
                        }
                    },
                    onProgress = {
                        navController.navigate(AppRoute.Progress.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Home.route) {
                val viewModel = viewModel<HomeViewModel> {
                    HomeViewModel(container.progressRepository, container.syllabusRepository)
                }
                LaunchedEffect(userId) {
                    viewModel.refresh(userId)
                }
                HomeScreen(
                    viewModel = viewModel,
                    onStartModule = { level, moduleIndex ->
                        selectedLessonLevel = level
                        selectedLessonModuleIndex = moduleIndex
                        navController.navigate(AppRoute.Lesson.route)
                    },
                    onContinueLearning = {
                        selectedLessonLevel = null
                        selectedLessonModuleIndex = null
                        navController.navigate(AppRoute.Lesson.route) {
                            launchSingleTop = true
                        }
                    },
                    onStartPlacement = {
                        navController.navigate(AppRoute.Placement.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppRoute.Placement.route) {
                val viewModel = viewModel<PlacementViewModel> {
                    PlacementViewModel(container.placementRepository)
                }
                PlacementScreen(viewModel = viewModel) {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable(AppRoute.Lesson.route) {
                val viewModel = viewModel<LessonViewModel> {
                    LessonViewModel(container.lessonRepository)
                }
                LessonScreen(
                    viewModel = viewModel,
                    userId = userId,
                    selectedLevel = selectedLessonLevel,
                    selectedModuleIndex = selectedLessonModuleIndex,
                    onContinue = { navController.navigate(AppRoute.Progress.route) },
                )
            }
            composable(AppRoute.Progress.route) {
                val viewModel = viewModel<ProgressViewModel> {
                    ProgressViewModel(container.progressRepository)
                }
                ProgressScreen(
                    viewModel = viewModel,
                    userId = userId,
                    onRefresh = {},
                )
            }
        }
    }
}

private data class BottomTabSpec(
    val route: String,
    val label: String,
    val mark: String,
    val color: Color,
    val onClick: () -> Unit,
)

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onHome: () -> Unit,
    onLesson: () -> Unit,
    onProgress: () -> Unit,
) {
    val tabs = listOf(
        BottomTabSpec(AppRoute.Home.route, "首页", "首", AppBlue, onHome),
        BottomTabSpec(AppRoute.Lesson.route, "课程", "学", AppSky, onLesson),
        BottomTabSpec(AppRoute.Progress.route, "进度", "✓", AppGreen, onProgress),
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                BottomTab(
                    modifier = Modifier.weight(1f),
                    tab = tab,
                    selected = currentRoute == tab.route,
                )
            }
        }
    }
}

@Composable
private fun BottomTab(
    modifier: Modifier,
    tab: BottomTabSpec,
    selected: Boolean,
) {
    val container = if (selected) tab.color.copy(alpha = 0.12f) else Color.Transparent
    val contentColor = if (selected) tab.color else AppMuted
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = tab.onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) tab.color else Color(0xFFF3F6FA)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = tab.mark,
                color = if (selected) Color.White else AppMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = tab.label,
            color = if (selected) AppInk else contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}
