// kotlin
package com.example.eventfeed.ui.events.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onProfileClicked: () -> Unit,
    viewModel: EventDetailsVM = koinViewModel(parameters = { parametersOf(eventId) })
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isLoading.collectAsStateWithLifecycle() // reuse loading for refresh indicator
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val event by viewModel.event.collectAsStateWithLifecycle()
    val title by viewModel.eventTitle.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = title, overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.Cloud,
                            contentDescription = if (isOffline) "Offline" else "Online",
                            tint = if (isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(24.dp)
                        )

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
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                            }
                        }

                        IconButton(onClick = { onProfileClicked() }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading && event == null) {
                Text(text = "Loading...", style = MaterialTheme.typography.bodyLarge)
            } else {
                // show more attributes: fall back to the event.toString() if exact fields are unknown

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )

                event?.let {

                    Text(
                        text = "ID: $eventId",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    it.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    it.location?.let { location ->
                        Text(
                            text = "Location: $location",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = "Start Time: ${it.startTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    it.endTime?.let { endTime ->
                        if (endTime != 0L) {
                            Text(
                                text = "End Time: $endTime",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    if (it.isAllDay) {
                        Text(
                            text = "All Day Event: $it.isAllDay",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = "Created At: ${it.createdAt}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}