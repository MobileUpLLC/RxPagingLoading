package ru.mobileup.rxplce

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.PresentationModel

class LceScreenPmImpl<T>(
    private val loadSource: Single<T>
) : PresentationModel(), LceScreenPm<T> {

    override val data = State<T>()

    override val isLoading = State<Boolean>()
    override val isRefreshing = State<Boolean>()

    override val refreshEnabled = State<Boolean>()

    override val contentVisible = State<Boolean>()
    override val emptyViewVisible = State<Boolean>()
    override val errorViewVisible = State<Boolean>()

    override val refreshAction = Action<Unit>()
    override val retryLoadAction = Action<Unit>()

    private val lcePm = LcePmImpl(loadSource)

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
            .map { it.refreshing && it.data == null }
            .distinctUntilChanged()
            .subscribe(isLoading.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.refreshing && it.data != null }
            .distinctUntilChanged()
            .subscribe(isRefreshing.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.data != null && it.refreshingError == null }
            .distinctUntilChanged()
            .subscribe(refreshEnabled.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.data != null }
            .distinctUntilChanged()
            .subscribe(contentVisible.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.dataIsEmpty && it.refreshingError == null && it.refreshing.not() }
            .distinctUntilChanged()
            .subscribe(emptyViewVisible.consumer)
            .untilDestroy()

        lcePm.dataState.observable
            .map { it.refreshingError != null && it.data == null }
            .doOnNext {
                Log.d("DDD", "ERROR VISIBLE = $it")
            }
            .distinctUntilChanged()
            .subscribe(errorViewVisible.consumer)
            .untilDestroy()
    }
}