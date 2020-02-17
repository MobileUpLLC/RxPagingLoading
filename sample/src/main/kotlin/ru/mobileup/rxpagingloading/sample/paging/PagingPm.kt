package ru.mobileup.rxpagingloading.sample.paging

import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import ru.mobileup.rxpagingloading.*

class PagingPm(repository: ItemsRepository) :PresentationModel() {

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

    val content = state { paging.contentChanges() }

    val isLoading = state { paging.isLoading() }
    val isRefreshing = state { paging.isRefreshing() }
    val refreshEnabled = state { paging.refreshEnabled() }

    val contentViewVisible = state { paging.contentVisible() }
    val emptyViewVisible = state { paging.emptyVisible() }
    val errorViewVisible = state { paging.errorVisible() }

    val pageIsLoading = state { paging.pageIsLoading() }
    val pageErrorVisible = state { paging.pagingErrorVisible() }

    val refreshAction = action<Unit> {
        this.startWith(Unit)
            .map { Paging.Action.REFRESH }
            .doOnNext(paging.actions)
    }

    val retryAction = action<Unit> {
        this.map { Paging.Action.REFRESH }
            .doOnNext(paging.actions)
    }

    val nextPageAction = action<Unit> {
        this.map { Paging.Action.LOAD_NEXT_PAGE }
            .doOnNext(paging.actions)
    }

    val retryNextPageAction = action<Unit> {
        this.map { Paging.Action.LOAD_NEXT_PAGE }
            .doOnNext(paging.actions)
    }

    val scrollToTop = command<Unit>()
    val showError = command<String>()

    override fun onCreate() {
        super.onCreate()

        paging.errorChanges()
            .subscribe {
                if (content.valueOrNull != null) {
                    showError.consumer.accept("Refreshing Error")
                }
            }
            .untilDestroy()

        paging.scrollToTop()
            .subscribe(scrollToTop.consumer)
            .untilDestroy()
    }
}