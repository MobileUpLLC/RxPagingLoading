package ru.mobileup.rxplce

data class ScreenState<T>(
    val data: T?,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val refreshEnabled: Boolean,
    val contentIsVisible: Boolean,
    val emptyViewIsVisible: Boolean,
    val errorViewIsVisible: Boolean
)