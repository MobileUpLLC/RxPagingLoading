package ru.mobileup.rxplce

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxplce.Lce.Action
import ru.mobileup.rxplce.Lce.DataState

class LceImpl<T> private constructor(
    private val refreshData: Completable?,
    private val loadData: Single<T>?,
    private val dataChanges: Observable<T>?
) : Lce<T> {

    constructor(loadingData: Single<T>) : this(
        refreshData = null,
        loadData = loadingData,
        dataChanges = null
    )

    constructor(
        refreshData: Completable,
        dataChanges: Observable<T>
    ) : this(
        refreshData = refreshData,
        loadData = null,
        dataChanges = dataChanges
    )

    private val stateSubject = BehaviorSubject.createDefault<DataState<T>>(DataState()).toSerialized()
    private val actionSubject = PublishSubject.create<Action>().toSerialized()

    override val actions: Consumer<Action>
        get() = Consumer { actionSubject.onNext(it) }

    override val state: Observable<DataState<T>>

    init {

        val observable =
            when {
                refreshData != null -> refreshData
                    .toSingleDefault(Unit)
                    .toObservable()
                    .map<InternalAction> { InternalAction.RefreshSuccess }
                    .startWith(InternalAction.StartRefresh)
                    .onErrorReturn { InternalAction.RefreshFail(it) }
                loadData != null -> loadData
                    .toObservable()
                    .map<InternalAction> { InternalAction.LoadSuccess(it) }
                    .startWith(InternalAction.StartLoad)
                    .onErrorReturn { InternalAction.LoadFail(it) }
                else -> Observable.empty()
            }

        state = actionSubject
            .withLatestFrom(
                stateSubject,
                BiFunction { _: Action, state: DataState<T> -> state }
            )
            .filter { !it.refreshing }
            .switchMap { observable }
            .mergeWith(
                dataChanges
                    ?.map { InternalAction.UpdateData(it) }
                    ?: Observable.empty()
            )
            .scan(DataState<T>()) { state, action ->
                when (action) {
                    is InternalAction.StartLoad -> {
                        state.copy(
                            refreshing = true,
                            refreshingError = null
                        )
                    }
                    is InternalAction.LoadSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            refreshing = false,
                            data = action.data as T
                        )
                    }
                    is InternalAction.LoadFail -> {
                        state.copy(
                            refreshing = false,
                            refreshingError = action.error
                        )
                    }
                    is InternalAction.StartRefresh -> {
                        state.copy(
                            refreshing = true,
                            refreshingError = null
                        )
                    }
                    is InternalAction.RefreshSuccess -> {
                        state.copy(
                            refreshing = false
                        )
                    }
                    is InternalAction.RefreshFail -> {
                        state.copy(
                            refreshing = false,
                            refreshingError = action.error
                        )
                    }
                    is InternalAction.UpdateData<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            data = action.data as T
                        )
                    }
                }
            }
            .distinctUntilChanged()
    }

    private sealed class InternalAction {
        object StartRefresh : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()
        object RefreshSuccess : InternalAction()
        object StartLoad : InternalAction()
        class LoadSuccess<T>(val data: T) : InternalAction()
        class LoadFail(val error: Throwable) : InternalAction()
        class UpdateData<T>(val data: T) : InternalAction()
    }
}