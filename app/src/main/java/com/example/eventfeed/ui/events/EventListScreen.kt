package com.example.eventfeed.ui.events

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun EventListScreen(onOpen: (String) -> Unit) {
    Text(text = "List of Events")
}