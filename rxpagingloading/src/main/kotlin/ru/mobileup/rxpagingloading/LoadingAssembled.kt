package ru.mobileup.rxpagingloading

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxpagingloading.Loading.Action
import ru.mobileup.rxpagingloading.Loading.Action.FORCE_REFRESH
import ru.mobileup.rxpagingloading.Loading.Action.REFRESH
import ru.mobileup.rxpagingloading.Loading.State
import ru.mobileup.rxpagingloading.LoadingAssembled.InternalAction.*

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

    private val stateSubject = BehaviorSubject.create<State<T>>().toSerialized()
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
                    REFRESH -> state.loading.not()
                    FORCE_REFRESH -> true
                }
            }
            .switchMap { (action, _) ->
                refresh
                    .toSingleDefault(Unit)
                    .toObservable()
                    .map<InternalAction> { RefreshSuccess }
                    .startWith(RefreshStart(force = action == FORCE_REFRESH))
                    .onErrorReturn { RefreshFail(it) }
            }
            .mergeWith(
                updates.map { UpdateData(it) }
            )
            .observeOn(AndroidSchedulers.mainThread())
            .scan(State<T>()) { state, action ->
                when (action) {
                    is RefreshStart -> {
                        if (action.force) {
                            state.copy(
                                content = null,
                                loading = true,
                                error = null
                            )
                        } else {
                            state.copy(
                                loading = true,
                                error = null
                            )
                        }
                    }
                    is RefreshSuccess -> {
                        state.copy(
                            loading = false
                        )
                    }
                    is RefreshFail -> {
                        state.copy(
                            loading = false,
                            error = action.error
                        )
                    }
                    is UpdateData<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            content = action.data as T
                        )
                    }
                }
            }
            .distinctUntilChanged()
            .doOnNext { stateSubject.onNext(it) }
            .replay(1)
            .refCount()
    }

    private sealed class InternalAction {
        class RefreshStart(val force: Boolean) : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()
        object RefreshSuccess : InternalAction()
        class UpdateData<T>(val data: T) : InternalAction()
    }
}