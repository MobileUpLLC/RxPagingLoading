package ru.mobileup.rxplce

import io.reactivex.Observable
import me.dmdev.rxpm.PresentationModel

typealias PagingStateMapper<T> = ((pagingState: PagingPm.PagingState<T>) -> PagingScreenPmImpl.ScreenState<T>)

class DefaultPagingStateMapper<T> : PagingStateMapper<T> {
    override fun invoke(pagingState: PagingPm.PagingState<T>): PagingScreenPmImpl.ScreenState<T> {
        return object : PagingScreenPmImpl.ScreenState<T> {
            override val data: List<T>?
                get() = pagingState.data

            override val isLoading: Boolean
                get() = pagingState.data == null && pagingState.refreshing

            override val isRefreshing: Boolean
                get() = pagingState.data != null && pagingState.refreshing

            override val refreshEnabled: Boolean
                get() = contentIsVisible || emptyViewIsVisible || errorViewIsVisible

            override val contentIsVisible: Boolean
                get() = pagingState.data != null && pagingState.dataIsEmpty().not()

            override val emptyViewIsVisible: Boolean
                get() = pagingState.data != null && pagingState.dataIsEmpty()

            override val errorViewIsVisible: Boolean
                get() = data == null && pagingState.refreshingError != null

        }
    }
}

class PagingScreenPmImpl<T>(
    private val pagingPm: PagingPm<T>,
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

    private val screenStateChanges = pagingPm.pagingState.observable.map {
        stateMapper(it)
    }

    interface ScreenState<T> {
        val data: List<T>?
        val isLoading: Boolean
        val isRefreshing: Boolean
        val refreshEnabled: Boolean
        val contentIsVisible: Boolean
        val emptyViewIsVisible: Boolean
        val errorViewIsVisible: Boolean
    }

    override fun onCreate() {
        super.onCreate()

        if (pagingPm is PresentationModel) {
            pagingPm.attachToParent(this)
        }

        Observable
            .merge(
                refreshAction.observable,
                retryLoadAction.observable
            )
            .startWith(Unit)
            .subscribe(pagingPm.refreshes.consumer)
            .untilDestroy()

        Observable
            .merge(
                nextPageAction.observable,
                retryLoadNextPageAction.observable
            )
            .filter { pagingPm.pagingState.value.pageLoadingError == null }
            .subscribe(pagingPm.loadNextPage.consumer)
            .untilDestroy()

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

        pagingPm.pagingState.observable
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

        pagingPm.pagingState.observable
            .map { it.pageLoadingError != null }
            .distinctUntilChanged()
            .subscribe(pageErrorVisible.consumer)
            .untilDestroy()
    }
}