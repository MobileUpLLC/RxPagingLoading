package ru.mobileup.rxplce.pm

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import ru.mobileup.rxplce.Paging

class PagingPmImpl<T>(
    private val paging: Paging<T>,
    private val stateMapper: ScreenStateMapper<List<T>> = ScreenStateMapperDefault()
) : PresentationModel(), PagingPm<T> {

    override val content = state<List<T>>()

    override val isLoading = state<Boolean>()
    override val isRefreshing = state<Boolean>()
    override val pageIsLoading = state<Boolean>()

    override val refreshEnabled = state<Boolean>()

    override val contentViewVisible = state<Boolean>()
    override val emptyViewVisible = state<Boolean>()
    override val errorViewVisible = state<Boolean>()
    override val pageErrorVisible = state<Boolean>()

    override val scrollToTop = command<Unit>()

    override val refreshAction = action<Unit>()
    override val nextPageAction = action<Unit>()
    override val retryAction = action<Unit>()
    override val retryNextPageAction = action<Unit>()

    val errorNoticeObservable: Observable<Throwable> = paging.state
        .filter { it.content != null && it.error != null }
        .map { it.error!! }
        .distinctUntilChanged()

    val pageErrorObservable: Observable<Throwable> = paging.state
        .filter { it.pageError != null }
        .map { it.pageError!! }
        .distinctUntilChanged()

    private val screenStateChanges = paging.state
        .map {
            stateMapper.mapToScreenState(it.loading, it.content, it.error)
        }
        .share()

    override fun onCreate() {
        super.onCreate()

        screenStateChanges
            .filter { it.content != null }
            .map { it.content!! }
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

        paging.state
            .map { it.pageError != null }
            .distinctUntilChanged()
            .subscribe(pageErrorVisible.consumer)
            .untilDestroy()

        Observable
            .merge(
                refreshAction.observable,
                retryAction.observable
            )
            .startWith(Unit)
            .map { Paging.Action.REFRESH }
            .subscribe(paging.actions)
            .untilDestroy()

        Observable
            .merge(
                nextPageAction.observable,
                retryNextPageAction.observable
            )
            .map { Paging.Action.LOAD_NEXT_PAGE }
            .subscribe(paging.actions)
            .untilDestroy()
    }
}