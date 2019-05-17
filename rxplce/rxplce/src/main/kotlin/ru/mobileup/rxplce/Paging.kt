package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.functions.Consumer

interface Paging<T> {

    enum class Action { REFRESH, LOAD_NEXT_PAGE }

    val state: Observable<State<T>>
    val actions: Consumer<Action>

    data class State<T>(
        val content: List<T>? = null,
        val loading: Boolean = false,
        val error: Throwable? = null,
        val pageLoading: Boolean = false,
        val pageError: Throwable? = null,
        val lastPage: Page<T>? = null
    ) {
        val isEndReached: Boolean get() = lastPage?.isEndReached ?: false
    }

    interface Page<T> {
        val items: List<T>
        val lastItem: T? get() = items.lastOrNull()
        val isEndReached: Boolean
    }
}