package ru.mobileup.rxplce

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel

class PagingScreenPmImpl<T>(
    private val paging: Paging<T>,
    private val stateMapper: ScreenStateMapper<List<T>> = DefaultScreenStateMapper()
) : PresentationModel(), PagingScreenPm<T> {

    override val content = State<List<T>>()

    override val isLoading = State<Boolean>()
    override val isRefreshing = State<Boolean>()
    override val pageIsLoading = State<Boolean>()

    override val refreshEnabled = State<Boolean>()

    override val contentVisible = State<Boolean>()
    override val emptyViewVisible = State<Boolean>()
    override val errorViewVisible = State<Boolean>()
    override val pageErrorVisible = State<Boolean>()

    override val scrollToTop = Command<Unit>()

    override val refreshAction = Action<Unit>()
    override val nextPageAction = Action<Unit>()
    override val retryLoadAction = Action<Unit>()
    override val retryLoadNextPageAction = Action<Unit>()

    private val screenStateChanges = paging.state
        .map {
            stateMapper.mapLceStateToScreenState(it.loading, it.content, it.loadingError)
        }
        .share()

    override fun onCreate() {
        super.onCreate()

        screenStateChanges
            .filter { it.data != null }
            .map { it.data!! }
            .distinctUntilChanged { l1: List<T>, l2: List<T> -> l1 === l2 }
            .subscribe {
                if (it.size <= content.valueOrNull?.size ?: 0) {
                    scrollToTop.consumer.accept(Unit)
                }
                content.consumer.accept(it)
            }
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

        paging.state
            .map { it.pageLoading }
            .distinctUntilChanged()
            .subscribe(pageIsLoading.consumer)
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

        paging.state
            .map { it.pageLoadingError != null }
            .distinctUntilChanged()
            .subscribe(pageErrorVisible.consumer)
            .untilDestroy()

        Observable
            .merge(
                refreshAction.observable,
                retryLoadAction.observable
            )
            .startWith(Unit)
            .map { Paging.Action.REFRESH }
            .subscribe(paging.actions)
            .untilDestroy()

        Observable
            .merge(
                nextPageAction.observable,
                retryLoadNextPageAction.observable
            )
            .map { Paging.Action.LOAD_NEXT_PAGE }
            .subscribe(paging.actions)
            .untilDestroy()
    }
}