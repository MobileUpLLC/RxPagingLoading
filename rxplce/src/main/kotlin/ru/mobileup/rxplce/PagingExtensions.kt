package ru.mobileup.rxplce

import io.reactivex.Observable

fun <T> Paging<T>.contentChanges(): Observable<List<T>> {
    return state
        .filter { it.content != null }
        .map { it.content!! }
        .distinctUntilChanged { l1, l2 -> l1 === l2 }
}

fun <T> Paging<T>.loadingChanges(): Observable<Boolean> {
    return state
        .map { it.loading }
        .distinctUntilChanged()
}

fun <T> Paging<T>.errorChanges(): Observable<Throwable> {
    return state
        .filter { it.error != null }
        .map { it.error!! }
        .distinctUntilChanged()
}

fun <T> Paging<T>.contentVisible(): Observable<Boolean> {
    return state.map {
        it.content != null && contentIsEmpty(it.content).not()
    }.distinctUntilChanged()
}

fun <T> Paging<T>.emptyVisible(): Observable<Boolean> {
    return state.map {
        it.content != null && contentIsEmpty(it.content)
    }.distinctUntilChanged()
}

fun <T> Paging<T>.errorVisible(): Observable<Boolean> {
    return state.map {
        it.content == null && it.error != null
    }.distinctUntilChanged()
}

fun <T> Paging<T>.isLoading(): Observable<Boolean> {
    return state.map {
        it.content == null && it.loading
    }.distinctUntilChanged()
}

fun <T> Paging<T>.isRefreshing(): Observable<Boolean> {
    return state.map {
        it.content != null && it.loading
    }.distinctUntilChanged()
}

fun <T> Paging<T>.pageIsLoading(): Observable<Boolean> {
    return state
        .map { it.pageLoading }
        .distinctUntilChanged()
}

fun <T> Paging<T>.pagingErrorChanges(): Observable<Throwable> {
    return state
        .filter { it.pagingError != null }
        .map { it.pagingError!! }
        .distinctUntilChanged()
}

fun <T> Paging<T>.pagingErrorVisible(): Observable<Boolean> {
    return state
        .map { it.pagingError != null }
        .distinctUntilChanged()
}

fun <T> Paging<T>.scrollToTop(): Observable<Unit> {
    return contentChanges()
        .scan(emptyList<T>() to emptyList<T>()) { pair, list ->
            pair.second to list
        }
        .map { it.second.size <= it.first.size }
        .filter { it }
        .map { Unit }
}