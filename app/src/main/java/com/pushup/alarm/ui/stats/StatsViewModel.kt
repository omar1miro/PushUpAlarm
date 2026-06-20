package com.pushup.alarm.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushup.alarm.data.model.DailyStats
import com.pushup.alarm.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val todayPushUps: Int = 0,
    val todayAlarmsCompleted: Int = 0,
    val currentStreak: Int = 0,
    val totalPushUps: Int = 0,
    val recentStats: List<DailyStats> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        statsRepository.getTodayStats(),
        statsRepository.getRecentStats(30),
        statsRepository.getAllStats()
    ) { today, recent, all ->
        StatsUiState(
            todayPushUps = today?.totalPushUps ?: 0,
            todayAlarmsCompleted = today?.alarmsCompleted ?: 0,
            currentStreak = calculateStreak(recent),
            totalPushUps = all.sumOf { it.totalPushUps },
            recentStats = recent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    private fun calculateStreak(stats: List<DailyStats>): Int {
        var streak = 0
        for (day in stats.sortedByDescending { it.date }) {
            if (day.streakDay) streak++ else break
        }
        return streak
    }
}
