package ru.mobileup.rxplce

import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import ru.mobileup.rxplce.Paging.Page
import ru.mobileup.rxplce.Paging.State
import ru.mobileup.rxplce.util.SchedulersRule
import java.io.IOException
import java.util.concurrent.TimeUnit

class PagingPmImplTest {

    @get:Rule
    val schedulers = SchedulersRule(true)

    private val refreshingError = IOException()
    private val pageLoadingError = IOException()

    data class DataPage(
        override val items: List<Int>,
        override val isEndReached: Boolean
    ) : Page<Int>

    private fun getPage(offset: Int, isReachedEnd: Boolean): Page<Int> {
        return DataPage(
            items = List(3) { index -> index + offset + 1 },
            isEndReached = isReachedEnd
        )
    }

    @Test fun initialState() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            )
        )
    }

    @Test fun multicast() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()
        val testObserver2 = paging.state.test()

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            )
        )

        testObserver2.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            )
        )
    }

    @Test fun firstLoadPage() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun errorOnFirstLoadPage() {

        val paging = PagingImpl<Int>(pageSource = { _, _ ->
            Single.error(refreshingError)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = refreshingError,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            )
        )
    }

    @Test fun pagingSuccess() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3, 4, 5, 6),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(4, 5, 6), false)
            )
        )
    }

    @Test fun pagingFail() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
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
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = pageLoadingError,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun isReachedEnd() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single.just(getPage(offset, offset > 0))
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3, 4, 5, 6),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(4, 5, 6), true)
            )
        )
    }

    @Test fun blockRepeatedRefreshes() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
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
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun blockRepeatedPaging() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
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
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3, 4, 5, 6),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(4, 5, 6), false)
            )
        )
    }

    @Test fun blockPagingOnRefreshing() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single
                .just(getPage(offset, false))
                .delay(1, TimeUnit.SECONDS)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)

        schedulers.testScheduler.advanceTimeTo(2, TimeUnit.SECONDS)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun interruptPagingByRefresh() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
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
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun forceRefresh() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single.just(getPage(offset, false))
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)
        paging.actions.accept(Paging.Action.FORCE_REFRESH)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun forceRefreshOnLoading() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single
                .just(getPage(offset, false))
                .delay(3, TimeUnit.SECONDS)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(1, TimeUnit.SECONDS)

        paging.actions.accept(Paging.Action.FORCE_REFRESH)

        schedulers.testScheduler.advanceTimeTo(4, TimeUnit.SECONDS)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }

    @Test fun interruptPagingByForcedRefresh() {

        val paging = PagingImpl<Int>(pageSource = { offset, _ ->
            Single
                .just(getPage(offset, false))
                .delay(1, TimeUnit.SECONDS)
        })

        val testObserver = paging.state.test()

        paging.actions.accept(Paging.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(2, TimeUnit.SECONDS)

        paging.actions.accept(Paging.Action.LOAD_NEXT_PAGE)
        paging.actions.accept(Paging.Action.FORCE_REFRESH)

        schedulers.testScheduler.advanceTimeTo(4, TimeUnit.SECONDS)

        testObserver.assertValues(
            State(
                content = null,
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = true,
                lastPage = DataPage(listOf(1, 2, 3), false)
            ),

            State(
                content = null,
                error = null,
                pagingError = null,
                loading = true,
                pageLoading = false,
                lastPage = null
            ),

            State(
                content = listOf(1, 2, 3),
                error = null,
                pagingError = null,
                loading = false,
                pageLoading = false,
                lastPage = DataPage(listOf(1, 2, 3), false)
            )
        )
    }
}