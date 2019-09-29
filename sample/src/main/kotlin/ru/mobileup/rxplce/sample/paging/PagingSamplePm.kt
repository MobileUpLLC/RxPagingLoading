package ru.mobileup.rxplce.sample.paging

import me.dmdev.rxpm.*
import ru.mobileup.rxplce.*
import ru.mobileup.rxplce.sample.BasePresentationModel

class PagingSamplePm(repository: ItemsRepository) : BasePresentationModel() {

    class PageInfo(
        override val items: List<Item>,
        override val isEndReached: Boolean
    ) : Paging.Page<Item>

    private val loader = PagingImpl<Item>(
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

    val content = stateOf(loader.contentChanges())

    val isLoading = stateOf(loader.isLoading())
    val isRefreshing = stateOf(loader.isRefreshing())

    val contentViewVisible = stateOf(loader.contentVisible())
    val emptyViewVisible = stateOf(loader.emptyVisible())
    val errorViewVisible = stateOf(loader.errorVisible())

    val pageIsLoading = stateOf(loader.pageIsLoading())
    val pageErrorVisible = stateOf(loader.pagingErrorVisible())

    val refreshAction = actionTo<Unit, Paging.Action>(loader.actions) {
        startWith(Unit).map { Paging.Action.REFRESH }
    }

    val retryAction = actionTo<Unit, Paging.Action>(loader.actions) {
        map { Paging.Action.REFRESH }
    }

    val nextPageAction = actionTo<Unit, Paging.Action>(loader.actions) {
        map { Paging.Action.LOAD_NEXT_PAGE }
    }

    val retryNextPageAction = actionTo<Unit, Paging.Action>(loader.actions) {
        map { Paging.Action.LOAD_NEXT_PAGE }
    }

    val scrollToTop = command<Unit>()
    val showError = command<String>()

    override fun onCreate() {
        super.onCreate()

        loader.errorChanges()
            .subscribe {
                if (content.valueOrNull.isNullOrEmpty().not()) {
                    showError.consumer.accept("Refreshing Error")
                }
            }
            .untilDestroy()

        loader.scrollToTop()
            .subscribe(scrollToTop.consumer)
            .untilDestroy()
    }
}