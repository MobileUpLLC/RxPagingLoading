package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.PagingPm.Page

class PagingScreenPmImpl<T>(
    pagingSource: ((offset: Int, lastPage: Page<T>?) -> Single<Page<T>>)
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

    private val pagingPm = PagingPmImpl(pagingSource)

    override fun onCreate() {
        super.onCreate()

        pagingPm.attachToParent(this)

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

        pagingPm.pagingState.observable
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

        pagingPm.pagingState.observable
            .map { it.refreshing && it.data == null }
            .distinctUntilChanged()
            .subscribe(isLoading.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.refreshing && it.data != null }
            .distinctUntilChanged()
            .subscribe(isRefreshing.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.data != null }
            .distinctUntilChanged()
            .subscribe(refreshEnabled.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.pageIsLoading }
            .distinctUntilChanged()
            .subscribe(pageIsLoading.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.data?.isNotEmpty() ?: false }
            .distinctUntilChanged()
            .subscribe(contentVisible.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.data?.isEmpty() ?: false && it.refreshingError == null && it.refreshing.not() }
            .distinctUntilChanged()
            .subscribe(emptyViewVisible.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.refreshingError != null && it.data?.isEmpty() ?: true }
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