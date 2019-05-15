package ru.mobileup.rxplce

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.LceScreenPmImpl.ScreenState

typealias StateMapper<T> = ((lceState: Lce.DataState<T>) -> ScreenState<T>)

class DefaultStateMapper<T> : StateMapper<T> {
    override fun invoke(lceState: Lce.DataState<T>): ScreenState<T> {

        val contentIsVisible = lceState.data != null && lceState.dataIsEmpty().not()
        val emptyViewIsVisible = lceState.data != null && lceState.dataIsEmpty()
        val errorViewIsVisible = lceState.data == null && lceState.refreshingError != null

        return ScreenState(
            data = lceState.data,
            isLoading = lceState.data == null && lceState.refreshing,
            isRefreshing = lceState.data != null && lceState.refreshing,
            contentIsVisible = contentIsVisible,
            emptyViewIsVisible = emptyViewIsVisible,
            errorViewIsVisible = errorViewIsVisible,
            refreshEnabled = contentIsVisible || emptyViewIsVisible || errorViewIsVisible
        )
    }
}

class LceScreenPmImpl<T>(
    private val lce: Lce<T>,
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

    private val screenStateChanges = lce.state.map {
        stateMapper(it)
    }.share()

    data class ScreenState<T>(
        val data: T?,
        val isLoading: Boolean,
        val isRefreshing: Boolean,
        val refreshEnabled: Boolean,
        val contentIsVisible: Boolean,
        val emptyViewIsVisible: Boolean,
        val errorViewIsVisible: Boolean
    )

    override fun onCreate() {
        super.onCreate()

        if (lce is PresentationModel) {
            lce.attachToParent(this)
        }

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

        lce.state
            .filter { it.data != null && it.refreshingError != null }
            .map { it.refreshingError!! }
            .distinctUntilChanged()
            .subscribe(showError.consumer)
            .untilDestroy()

        Observable.merge(refreshAction.observable, retryLoadAction.observable)
            .startWith(Unit)
            .map { Lce.Action.REFRESH }
            .subscribe(lce.actions)
            .untilDestroy()
    }
}