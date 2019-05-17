package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.functions.Consumer

interface Paging<T> {

    enum class Action { REFRESH, LOAD_NEXT_PAGE }

    val state: Observable<PagingState<T>>
    val actions: Consumer<Action>

    data class PagingState<T>(
        val data: List<T>? = null,
        val refreshingError: Throwable? = null,
        val pageLoadingError: Throwable? = null,
        val refreshing: Boolean = false,
        val pageIsLoading: Boolean = false,
        val lastPage: Page<T>? = null
    ) {
        val isReachedEnd: Boolean get() = lastPage?.isReachedEnd ?: false

        fun dataIsEmpty(): Boolean {
            return when (data) {
                is Collection<*> -> data.isEmpty()
                is Array<*> -> data.isEmpty()
                is Lce.DataMaybeEmpty -> data.isEmpty()
                else -> false
            }
        }
    }

    interface Page<T> {
        val list: List<T>
        val lastItem: T? get() = list.lastOrNull()
        val isReachedEnd: Boolean
    }
}