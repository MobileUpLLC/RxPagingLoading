package ru.mobileup.rxplce

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.LceScreenPmImpl.ScreenState

typealias StateMapper<T> = ((lceState: LcePmImpl.DataState<T>) -> ScreenState<T>)

class DefaultStateMapper<T> : StateMapper<T> {
    override fun invoke(lceState: LcePmImpl.DataState<T>): ScreenState<T> {
        return object : ScreenState<T> {
            override val data: T?
                get() = lceState.data

            override val isLoading: Boolean
                get() = lceState.data == null && lceState.refreshing

            override val isRefreshing: Boolean
                get() = lceState.data != null && lceState.refreshing

            override val refreshEnabled: Boolean
                get() = contentIsVisible || emptyViewIsVisible || errorViewIsVisible

            override val contentIsVisible: Boolean
                get() = lceState.data != null && lceState.dataIsEmpty.not()

            override val emptyViewIsVisible: Boolean
                get() = lceState.data != null && lceState.dataIsEmpty

            override val errorViewIsVisible: Boolean
                get() = data == null && lceState.refreshingError != null

        }
    }
}

class LceScreenPmImpl<T> private constructor(
    private val stateMapper: StateMapper<T>,
    private val refreshData: Completable?,
    private val loadData: Single<T>?,
    private val dataChanges: Observable<T>?
) : PresentationModel(), LceScreenPm<T> {

    constructor(
        loadingData: Single<T>,
        stateMapper: StateMapper<T> = DefaultStateMapper()
    ) : this(
        stateMapper,
        refreshData = null,
        loadData = loadingData,
        dataChanges = null
    )

    constructor(
        refreshData: Completable,
        dataChanges: Observable<T>,
        stateMapper: StateMapper<T> = DefaultStateMapper()
    ) : this(
        stateMapper,
        refreshData = refreshData,
        loadData = null,
        dataChanges = dataChanges
    )

    override val data = State<T>()

    override val isLoading = State<Boolean>()
    override val isRefreshing = State<Boolean>()

    override val refreshEnabled = State<Boolean>()

    override val contentVisible = State<Boolean>()
    override val emptyViewVisible = State<Boolean>()
    override val errorViewVisible = State<Boolean>()

    override val refreshAction = Action<Unit>()
    override val retryLoadAction = Action<Unit>()

    val showError = Command<Throwable>()

    private val lcePm = if (loadData != null) {
        LcePmImpl(loadData)
    } else {
        LcePmImpl(refreshData!!, dataChanges!!)
    }

    private val screenStateChanges = lcePm.dataState.observable.map {
        stateMapper(it)
    }

    interface ScreenState<T> {
        val data: T?
        val isLoading: Boolean
        val isRefreshing: Boolean
        val refreshEnabled: Boolean
        val contentIsVisible: Boolean
        val emptyViewIsVisible: Boolean
        val errorViewIsVisible: Boolean
    }

    override fun onCreate() {
        super.onCreate()

        lcePm.attachToParent(this)

        Observable.merge(refreshAction.observable, retryLoadAction.observable)
            .startWith(Unit)
            .subscribe(lcePm.refreshes.consumer)
            .untilDestroy()

        screenStateChanges
            .filter { it.data != null }
            .map { it.data!! }
            .subscribe(data.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.isLoading }
            .distinctUntilChanged()
            .subscribe(isLoading.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.isRefreshing }
            .distinctUntilChanged()
            .subscribe(isRefreshing.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.refreshEnabled }
            .distinctUntilChanged()
            .subscribe(refreshEnabled.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.contentIsVisible }
            .distinctUntilChanged()
            .subscribe(contentVisible.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.emptyViewIsVisible }
            .distinctUntilChanged()
            .subscribe(emptyViewVisible.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.errorViewIsVisible }
            .distinctUntilChanged()
            .subscribe(errorViewVisible.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .filter { it.data != null && it.refreshingError != null }
            .map { it.refreshingError!! }
            .distinctUntilChanged()
            .subscribe(showError.consumer)
            .untilDestroy()
    }
}