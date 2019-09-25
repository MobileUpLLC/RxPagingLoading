package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.functions.Consumer

/**
 *
 * This interface describes [input][actions] and [output][state] of a state machine.
 * It is used to implement a data loader with ordinary states: loading, content, error (LCE).
 *
 * @see LoadingAssembled
 * @see LoadingOrdinary
 */
interface Loading<T> {

    /**
     * UI-events or intentions to change a state of data.
     */
    enum class Action { REFRESH }

    /**
     * The observer of changing [state][State].
     */
    val state: Observable<State<T>>

    /**
     * Consumer of [actions][Action].
     */
    val actions: Consumer<Action>

    /**
     * The LCE state.
     *
     * @property[content] loaded data.
     * @property[loading] indicates that data is loading or updating.
     * @property[error] an error occurred when loading or updating data.
     */
    data class State<T>(
        val content: T? = null,
        val loading: Boolean = false,
        val error: Throwable? = null
    )

    fun contentChanges(): Observable<T> {
        return state
            .filter { it.content != null }
            .map { it.content!! }
            .distinctUntilChanged { l1, l2 -> l1 === l2 }
    }

    fun loadingChanges(): Observable<Boolean> {
        return state
            .map { it.loading }
            .distinctUntilChanged()
    }

    fun errorChanges(): Observable<Throwable> {
        return state
            .filter { it.error != null }
            .map { it.error!! }
            .distinctUntilChanged()
    }
}