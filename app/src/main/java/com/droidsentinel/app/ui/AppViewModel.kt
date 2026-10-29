package com.droidsentinel.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.droidsentinel.app.DroidSentinelApp
import com.droidsentinel.app.data.history.ScanHistoryEntity
import com.droidsentinel.app.data.history.ScanHistoryMapper
import com.droidsentinel.app.domain.advisor.AdvisorResponse
import com.droidsentinel.app.domain.model.AppScanSummary
import com.droidsentinel.app.domain.model.DatabaseStatus
import com.droidsentinel.app.domain.model.InstalledAppInfo
import com.droidsentinel.app.domain.model.ScanProgress
import com.droidsentinel.app.domain.model.ScanResult
import com.droidsentinel.app.domain.model.ScannerAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val scanning: Boolean = false,
    val progress: ScanProgress? = null,
    val lastResult: ScanResult? = null,
    val availability: ScannerAvailability,
    val database: DatabaseStatus,
    val error: String? = null,
    val advisor: AdvisorResponse? = null,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as DroidSentinelApp).container

    private val _home = MutableStateFlow(
        HomeUiState(
            availability = container.scanner.availability(),
            database = container.databaseRepository.status(),
        ),
    )
    val home: StateFlow<HomeUiState> = _home

    private val _apps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val apps: StateFlow<List<InstalledAppInfo>> = _apps

    private val _selectedApp = MutableStateFlow<AppScanSummary?>(null)
    val selectedApp: StateFlow<AppScanSummary?> = _selectedApp

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val history: StateFlow<List<ScanHistoryEntity>> = container.history.dao().observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            runCatching { container.databaseRepository.load() }
            refreshApps()
            _home.value = _home.value.copy(
                availability = container.scanner.availability(),
                database = container.databaseRepository.status(),
            )
        }
        viewModelScope.launch {
            container.databaseRepository.statusFlow.collect { status ->
                _home.value = _home.value.copy(
                    database = status,
                    availability = container.scanner.availability(),
                )
            }
        }
        viewModelScope.launch {
            container.coordinator.progress.collect { progress ->
                _home.value = _home.value.copy(progress = progress)
            }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _apps.value = container.installedApps.listInstalledApps()
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun scanDevice() {
        if (_home.value.scanning) return
        viewModelScope.launch {
            _home.value = _home.value.copy(scanning = true, error = null)
            val result = runCatching { container.coordinator.scanInstalledApps() }
                .onFailure { _home.value = _home.value.copy(error = it.message, scanning = false) }
                .getOrNull() ?: return@launch
            persist(result)
            _home.value = _home.value.copy(
                scanning = false,
                lastResult = result,
                advisor = container.advisor.explain(result),
                availability = container.scanner.availability(),
            )
        }
    }

    fun scanApk(uri: Uri) {
        if (_home.value.scanning) return
        viewModelScope.launch {
            _home.value = _home.value.copy(scanning = true, error = null)
            val result = runCatching { container.coordinator.scanApk(uri) }
                .onFailure { _home.value = _home.value.copy(error = it.message, scanning = false) }
                .getOrNull() ?: return@launch
            persist(result)
            _home.value = _home.value.copy(
                scanning = false,
                lastResult = result,
                advisor = container.advisor.explain(result),
            )
        }
    }

    fun cancelScan() {
        container.coordinator.cancel()
    }

    fun updateDatabase() {
        viewModelScope.launch {
            _home.value = _home.value.copy(error = null)
            runCatching { container.databaseRepository.update { } }
                .onFailure { _home.value = _home.value.copy(error = it.message) }
            _home.value = _home.value.copy(
                availability = container.scanner.availability(),
                database = container.databaseRepository.status(),
            )
        }
    }

    fun openApp(packageName: String) {
        val fromScan = _home.value.lastResult?.appSummaries?.firstOrNull { it.app.packageName == packageName }
        if (fromScan != null) {
            _selectedApp.value = fromScan
            return
        }
        val app = container.installedApps.getApp(packageName) ?: return
        val findings = container.metadataEngine.analyze(app)
        _selectedApp.value = AppScanSummary(
            app = app,
            signature = com.droidsentinel.app.domain.model.SignatureScanOutcome(
                status = com.droidsentinel.app.domain.model.SignatureScanStatus.NOT_SCANNED,
                message = "This app has not been signature-scanned in the current session.",
            ),
            findings = findings,
            metadataRisk = container.metadataEngine.highestSeverity(findings),
        )
    }

    fun explainSelected(): AdvisorResponse? = _selectedApp.value?.let { container.advisor.explain(it) }

    private suspend fun persist(result: ScanResult) {
        container.history.dao().upsert(ScanHistoryMapper.toEntity(result))
    }
}
