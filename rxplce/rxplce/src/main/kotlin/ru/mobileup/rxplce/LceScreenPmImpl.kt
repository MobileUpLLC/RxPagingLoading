package ru.mobileup.rxplce

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.LceScreenPmImpl.ScreenState

typealias StateMapper<T> = ((lceState: LcePm.DataState<T>) -> ScreenState<T>)

class DefaultStateMapper<T> : StateMapper<T> {
    override fun invoke(lceState: LcePm.DataState<T>): ScreenState<T> {
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
                get() = lceState.data != null && lceState.dataIsEmpty().not()

            override val emptyViewIsVisible: Boolean
                get() = lceState.data != null && lceState.dataIsEmpty()

            override val errorViewIsVisible: Boolean
                get() = data == null && lceState.refreshingError != null

        }
    }
}

class LceScreenPmImpl<T>(
    private val lcePm: LcePm<T>,
    private val stateMapper: StateMapper<T> = DefaultStateMapper()
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

    val showError = Command<Throwable>()

    private val screenStateChanges = lcePm.dataState.observable.share().map {
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

        if (lcePm is PresentationModel) {
            lcePm.attachToParent(this)
        }

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