package ru.mobileup.rxplce.pm

data class ScreenState<T>(
    val content: T?,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val refreshEnabled: Boolean,
    val contentViewVisible: Boolean,
    val emptyViewVisible: Boolean,
    val errorViewVisible: Boolean
)