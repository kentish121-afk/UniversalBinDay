package com.universalbinday.app.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.universalbinday.app.data.SettingsRepository
import com.universalbinday.app.model.AppSettings
import com.universalbinday.app.model.Council
import com.universalbinday.app.model.Defaults
import com.universalbinday.app.model.UserBinConfig
import com.universalbinday.app.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class BinViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    private val _settings = MutableStateFlow(AppSettings(bins = Defaults.defaultUserBins()))
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _selectedCouncil = MutableStateFlow<Council?>(null)
    val selectedCouncil: StateFlow<Council?> = _selectedCouncil.asStateFlow()

    init {
        viewModelScope.launch {
            repo.settingsFlow.collect { s ->
                _settings.value = s
                _selectedCouncil.value = Defaults.councils.find { it.id == s.selectedCouncilId }
                AlarmScheduler.scheduleAll(getApplication(), s)
            }
        }
    }

    fun selectCouncil(council: Council) {
        viewModelScope.launch {
            val updated = _settings.value.copy(selectedCouncilId = council.id)
            repo.saveSettings(updated)
        }
    }

    fun updateBin(config: UserBinConfig) {
        viewModelScope.launch {
            repo.updateBin(config)
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val updated = _settings.value.copy(notificationHour = hour, notificationMinute = minute)
            repo.saveSettings(updated)
        }
    }

    /** Returns true if today is a collection day for any enabled bin AND current time >= 17:00 */
    fun canReportMissed(): Boolean {
        val now = Calendar.getInstance()
        if (now.get(Calendar.HOUR_OF_DAY) < 17) return false

        val today = now.get(Calendar.DAY_OF_WEEK)
        return _settings.value.bins.any { bin ->
            bin.enabled && today in bin.daysOfWeek
        }
    }

    fun createReportIntent(): Intent? {
        val council = _selectedCouncil.value ?: return null
        val email = council.reportEmail ?: return null

        val subject = council.reportSubject
        val body = buildString {
            append("Hello,\n\n")
            append("I would like to report that my bin(s) were not collected today.\n\n")
            append("Council: ${council.name}\n")
            append("Date: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.UK).format(java.util.Date())}\n\n")
            append("Thank you.")
        }

        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }
}
