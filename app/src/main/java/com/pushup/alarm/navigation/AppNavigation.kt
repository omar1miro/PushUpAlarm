package com.pushup.alarm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pushup.alarm.ui.alarm.AlarmListScreen
import com.pushup.alarm.ui.createalarm.CreateAlarmScreen
import com.pushup.alarm.ui.stats.StatsScreen

object Routes {
    const val ALARM_LIST = "alarm_list"
    const val CREATE_ALARM = "create_alarm?alarmId={alarmId}"
    const val STATS = "stats"

    fun createAlarm(alarmId: Long? = null): String {
        return if (alarmId != null) "create_alarm?alarmId=$alarmId" else "create_alarm"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ALARM_LIST
    ) {
        composable(Routes.ALARM_LIST) {
            AlarmListScreen(
                onCreateAlarm = { navController.navigate(Routes.createAlarm()) },
                onEditAlarm = { id -> navController.navigate(Routes.createAlarm(id)) },
                onStatsClick = { navController.navigate(Routes.STATS) }
            )
        }

        composable(
            route = Routes.CREATE_ALARM,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            CreateAlarmScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
