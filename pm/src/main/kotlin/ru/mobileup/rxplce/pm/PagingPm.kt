package ru.mobileup.rxplce.pm

import me.dmdev.rxpm.Action
import me.dmdev.rxpm.Command
import me.dmdev.rxpm.State

interface PagingPm<T> {

    val content: State<List<T>>

    val isLoading: State<Boolean>
    val isRefreshing: State<Boolean>
    val pageIsLoading: State<Boolean>
    val pageErrorVisible: State<Boolean>

    val refreshEnabled: State<Boolean>

    val contentViewVisible: State<Boolean>
    val emptyViewVisible: State<Boolean>
    val errorViewVisible: State<Boolean>

    val scrollToTop: Command<Unit>

    val refreshAction: Action<Unit>
    val retryAction: Action<Unit>
    val nextPageAction: Action<Unit>
    val retryNextPageAction: Action<Unit>
}

