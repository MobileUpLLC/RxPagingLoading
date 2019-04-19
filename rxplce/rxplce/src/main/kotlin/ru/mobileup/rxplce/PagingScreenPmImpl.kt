package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.PagingPmImpl.ActionType

class PagingScreenPmImpl<T>(
    pagingSource: ((offset: Int, lastPage: PagingPmImpl.Page<T>?) -> Single<PagingPmImpl.Page<T>>)
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

    override val refreshAction = Action<Unit>()
    override val nextPageAction = Action<Unit>()
    override val retryLoadAction = Action<Unit>()
    override val retryLoadNextPageAction = Action<Unit>()

    private val pagingPm = PagingPmImpl(pagingSource)

    override fun onCreate() {
        super.onCreate()

        pagingPm.attachToParent(this)

        Observable.merge(
            refreshAction.observable.map { ActionType.REFRESH },
            nextPageAction.observable.map { ActionType.LOAD_PAGE },
            retryLoadAction.observable.map { ActionType.REFRESH },
            retryLoadNextPageAction.observable.map { ActionType.LOAD_PAGE }
        )
            .startWith(ActionType.REFRESH)
            .subscribe(pagingPm.actions.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .filter { it.data != null }
            .map { it.data!! }
            .distinctUntilChanged { l1: List<T>, l2: List<T> -> l1 === l2 }
            .subscribe(data.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.refreshing && it.data?.isEmpty() ?: true }
            .distinctUntilChanged()
            .subscribe(isLoading.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.refreshing && it.data?.isNotEmpty() ?: false }
            .distinctUntilChanged()
            .subscribe(isRefreshing.consumer)
            .untilDestroy()

        pagingPm.pagingState.observable
            .map { it.data != null && it.refreshingError == null }
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