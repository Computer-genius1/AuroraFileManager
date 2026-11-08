package com.example.aurorafilemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aurorafilemanager.data.AppUpdateInfo
import com.example.aurorafilemanager.data.FileManager
import com.example.aurorafilemanager.data.UpdateChecker
import com.example.aurorafilemanager.model.FileItem
import com.example.aurorafilemanager.model.SortBy
import com.example.aurorafilemanager.model.SortOrder
import com.example.aurorafilemanager.model.ViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    private val currentVersionCode = 10

    private val lastSeenVersionCode = prefs.getInt("last_seen_version_code", -1)

    private val _hasSeenOnboarding = MutableStateFlow(prefs.getBoolean("has_seen_onboarding", false))
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding

    private val _shouldShowUpdateComplete = MutableStateFlow(
        _hasSeenOnboarding.value && lastSeenVersionCode > 0 && lastSeenVersionCode < currentVersionCode
    )
    val shouldShowUpdateComplete: StateFlow<Boolean> = _shouldShowUpdateComplete

    fun dismissUpdateComplete() {
        prefs.edit().putInt("last_seen_version_code", currentVersionCode).apply()
        _shouldShowUpdateComplete.value = false
    }

    fun completeOnboarding() {
        prefs.edit()
            .putBoolean("has_seen_onboarding", true)
            .putInt("last_seen_version_code", currentVersionCode)
            .apply()
        _hasSeenOnboarding.value = true
    }

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates

    fun checkForUpdates(currentVersionName: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isCheckingUpdates.value = true
            val info = UpdateChecker.checkForUpdates(currentVersionName)
            _isCheckingUpdates.value = false
            if (info != null && info.isUpdateAvailable) {
                _updateInfo.value = info
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    private val defaultDir = FileManager.rootStorage

    private val _currentDirectory = MutableStateFlow(if (defaultDir.exists()) defaultDir else application.filesDir)
    val currentDirectory: StateFlow<File> = _currentDirectory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortBy = MutableStateFlow(SortBy.NAME)
    val sortBy: StateFlow<SortBy> = _sortBy

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode

    private val _showHidden = MutableStateFlow(false)
    val showHidden: StateFlow<Boolean> = _showHidden

    private val _selectedItems = MutableStateFlow<Set<FileItem>>(emptySet())
    val selectedItems: StateFlow<Set<FileItem>> = _selectedItems

    private val _clipboard = MutableStateFlow<Pair<List<File>, Boolean>?>(null)
    val clipboard: StateFlow<Pair<List<File>, Boolean>?> = _clipboard

    private val _refreshTrigger = MutableStateFlow(0L)

    val files: StateFlow<List<FileItem>> = combine(
        _currentDirectory,
        _searchQuery,
        _refreshTrigger,
        combine(_sortBy, _sortOrder, _showHidden) { b, o, h -> Triple(b, o, h) }
    ) { dir, query, _, sortAndHidden ->
        val sortByVal = sortAndHidden.first
        val sortOrderVal = sortAndHidden.second
        val showHiddenVal = sortAndHidden.third

        val rawList = FileManager.listFiles(dir, showHiddenVal)
        val filtered = if (query.isBlank()) {
            rawList
        } else {
            rawList.filter { it.name.contains(query, ignoreCase = true) }
        }

        val sorted = when (sortByVal) {
            SortBy.NAME -> if (sortOrderVal == SortOrder.ASCENDING) {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            } else {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })).reversed()
            }
            SortBy.SIZE -> if (sortOrderVal == SortOrder.ASCENDING) {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.size }))
            } else {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.size })).reversed()
            }
            SortBy.DATE -> if (sortOrderVal == SortOrder.ASCENDING) {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified }))
            } else {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified })).reversed()
            }
            SortBy.TYPE -> if (sortOrderVal == SortOrder.ASCENDING) {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.extension }, { it.name.lowercase() }))
            } else {
                filtered.sortedWith(compareBy({ !it.isDirectory }, { it.extension }, { it.name.lowercase() })).reversed()
            }
        }
        sorted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quickAccessFolders = FileManager.getQuickAccessFolders()

    fun navigateTo(directory: File) {
        if (directory.exists() && (directory.isDirectory || !directory.isFile)) {
            _currentDirectory.value = directory
            clearSelection()
            _searchQuery.value = ""
        }
    }

    fun navigateUp() {
        val parent = _currentDirectory.value.parentFile
        if (parent != null && parent.exists()) {
            navigateTo(parent)
        }
    }

    fun refresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sortBy: SortBy) {
        if (_sortBy.value == sortBy) {
            _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
        } else {
            _sortBy.value = sortBy
            _sortOrder.value = SortOrder.ASCENDING
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleShowHidden() {
        _showHidden.value = !_showHidden.value
    }

    fun toggleSelection(item: FileItem) {
        val current = _selectedItems.value.toMutableSet()
        if (current.contains(item)) {
            current.remove(item)
        } else {
            current.add(item)
        }
        _selectedItems.value = current
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
    }

    fun selectAll() {
        _selectedItems.value = files.value.toSet()
    }

    fun createDirectory(name: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = FileManager.createDirectory(_currentDirectory.value, name)
            refresh()
            onResult(ok)
        }
    }

    fun createFile(name: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = FileManager.createFile(_currentDirectory.value, name)
            refresh()
            onResult(ok)
        }
    }

    fun deleteSelected(onResult: (Boolean, Boolean) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            var allOk = true
            var blockedProtected = false
            for (item in _selectedItems.value) {
                if (FileManager.isProtectedDirectory(item.file)) {
                    blockedProtected = true
                    allOk = false
                    continue
                }
                if (!FileManager.deleteItem(item.file)) {
                    allOk = false
                }
            }
            clearSelection()
            refresh()
            onResult(allOk, blockedProtected)
        }
    }

    fun renameItem(item: FileItem, newName: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = FileManager.renameItem(item.file, newName)
            refresh()
            onResult(ok)
        }
    }

    fun copySelected(isCut: Boolean) {
        val selectedFiles = _selectedItems.value.map { it.file }
        if (selectedFiles.isNotEmpty()) {
            _clipboard.value = Pair(selectedFiles, isCut)
            clearSelection()
        }
    }

    fun paste(onResult: (Boolean) -> Unit = {}) {
        val clip = _clipboard.value ?: return
        val sources = clip.first
        val isCut = clip.second
        val destDir = _currentDirectory.value

        viewModelScope.launch {
            var allOk = true
            for (source in sources) {
                val ok = if (isCut) {
                    FileManager.moveItem(source, destDir)
                } else {
                    FileManager.copyItem(source, destDir)
                }
                if (!ok) allOk = false
            }
            if (isCut && allOk) {
                _clipboard.value = null
            }
            refresh()
            onResult(allOk)
        }
    }
}