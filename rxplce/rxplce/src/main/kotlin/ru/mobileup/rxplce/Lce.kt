package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.functions.Consumer

interface Lce<T> {

    enum class Action { REFRESH }

    val state: Observable<DataState<T>>
    val actions: Consumer<Action>

    interface DataMaybeEmpty {
        fun isEmpty(): Boolean
    }

    data class DataState<T>(
        val data: T? = null,
        val refreshingError: Throwable? = null,
        val refreshing: Boolean = false
    ) {

        fun dataIsEmpty(): Boolean {
            return when (data) {
                is Collection<*> -> data.isEmpty()
                is Array<*> -> data.isEmpty()
                is DataMaybeEmpty -> data.isEmpty()
                else -> false
            }
        }
    }
}