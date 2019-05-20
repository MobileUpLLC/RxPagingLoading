package ru.mobileup.rxplce

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel

class LoadingPmImpl<T>(
    private val loading: Loading<T>,
    private val stateMapper: ScreenStateMapper<T> = ScreenStateMapperDefault()
) : PresentationModel(), LoadingPm<T> {

    override val content = State<T>()

    override val isLoading = State<Boolean>()
    override val isRefreshing = State<Boolean>()

    override val refreshEnabled = State<Boolean>()

    override val contentViewVisible = State<Boolean>()
    override val emptyViewVisible = State<Boolean>()
    override val errorViewVisible = State<Boolean>()

    override val refreshAction = Action<Unit>()
    override val retryAction = Action<Unit>()

    val errorNoticeObservable: Observable<Throwable> = loading.state
        .filter { it.content != null && it.error != null }
        .map { it.error!! }
        .distinctUntilChanged()

    private val screenStateChanges = loading.state
        .map {
            stateMapper.mapToScreenState(it.loading, it.content, it.error)
        }
        .share()

    override fun onCreate() {
        super.onCreate()

        screenStateChanges
            .filter { it.content != null }
            .map { it.content!! }
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
            .map { it.contentViewVisible }
            .distinctUntilChanged()
            .subscribe(contentViewVisible.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.emptyViewVisible }
            .distinctUntilChanged()
            .subscribe(emptyViewVisible.consumer)
            .untilDestroy()

        screenStateChanges
            .map { it.errorViewVisible }
            .distinctUntilChanged()
            .subscribe(errorViewVisible.consumer)
            .untilDestroy()

        Observable.merge(refreshAction.observable, retryAction.observable)
            .startWith(Unit)
            .map { Loading.Action.REFRESH }
            .subscribe(loading.actions)
            .untilDestroy()
    }
}