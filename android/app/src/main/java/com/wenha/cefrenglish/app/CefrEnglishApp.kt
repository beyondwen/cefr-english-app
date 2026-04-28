package com.wenha.cefrenglish.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == AppRoute.Home.route,
                        onClick = {
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(AppRoute.Home.route)
                                launchSingleTop = true
                            }
                        },
                        label = { Text("首页") },
                        icon = {},
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppRoute.Lesson.route,
                        onClick = {
                            selectedLessonLevel = null
                            selectedLessonModuleIndex = null
                            navController.navigate(AppRoute.Lesson.route) {
                                launchSingleTop = true
                            }
                        },
                        label = { Text("今日课程") },
                        icon = {},
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppRoute.Progress.route,
                        onClick = {
                            navController.navigate(AppRoute.Progress.route) {
                                launchSingleTop = true
                            }
                        },
                        label = { Text("进度") },
                        icon = {},
                    )
                }
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
