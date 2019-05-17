package ru.mobileup.rxplce

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel

typealias PagingStateMapper<T> = ((pagingState: Paging.PagingState<T>) -> PagingScreenPmImpl.ScreenState<T>)

class DefaultPagingStateMapper<T> : PagingStateMapper<T> {
    override fun invoke(pagingState: Paging.PagingState<T>): PagingScreenPmImpl.ScreenState<T> {

        val contentIsVisible = pagingState.data != null && pagingState.dataIsEmpty().not()
        val emptyViewIsVisible = pagingState.data != null && pagingState.dataIsEmpty()
        val errorViewIsVisible = pagingState.data == null && pagingState.refreshingError != null

        return PagingScreenPmImpl.ScreenState(
            data = pagingState.data,
            isLoading = pagingState.data == null && pagingState.refreshing,
            isRefreshing = pagingState.data != null && pagingState.refreshing,
            contentIsVisible = contentIsVisible,
            emptyViewIsVisible = emptyViewIsVisible,
            errorViewIsVisible = errorViewIsVisible,
            refreshEnabled = contentIsVisible || emptyViewIsVisible || errorViewIsVisible
        )
    }
}

class PagingScreenPmImpl<T>(
    private val paging: Paging<T>,
    private val stateMapper: PagingStateMapper<T> = DefaultPagingStateMapper()
) : PresentationModel(), PagingScreenPm<T> {

    override val data = State<List<T>>()

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

    private val screenStateChanges = paging.state.map {
        stateMapper(it)
    }.share()

    data class ScreenState<T>(
        val data: List<T>?,
        val isLoading: Boolean,
        val isRefreshing: Boolean,
        val refreshEnabled: Boolean,
        val contentIsVisible: Boolean,
        val emptyViewIsVisible: Boolean,
        val errorViewIsVisible: Boolean
    )

    override fun onCreate() {
        super.onCreate()

        screenStateChanges
            .filter { it.data != null }
            .map { it.data!! }
            .distinctUntilChanged { l1: List<T>, l2: List<T> -> l1 === l2 }
            .subscribe {
                if (it.size <= data.valueOrNull?.size ?: 0) {
                    scrollToTop.consumer.accept(Unit)
                }
                data.consumer.accept(it)
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
            .map { it.pageIsLoading }
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