package ru.mobileup.rxplce

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxplce.Loading.Action
import ru.mobileup.rxplce.Loading.State

/**
 * The data loader [implementation][Loading].
 * Used when there is a [completable][Completable] for refreshing data and a separate stream for observing the same data.
 *
 * @param[refresh] refreshes the data.
 * @param[updates] observer of the data.
 */
class LoadingAssembled<T>(
    refresh: Completable,
    updates: Observable<T>
) : Loading<T> {

    private val stateSubject = BehaviorSubject.createDefault<State<T>>(State()).toSerialized()
    private val actionSubject = PublishSubject.create<Action>().toSerialized()

    override val state: Observable<State<T>>
    override val actions: Consumer<Action> = Consumer { actionSubject.onNext(it) }

    init {

        state = actionSubject
            .withLatestFrom(
                stateSubject,
                BiFunction { action: Action, state: State<T> -> action to state }
            )
            .filter { (action, state) ->
                when (action) {
                    Action.REFRESH -> state.loading.not()
                    Action.FORCE_REFRESH -> true
                }
            }
            .switchMap {
                refresh
                    .toSingleDefault(Unit)
                    .toObservable()
                    .map<InternalAction> { InternalAction.RefreshSuccess }
                    .startWith(InternalAction.RefreshStart)
                    .onErrorReturn { InternalAction.RefreshFail(it) }
            }
            .mergeWith(
                updates.map { InternalAction.UpdateData(it) }
            )
            .observeOn(AndroidSchedulers.mainThread())
            .scan(State<T>()) { state, action ->
                when (action) {
                    is InternalAction.RefreshStart -> {
                        state.copy(
                            loading = true,
                            error = null
                        )
                    }
                    is InternalAction.RefreshSuccess -> {
                        state.copy(
                            loading = false
                        )
                    }
                    is InternalAction.RefreshFail -> {
                        state.copy(
                            loading = false,
                            error = action.error
                        )
                    }
                    is InternalAction.UpdateData<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            content = action.data as T
                        )
                    }
                }
            }
            .doOnNext { stateSubject.onNext(it) }
            .share()
    }

    private sealed class InternalAction {
        object RefreshStart : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()
        object RefreshSuccess : InternalAction()
        class UpdateData<T>(val data: T) : InternalAction()
    }
}