package ru.mobileup.rxplce

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import me.dmdev.rxpm.PresentationModel


class LcePmImpl<T> private constructor(
    private val refreshData: Completable?,
    private val loadData: Single<T>?,
    private val dataChanges: Observable<T>?
) : PresentationModel() {

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

    val dataState = State<DataState<T>>(DataState())
    val refreshes = Action<Unit>()

    interface DataMaybeEmpty {
        fun isEmpty(): Boolean
    }

    data class DataState<T>(
        val internalAction: InternalAction? = null,
        val data: T? = null,
        val refreshingError: Throwable? = null,
        val refreshing: Boolean = false
    ) {
        val dataIsEmpty = when (data) {
            is Collection<*> -> data.isEmpty()
            is Array<*> -> data.isEmpty()
            is String -> data.isEmpty()
            is DataMaybeEmpty -> data.isEmpty()
            else -> false
        }

        val dataIsEmptyOrNull = dataIsEmpty || data == null
    }

    override fun onCreate() {
        super.onCreate()

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

        refreshes.observable
            .withLatestFrom(
                dataState.observable,
                BiFunction { _: Unit, state: DataState<T> -> state }
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
                            internalAction = action,
                            refreshing = true,
                            refreshingError = null
                        )
                    }
                    is InternalAction.LoadSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            internalAction = action,
                            refreshing = false,
                            data = action.data as T
                        )
                    }
                    is InternalAction.LoadFail -> {
                        state.copy(
                            internalAction = action,
                            refreshing = false,
                            refreshingError = action.error
                        )
                    }
                    is InternalAction.StartRefresh -> {
                        state.copy(
                            internalAction = action,
                            refreshing = true,
                            refreshingError = null
                        )
                    }
                    is InternalAction.RefreshSuccess -> {
                        state.copy(
                            internalAction = action,
                            refreshing = false
                        )
                    }
                    is InternalAction.RefreshFail -> {
                        state.copy(
                            internalAction = action,
                            refreshing = false,
                            refreshingError = action.error
                        )
                    }
                    is InternalAction.UpdateData<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            internalAction = action,
                            data = action.data as T
                        )
                    }
                }
            }
            .distinctUntilChanged()
            .subscribe(dataState.consumer)
            .untilDestroy()

    }

    sealed class InternalAction {
        object StartRefresh : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()
        object RefreshSuccess : InternalAction()
        object StartLoad : InternalAction()
        class LoadSuccess<T>(val data: T) : InternalAction()
        class LoadFail(val error: Throwable) : InternalAction()
        class UpdateData<T>(val data: T) : InternalAction()
    }
}