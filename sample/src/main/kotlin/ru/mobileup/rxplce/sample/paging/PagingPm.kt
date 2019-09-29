package ru.mobileup.rxplce.sample.paging

import me.dmdev.rxpm.command
import ru.mobileup.rxplce.*
import ru.mobileup.rxplce.sample.BasePresentationModel

class PagingPm(repository: ItemsRepository) : BasePresentationModel() {

    class PageInfo(
        override val items: List<Item>,
        override val isEndReached: Boolean
    ) : Paging.Page<Item>

    private val paging = PagingImpl<Item>(
        pageSource = { offset, lastPage ->
            repository
                .loadPage(last = lastPage?.lastItem)
                .map {
                    PageInfo(
                        items = it.list,
                        isEndReached = (offset + it.list.size) == it.totalCount
                    )
                }
        }
    )

    val content = stateOf(paging.contentChanges())

    val isLoading = stateOf(paging.isLoading())
    val isRefreshing = stateOf(paging.isRefreshing())
    val refreshEnabled = stateOf(paging.refreshEnabled())

    val contentViewVisible = stateOf(paging.contentVisible())
    val emptyViewVisible = stateOf(paging.emptyVisible())
    val errorViewVisible = stateOf(paging.errorVisible())

    val pageIsLoading = stateOf(paging.pageIsLoading())
    val pageErrorVisible = stateOf(paging.pagingErrorVisible())

    val refreshAction = actionTo<Unit, Paging.Action>(paging.actions) {
        startWith(Unit).map { Paging.Action.REFRESH }
    }

    val retryAction = actionTo<Unit, Paging.Action>(paging.actions) {
        map { Paging.Action.REFRESH }
    }

    val nextPageAction = actionTo<Unit, Paging.Action>(paging.actions) {
        map { Paging.Action.LOAD_NEXT_PAGE }
    }

    val retryNextPageAction = actionTo<Unit, Paging.Action>(paging.actions) {
        map { Paging.Action.LOAD_NEXT_PAGE }
    }

    val scrollToTop = command<Unit>()
    val showError = command<String>()

    override fun onCreate() {
        super.onCreate()

        paging.errorChanges()
            .subscribe {
                if (content.valueOrNull.isNullOrEmpty().not()) {
                    showError.consumer.accept("Refreshing Error")
                }
            }
            .untilDestroy()

        paging.scrollToTop()
            .subscribe(scrollToTop.consumer)
            .untilDestroy()
    }
}