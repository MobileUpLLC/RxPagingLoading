package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxplce.Loading.Action
import ru.mobileup.rxplce.Loading.State

/**
 * Simple [implementation][Loading] of the data loader.
 *
 * @param[source] the source of data.
 */
class LoadingOrdinary<T>(
    source: Single<T>
) : Loading<T> {

    private val stateSubject = BehaviorSubject.createDefault<State<T>>(State()).toSerialized()
    private val actionSubject = PublishSubject.create<Action>().toSerialized()

    override val state: Observable<State<T>>
    override val actions: Consumer<Action> = Consumer { actionSubject.onNext(it) }

    init {

        state = actionSubject
            .withLatestFrom(
                stateSubject,
                BiFunction { _: Action, state: State<T> -> state }
            )
            .filter { !it.loading }
            .switchMap {
                source
                    .toObservable()
                    .map<InternalAction> { InternalAction.LoadSuccess(it) }
                    .startWith(InternalAction.LoadStart)
                    .onErrorReturn { InternalAction.LoadFail(it) }
            }
            .scan(State<T>()) { state, action ->
                when (action) {
                    is InternalAction.LoadStart -> {
                        state.copy(
                            loading = true,
                            error = null
                        )
                    }
                    is InternalAction.LoadSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            loading = false,
                            content = action.data as T
                        )
                    }
                    is InternalAction.LoadFail -> {
                        state.copy(
                            loading = false,
                            error = action.error
                        )
                    }
                }
            }
            .doOnNext { stateSubject.onNext(it) }
            .share()
    }

    private sealed class InternalAction {
        object LoadStart : InternalAction()
        class LoadSuccess<T>(val data: T) : InternalAction()
        class LoadFail(val error: Throwable) : InternalAction()
    }
}