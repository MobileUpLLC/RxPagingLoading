package ru.mobileup.rxplce

import me.dmdev.rxpm.PresentationModel

interface PagingPm<T> {

    val pagingState: PresentationModel.State<PagingState<T>>
    val refreshes: PresentationModel.Action<Unit>
    val loadNextPage: PresentationModel.Action<Unit>

    data class PagingState<T>(
        val data: List<T>? = null,
        val refreshingError: Throwable? = null,
        val pageLoadingError: Throwable? = null,
        val refreshing: Boolean = false,
        val pageIsLoading: Boolean = false,
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