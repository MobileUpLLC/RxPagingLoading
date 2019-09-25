package ru.mobileup.rxplce.pm

import me.dmdev.rxpm.Action
import me.dmdev.rxpm.State

interface LoadingPm<T> {

    val content: State<T>

    val isLoading: State<Boolean>
    val isRefreshing: State<Boolean>

    val refreshEnabled: State<Boolean>

    val contentViewVisible: State<Boolean>
    val emptyViewVisible: State<Boolean>
    val errorViewVisible: State<Boolean>

    val refreshAction: Action<Unit>
    val retryAction: Action<Unit>
}