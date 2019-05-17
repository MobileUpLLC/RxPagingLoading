package ru.mobileup.rxplce

import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import ru.mobileup.rxplce.Paging.Page
import ru.mobileup.rxplce.Paging.PagingState
import ru.mobileup.rxplce.util.SchedulersRule
import java.io.IOException
import java.util.concurrent.TimeUnit

class PagingPmImplTest {

    @get:Rule
    val schedulers = SchedulersRule(true)

    private val refreshingError = IOException()
    private val pageLoadingError = IOException()

    data class DataPage(
        override val list: List<Int>,
        override val isReachedEnd: Boolean
    ) : Page<Int>

    private fun getPage(offset: Int, isReachedEnd: Boolean): Page<Int> {
        return DataPage(
            list = List(3) { index -> index + offset + 1 },
            isReachedEnd = isReachedEnd
        )
    }

    @Test fun initialState() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            )
        )
    }

    @Test fun firstLoadPage() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun errorOnFirstLoadPage() {

        val paging = PagingImpl<Int>(pagingSource = { _, _ ->
            Single.error(refreshingError)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = refreshingError,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            )
        )
    }

    @Test fun pagingSuccess() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3, 4, 5, 6),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(4, 5, 6), false)
            )
        )
    }

    @Test fun pagingFail() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            if (offset == 0) {
                Single.just(getPage(offset, false))
            } else {
                Single.error(pageLoadingError)
            }
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = pageLoadingError,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun isReachedEnd() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single.just(getPage(offset, offset > 0))
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3, 4, 5, 6),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(4, 5, 6), true)
            )
        )
    }

    @Test fun blockRepeatedRefreshes() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single
                .just(getPage(offset, false))
                .delay(1, TimeUnit.SECONDS)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(2, TimeUnit.SECONDS)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun blockRepeatedPaging() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single
                .just(getPage(offset, false))
                .delay(1, TimeUnit.SECONDS)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(2, TimeUnit.SECONDS)

        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        schedulers.testScheduler.advanceTimeTo(4, TimeUnit.SECONDS)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3, 4, 5, 6),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(4, 5, 6), false)
            )
        )
    }

    @Test fun blockPagingOnRefreshing() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single
                .just(getPage(offset, false))
                .delay(1, TimeUnit.SECONDS)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        schedulers.testScheduler.advanceTimeTo(2, TimeUnit.SECONDS)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun interruptPagingByRefresh() {

        val paging = PagingImpl<Int>(pagingSource = { offset, _ ->
            Single
                .just(getPage(offset, false))
                .delay(1, TimeUnit.SECONDS)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(2, TimeUnit.SECONDS)

        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(4, TimeUnit.SECONDS)

        testObserver.assertValues(
            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = null,
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = null
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = true,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            PagingState(
                data = listOf(1, 2, 3),
                refreshingError = null,
                pageLoadingError = null,
                refreshing = false,
                pageIsLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }
}