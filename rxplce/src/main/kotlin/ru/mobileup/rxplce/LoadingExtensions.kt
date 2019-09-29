package ru.mobileup.rxplce

import io.reactivex.Observable

fun <T> Loading<T>.contentChanges(): Observable<T> {
    return state
        .filter { it.content != null }
        .map { it.content!! }
        .distinctUntilChanged { l1, l2 -> l1 === l2 }
}

fun <T> Loading<T>.loadingChanges(): Observable<Boolean> {
    return state
        .map { it.loading }
        .distinctUntilChanged()
}

fun <T> Loading<T>. errorChanges(): Observable<Throwable> {
    return state
        .filter { it.error != null }
        .map { it.error!! }
        .distinctUntilChanged()
}

fun <T> Loading<T>.contentVisible(): Observable<Boolean> {
    return state.map {
        it.content != null && contentIsEmpty(it.content).not()
    }.distinctUntilChanged()
}

fun <T> Loading<T>.emptyVisible(): Observable<Boolean> {
    return state.map {
        it.content != null && contentIsEmpty(it.content)
    }.distinctUntilChanged()
}

fun <T> Loading<T>.errorVisible(): Observable<Boolean> {
    return state.map {
        it.content == null && it.error != null
    }.distinctUntilChanged()
}

fun <T> Loading<T>.isLoading(): Observable<Boolean> {
    return state.map {
        it.content == null && it.loading
    }.distinctUntilChanged()
}

fun <T> Loading<T>.isRefreshing(): Observable<Boolean> {
    return state.map {
        it.content != null && it.loading
    }.distinctUntilChanged()
}

fun <T> Loading<T>.refreshEnabled(): Observable<Boolean> {
    return isLoading()
        .map { it.not() }
        .distinctUntilChanged()
}