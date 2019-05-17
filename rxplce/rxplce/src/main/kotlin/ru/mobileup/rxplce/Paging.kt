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
        val loadingError: Throwable? = null,
        val pageLoading: Boolean = false,
        val pageLoadingError: Throwable? = null,
        val lastPage: Page<T>? = null
    ) {
        val isReachedEnd: Boolean get() = lastPage?.isReachedEnd ?: false
    }

    interface Page<T> {
        val list: List<T>
        val lastItem: T? get() = list.lastOrNull()
        val isReachedEnd: Boolean
    }
}