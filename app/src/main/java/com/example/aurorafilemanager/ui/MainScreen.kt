package com.example.aurorafilemanager.ui

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.aurorafilemanager.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aurorafilemanager.data.FileManager
import com.example.aurorafilemanager.model.FileItem
import com.example.aurorafilemanager.model.SortBy
import com.example.aurorafilemanager.model.SortOrder
import com.example.aurorafilemanager.model.ViewMode
import com.example.aurorafilemanager.viewmodel.FileExplorerViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FileExplorerViewModel) {
    val context = LocalContext.current
    val currentDirectory by viewModel.currentDirectory.collectAsStateWithLifecycle()
    val files by viewModel.files.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val showHidden by viewModel.showHidden.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val clipboard by viewModel.clipboard.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val isCheckingUpdates by viewModel.isCheckingUpdates.collectAsStateWithLifecycle()

    val currentVersion = "1.0.3"

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(currentVersion)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FileItem?>(null) }
    var propertiesTarget by remember { mutableStateOf<FileItem?>(null) }
    var showSystemDeleteWarning by remember { mutableStateOf(false) }
    var understoodRisk by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var showSortMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    var hasAccess by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    DisposableEffect(context) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    hasAccess = Environment.isExternalStorageManager()
                }
            }
        }
        val lifecycleOwner = context as? LifecycleOwner
        lifecycleOwner?.lifecycle?.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(lifecycleObserver)
        }
    }

    val storageInfo = remember(currentDirectory) { FileManager.getStorageInfo(currentDirectory) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Aurora File Manager",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()

                // Storage summary
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.storage), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { storageInfo.third },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${FileItem.formatFileSize(storageInfo.second)} free of ${FileItem.formatFileSize(storageInfo.first)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                HorizontalDivider()

                Text(stringResource(R.string.quick_access), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelMedium)
                viewModel.quickAccessFolders.forEach { (name, file) ->
                    NavigationDrawerItem(
                        icon = { Icon(getQuickAccessIcon(name), contentDescription = null) },
                        label = { Text(name) },
                        selected = currentDirectory == file,
                        onClick = {
                            viewModel.navigateTo(file)
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                    label = { Text(if (showHidden) stringResource(R.string.hide_hidden) else stringResource(R.string.show_hidden)) },
                    selected = false,
                    onClick = { viewModel.toggleShowHidden() }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text(stringResource(R.string.about_app)) },
                    selected = false,
                    onClick = {
                        showAboutDialog = true
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null) },
                    label = { Text(if (isCheckingUpdates) stringResource(R.string.checking_updates) else stringResource(R.string.check_for_updates)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.checkForUpdates(currentVersion) { updateFound ->
                            if (!updateFound) {
                                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.no_updates_found)) }
                            }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        if (selectedItems.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.items_selected, selectedItems.size),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                                singleLine = true,
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search))
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        } else {
                            Text(
                                text = currentDirectory.name.ifEmpty { "/" },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectedItems.isNotEmpty()) {
                                viewModel.clearSelection()
                            } else if (isSearchActive) {
                                isSearchActive = false
                                viewModel.setSearchQuery("")
                            } else if (currentDirectory.parentFile != null && currentDirectory.parentFile!!.exists()) {
                                viewModel.navigateUp()
                            } else {
                                scope.launch { drawerState.open() }
                            }
                        }) {
                            Icon(
                                imageVector = if (selectedItems.isNotEmpty() || isSearchActive) {
                                    Icons.AutoMirrored.Filled.ArrowBack
                                } else if (currentDirectory.parentFile == null) {
                                    Icons.Default.Menu
                                } else {
                                    Icons.AutoMirrored.Filled.ArrowBack
                                },
                                contentDescription = "Back / Menu"
                            )
                        }
                    },
                    actions = {
                        if (selectedItems.isNotEmpty()) {
                            val allSelected = files.isNotEmpty() && selectedItems.size == files.size
                            IconButton(onClick = {
                                if (allSelected) {
                                    viewModel.clearSelection()
                                } else {
                                    viewModel.selectAll()
                                }
                            }) {
                                Icon(
                                    imageVector = if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = stringResource(if (allSelected) R.string.deselect_all else R.string.select_all)
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) {
                                    viewModel.setSearchQuery("")
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_name)) },
                                    onClick = { viewModel.setSortBy(SortBy.NAME); showSortMenu = false },
                                    trailingIcon = { if (sortBy == SortBy.NAME) Icon(if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_size)) },
                                    onClick = { viewModel.setSortBy(SortBy.SIZE); showSortMenu = false },
                                    trailingIcon = { if (sortBy == SortBy.SIZE) Icon(if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_date)) },
                                    onClick = { viewModel.setSortBy(SortBy.DATE); showSortMenu = false },
                                    trailingIcon = { if (sortBy == SortBy.DATE) Icon(if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_type)) },
                                    onClick = { viewModel.setSortBy(SortBy.TYPE); showSortMenu = false },
                                    trailingIcon = { if (sortBy == SortBy.TYPE) Icon(if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null) }
                                )
                            }
                            IconButton(onClick = {
                                viewModel.setViewMode(if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST)
                            }) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = "Toggle View Mode"
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (selectedItems.isNotEmpty()) {
                    BottomAppBar(
                        actions = {
                            IconButton(onClick = {
                                viewModel.copySelected(isCut = false)
                                scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                            IconButton(onClick = {
                                viewModel.copySelected(isCut = true)
                                scope.launch { snackbarHostState.showSnackbar("Cut to clipboard") }
                            }) {
                                Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                            }
                            IconButton(onClick = {
                                val protectedMsg = context.getString(R.string.protected_android_folder_message)
                                val hasProtectedFiles = selectedItems.any { FileManager.isProtectedDirectory(it.file) }
                                val hasSystemFiles = selectedItems.any { FileManager.isSystemFile(it.file) }
                                if (hasProtectedFiles) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(protectedMsg)
                                    }
                                } else if (hasSystemFiles) {
                                    showSystemDeleteWarning = true
                                } else {
                                    viewModel.deleteSelected { success, blocked ->
                                        scope.launch {
                                            val msg = when {
                                                blocked -> protectedMsg
                                                success -> "Deleted selected items"
                                                else -> "Failed to delete some items"
                                            }
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                            if (selectedItems.size == 1) {
                                IconButton(onClick = { renameTarget = selectedItems.first() }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename")
                                }
                                IconButton(onClick = { propertiesTarget = selectedItems.first() }) {
                                    Icon(Icons.Default.Info, contentDescription = "Properties")
                                }
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                            }
                        }
                    )
                } else if (clipboard != null) {
                    val count = clipboard!!.first.size
                    val label = if (count == 1) clipboard!!.first.first().name else "$count items"
                    BottomAppBar(
                        actions = {
                            Text("Clipboard: $label", modifier = Modifier.padding(start = 16.dp))
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                viewModel.paste { success ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(if (success) "Pasted successfully" else "Failed to paste items")
                                    }
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (selectedItems.isEmpty() && clipboard == null) {
                    var showFabMenu by remember { mutableStateOf(false) }
                    Column(horizontalAlignment = Alignment.End) {
                        if (showFabMenu) {
                            ExtendedFloatingActionButton(
                                text = { Text("New Folder") },
                                icon = { Icon(Icons.Default.CreateNewFolder, null) },
                                onClick = { showCreateFolderDialog = true; showFabMenu = false },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExtendedFloatingActionButton(
                                text = { Text("New File") },
                                icon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, null) },
                                onClick = { showCreateFileDialog = true; showFabMenu = false },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
                            Icon(if (showFabMenu) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Permission warning banner if All Files Access is missing on Android 11+
                if (!hasAccess && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "All Files Access required to browse storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    context.startActivity(intent)
                                }
                            }) {
                                Text("Grant")
                            }
                        }
                    }
                }

                // Breadcrumb bar
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = currentDirectory.absolutePath,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Access Filter Chips Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.quickAccessFolders, key = { it.first }) { (name, file) ->
                        FilterChip(
                            selected = currentDirectory == file,
                            onClick = { viewModel.navigateTo(file) },
                            label = { Text(name, style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getQuickAccessIcon(name),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                if (files.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val message = if (isSearchActive && searchQuery.isNotBlank()) {
                            "No files match \"$searchQuery\""
                        } else if (!FileManager.canReadDirectory(currentDirectory)) {
                            "Permission denied / Restricted folder access"
                        } else {
                            "Directory is empty"
                        }
                        Text(message, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    if (viewMode == ViewMode.LIST) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(files, key = { it.path }) { item ->
                                FileListItem(
                                    item = item,
                                    isSelected = selectedItems.contains(item),
                                    onClick = {
                                        if (selectedItems.isNotEmpty()) {
                                            viewModel.toggleSelection(item)
                                        } else if (item.isDirectory) {
                                            viewModel.navigateTo(item.file)
                                        } else {
                                            openFile(context, item.file) { errorMsg ->
                                                scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelection(item)
                                    }
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(files, key = { it.path }) { item ->
                                FileGridItem(
                                    item = item,
                                    isSelected = selectedItems.contains(item),
                                    onClick = {
                                        if (selectedItems.isNotEmpty()) {
                                            viewModel.toggleSelection(item)
                                        } else if (item.isDirectory) {
                                            viewModel.navigateTo(item.file)
                                        } else {
                                            openFile(context, item.file) { errorMsg ->
                                                scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelection(item)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateFolderDialog) {
        val defaultFolderName = stringResource(R.string.default_untitled_folder)
        var folderName by remember { mutableStateOf(defaultFolderName) }
        val isBlank = folderName.isBlank()

        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(stringResource(R.string.new_folder)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text(stringResource(R.string.new_folder)) },
                        singleLine = true,
                        isError = isBlank,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isBlank) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.error_folder_name_blank),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            val name = folderName
                            showCreateFolderDialog = false
                            viewModel.createDirectory(name) { success ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(if (success) "Folder created" else "Failed to create folder")
                                }
                            }
                        }
                    },
                    enabled = !isBlank
                ) { Text(stringResource(R.string.create)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showCreateFileDialog) {
        val defaultFileName = stringResource(R.string.default_untitled_file)
        var fileName by remember { mutableStateOf(defaultFileName) }
        val isBlank = fileName.isBlank()

        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            title = { Text(stringResource(R.string.new_file)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text(stringResource(R.string.new_file)) },
                        singleLine = true,
                        isError = isBlank,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isBlank) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.error_file_name_blank),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fileName.isNotBlank()) {
                            val name = fileName
                            showCreateFileDialog = false
                            viewModel.createFile(name) { success ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(if (success) "File created" else "Failed to create file")
                                }
                            }
                        }
                    },
                    enabled = !isBlank
                ) { Text(stringResource(R.string.create)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (renameTarget != null) {
        var newName by remember(renameTarget) { mutableStateOf(renameTarget!!.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        val target = renameTarget!!
                        val name = newName
                        renameTarget = null
                        viewModel.renameItem(target, name) { success ->
                            scope.launch {
                                snackbarHostState.showSnackbar(if (success) "Renamed successfully" else "Failed to rename")
                            }
                        }
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (propertiesTarget != null) {
        val target = propertiesTarget!!
        val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { propertiesTarget = null },
            title = { Text("Properties") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Name: ${target.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("Path: ${target.path}", style = MaterialTheme.typography.bodySmall)
                    Text("Type: ${if (target.isDirectory) "Directory" else target.extension.uppercase()}", style = MaterialTheme.typography.bodySmall)
                    Text("Size: ${target.readableSize}", style = MaterialTheme.typography.bodySmall)
                    Text("Modified: ${dateFormat.format(Date(target.lastModified))}", style = MaterialTheme.typography.bodySmall)
                    Text("Readable: ${target.file.canRead()} | Writable: ${target.file.canWrite()}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { propertiesTarget = null }) { Text("Close") }
            }
        )
    }

    if (showSystemDeleteWarning) {
        AlertDialog(
            onDismissRequest = {
                showSystemDeleteWarning = false
                understoodRisk = false
            },
            title = { Text("Warning: Core System File") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You are about to delete a core system file. This may cause complications and break your system.")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = understoodRisk,
                            onCheckedChange = { understoodRisk = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "I understand the risk and I am doing this at my own risk. Aurora is not responsible",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSystemDeleteWarning = false
                        understoodRisk = false
                        viewModel.deleteSelected { success, blocked ->
                            scope.launch {
                                val msg = when {
                                    blocked -> context.getString(R.string.protected_android_folder_message)
                                    success -> "Deleted system items"
                                    else -> "Failed to delete system items"
                                }
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    },
                    enabled = understoodRisk,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete at Risk")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSystemDeleteWarning = false
                    understoodRisk = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.changelog_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• " + stringResource(R.string.changelog_samsung_fix), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("• " + stringResource(R.string.changelog_minor_fixes), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Welcome & Permission Onboarding Splash Screen", style = MaterialTheme.typography.bodyMedium)
                    Text("• Complete Spanish Language Support", style = MaterialTheme.typography.bodyMedium)
                    Text("• One-Tap Quick Access Folder Filter Chips", style = MaterialTheme.typography.bodyMedium)
                    Text("• Select All & Live Multi-Selection Counter", style = MaterialTheme.typography.bodyMedium)
                    Text("• Batch Copy, Cut, and Paste Operations", style = MaterialTheme.typography.bodyMedium)
                    Text("• One-Tap Search Clear & Jetpack Compose BackHandler", style = MaterialTheme.typography.bodyMedium)
                    Text("• StatFs Physical Storage Calculation Fix", style = MaterialTheme.typography.bodyMedium)
                    Text("• Root Shell (su) Fallback Directory Engine", style = MaterialTheme.typography.bodyMedium)
                    Text("• FileProvider Intent Chooser with MIME Detection", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (updateInfo != null) {
        val info = updateInfo!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateDialog() },
            title = { Text("${stringResource(R.string.update_available)}: ${info.latestVersionTag}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(info.releaseNotes, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            scope.launch { snackbarHostState.showSnackbar("Cannot open link") }
                        }
                        viewModel.dismissUpdateDialog()
                    }
                ) {
                    Text(stringResource(R.string.download_update))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                    Text(stringResource(R.string.later))
                }
            }
        )
    }
}

fun openFile(context: android.content.Context, file: File, onError: (String) -> Unit) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mimeType = getMimeType(file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("", uri)
        }

        val chooserIntent = Intent.createChooser(intent, "Open ${file.name}").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        // Fallback attempt with text/plain or generic type if activity resolution failed
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("", uri)
            }
            val chooserIntent = Intent.createChooser(fallbackIntent, "Open ${file.name}").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e2: Exception) {
            onError("Cannot open file: ${e2.localizedMessage ?: e.localizedMessage ?: "No application found to open this file"}")
        }
    }
}

fun getMimeType(file: File): String {
    val extension = file.extension.lowercase(Locale.getDefault())
    if (extension.isNotEmpty()) {
        try {
            val mimeFromMap = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mimeFromMap != null) return mimeFromMap
        } catch (_: Exception) {
            // Ignored if MimeTypeMap is unmocked in tests
        }
    }
    return when (extension) {
        "txt", "log", "json", "xml", "md", "csv", "tsv", "conf", "ini", "properties",
        "sh", "py", "kt", "java", "c", "cpp", "h", "js", "html", "css", "sql",
        "yaml", "yml", "gradle", "kts", "" -> "text/plain"
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg" -> "image/*"
        "mp4", "mkv", "avi", "mov", "webm", "3gp" -> "video/*"
        "mp3", "wav", "flac", "aac", "ogg", "m4a" -> "audio/*"
        "zip", "rar", "7z", "tar", "gz" -> "application/zip"
        "pdf" -> "application/pdf"
        "apk" -> "application/vnd.android.package-archive"
        else -> "*/*"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(item.lastModified))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getFileIcon(item),
                contentDescription = null,
                tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$dateStr • ${item.readableSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    item: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .padding(4.dp)
            .size(110.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getFileIcon(item),
                contentDescription = null,
                tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun getFileIcon(item: FileItem): ImageVector {
    if (item.isDirectory) return Icons.Default.Folder
    return when (item.extension) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp" -> Icons.Default.Image
        "mp4", "mkv", "avi", "mov", "webm" -> Icons.Default.Movie
        "mp3", "wav", "flac", "aac", "ogg" -> Icons.Default.AudioFile
        "zip", "rar", "7z", "tar", "gz" -> Icons.Default.FolderZip
        "pdf" -> Icons.Default.PictureAsPdf
        "txt", "md", "log", "json", "xml" -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}

fun getQuickAccessIcon(name: String): ImageVector {
    return when (name) {
        "Root Directory" -> Icons.Default.FolderSpecial
        "Internal Storage" -> Icons.Default.PhoneAndroid
        "Documents" -> Icons.Default.Description
        "Downloads" -> Icons.Default.Download
        "Pictures" -> Icons.Default.Image
        "Photos / DCIM" -> Icons.Default.PhotoCamera
        "Videos" -> Icons.Default.Movie
        "Music" -> Icons.Default.AudioFile
        else -> Icons.Default.Folder
    }
}
