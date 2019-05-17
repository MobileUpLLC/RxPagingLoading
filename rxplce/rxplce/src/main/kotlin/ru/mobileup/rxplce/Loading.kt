package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.functions.Consumer

interface Loading<T> {

    enum class Action { REFRESH }

    val state: Observable<State<T>>
    val actions: Consumer<Action>

    data class State<T>(
        val content: T? = null,
        val loading: Boolean = false,
        val error: Throwable? = null
    )
}