package com.example.eventfeed.ui.events.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onOpen: (Long) -> Unit,
    onProfileClicked: () -> Unit,
    viewModel: EventListVM = koinViewModel(),
    scrollToPosition: Int = 0
) {

    val events by viewModel.events.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    LaunchedEffect(listState, viewModel) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                // Get the first visible item index
                val firstIndex = visibleItems.firstOrNull()?.index ?: 0
                val size = visibleItems.size

                val focusedItem = firstIndex + size / 2

                focusedItem / viewModel.pageSize
            }
            .distinctUntilChanged()
            .collect { pageIndex ->
                viewModel.updateCurrentPage(pageIndex)
            }
    }

    LaunchedEffect(scrollToPosition, listState) {
        if (scrollToPosition > 0) {
            try {
                listState.requestScrollToItem(
                    scrollToPosition - 1
                )
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TopAppBar(
                    title = { Text("Events") },
                    actions = {
                        // connectivity indicator
                        Icon(
                            imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.Cloud,
                            contentDescription = if (isOffline) "Offline" else "Online",
                            tint = if (isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(24.dp)
                        )

                        // refresh button (shows tiny progress while refreshing)
                        IconButton(onClick = {
                            scope.launch { viewModel.refresh() }
                        }) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }

                        // profile button
                        IconButton(onClick = { onProfileClicked() }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isOffline) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        "Offline - showing cached content",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (error != null && events.isEmpty()) {
                Text(
                    text = error ?: "Unknown error",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (events.isEmpty() && !isLoading && error == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No events")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {

                    itemsIndexed(events) { index, e ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpen(e.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {

                            Column(Modifier.padding(16.dp)) {

                                Text(
                                    e.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    e.description ?: "",
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (index == events.lastIndex) {
                            viewModel.loadMore()
                        }
                    }

                    item {
                        if (isLoading && events.isNotEmpty()) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}