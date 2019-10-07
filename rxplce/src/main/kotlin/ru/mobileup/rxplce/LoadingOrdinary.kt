package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxplce.Loading.Action
import ru.mobileup.rxplce.Loading.Action.FORCE_REFRESH
import ru.mobileup.rxplce.Loading.Action.REFRESH
import ru.mobileup.rxplce.Loading.State
import ru.mobileup.rxplce.LoadingOrdinary.InternalAction.*

/**
 * Simple data loader [implementation][Loading].
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
                BiFunction { action: Action, state: State<T> -> action to state }
            )
            .filter { (action, state) ->
                when (action) {
                    REFRESH -> state.loading.not()
                    FORCE_REFRESH -> true
                }
            }
            .switchMap { (action, _) ->
                source
                    .toObservable()
                    .map<InternalAction> { LoadSuccess(it) }
                    .startWith(LoadStart(force = action == FORCE_REFRESH))
                    .onErrorReturn { LoadFail(it) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .scan(State<T>()) { state, action ->
                when (action) {
                    is LoadStart -> {
                        state.copy(
                            content = if (action.force) null else state.content,
                            loading = true,
                            error = null
                        )
                    }
                    is LoadSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            loading = false,
                            content = action.data as T
                        )
                    }
                    is LoadFail -> {
                        state.copy(
                            loading = false,
                            error = action.error
                        )
                    }
                }
            }
            .distinctUntilChanged()
            .doOnNext { stateSubject.onNext(it) }
            .share()
    }

    private sealed class InternalAction {
        class LoadStart(val force: Boolean) : InternalAction()
        class LoadSuccess<T>(val data: T) : InternalAction()
        class LoadFail(val error: Throwable) : InternalAction()
    }
}