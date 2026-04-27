package com.wenha.cefrenglish.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    LaunchedEffect(Unit) {
        userId = container.getUserId()
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoute.Home.route) {
            val viewModel = viewModel<HomeViewModel> {
                HomeViewModel(container.progressRepository)
            }
            LaunchedEffect(userId) {
                viewModel.refresh(userId)
            }
            HomeScreen(
                viewModel = viewModel,
                onStartPlacement = { navController.navigate(AppRoute.Placement.route) },
                onContinueLesson = {
                    if (viewModel.uiState.value.todayCompleted) {
                        navController.navigate(AppRoute.Progress.route)
                    } else {
                        navController.navigate(AppRoute.Lesson.route)
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
