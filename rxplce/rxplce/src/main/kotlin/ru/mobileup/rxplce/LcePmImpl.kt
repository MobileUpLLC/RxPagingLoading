package ru.mobileup.rxplce

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel

class LcePmImpl<T>(
    private val lce: Lce<T>,
    private val stateMapper: ScreenStateMapper<T> = DefaultScreenStateMapper()
) : PresentationModel(), LcePm<T> {

    override val content = State<T>()

    override val isLoading = State<Boolean>()
    override val isRefreshing = State<Boolean>()

    override val refreshEnabled = State<Boolean>()

    override val contentVisible = State<Boolean>()
    override val emptyViewVisible = State<Boolean>()
    override val errorViewVisible = State<Boolean>()

    override val refreshAction = Action<Unit>()
    override val retryLoadAction = Action<Unit>()

    val showError = Command<Throwable>()

    private val screenStateChanges = lce.state
        .map {
            stateMapper.mapLceStateToScreenState(it.loading, it.content, it.loadingError)
        }
        .share()

    override fun onCreate() {
        super.onCreate()

        screenStateChanges
            .filter { it.data != null }
            .map { it.data!! }
            .subscribe(content.consumer)
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
            .filter { it.content != null && it.loadingError != null }
            .map { it.loadingError!! }
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