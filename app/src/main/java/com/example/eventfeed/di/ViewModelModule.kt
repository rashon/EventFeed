package com.example.eventfeed.di

import com.example.eventfeed.ui.events.list.EventListVM
import com.example.eventfeed.ui.events.detail.EventDetailsVM
import com.example.eventfeed.ui.login.LoginVM
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginVM)
    viewModelOf(::EventListVM)
    viewModelOf(::EventDetailsVM)
}