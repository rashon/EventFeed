package com.example.eventfeed.ui.events

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun EventDetailScreen(eventId: String) {
    Text(text = "Details for event with ID: $eventId")
}