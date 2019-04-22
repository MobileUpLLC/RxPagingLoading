package ru.mobileup.rxplce

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.PresentationModel

class LceScreenPmImpl<T> private constructor(
    private val refreshData: Completable?,
    private val loadData: Single<T>?,
    private val dataChanges: Observable<T>?
) : PresentationModel(), LceScreenPm<T> {

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

    override val data = State<T>()

    override val isLoading = State<Boolean>()
    override val isRefreshing = State<Boolean>()

    override val refreshEnabled = State<Boolean>()

    override val contentVisible = State<Boolean>()
    override val emptyViewVisible = State<Boolean>()
    override val errorViewVisible = State<Boolean>()

    override val refreshAction = Action<Unit>()
    override val retryLoadAction = Action<Unit>()

    private val lcePm = if (loadData != null) {
        LcePmImpl(loadData)
    } else {
        LcePmImpl(refreshData!!, dataChanges!!)
    }

    override fun onCreate() {
        super.onCreate()

        lcePm.attachToParent(this)

        Observable.merge(refreshAction.observable, retryLoadAction.observable)
            .startWith(Unit)
            .subscribe(lcePm.refreshes.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .filter { it.data != null }
            .map { it.data!! }
            .subscribe(data.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.refreshing && it.dataIsEmptyOrNull }
            .distinctUntilChanged()
            .subscribe(isLoading.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.refreshing && it.dataIsEmptyOrNull.not() }
            .distinctUntilChanged()
            .subscribe(isRefreshing.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.dataIsEmptyOrNull.not() }
            .distinctUntilChanged()
            .subscribe(refreshEnabled.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.dataIsEmptyOrNull.not() }
            .distinctUntilChanged()
            .subscribe(contentVisible.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.dataIsEmpty && it.refreshingError == null && it.refreshing.not() }
            .distinctUntilChanged()
            .subscribe(emptyViewVisible.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.refreshingError != null && it.dataIsEmptyOrNull }
            .distinctUntilChanged()
            .subscribe(errorViewVisible.consumer)
            .untilDestroy()
    }
}