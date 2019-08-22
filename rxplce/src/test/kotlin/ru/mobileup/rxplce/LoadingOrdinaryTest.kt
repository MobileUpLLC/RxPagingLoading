package ru.mobileup.rxplce

import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import ru.mobileup.rxplce.util.SchedulersRule
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoadingOrdinaryTest {

    private val source = Single.just("foo")
    private val error = IOException()
    private val errorSource = Single.error<String>(error)

    @get:Rule val schedulers = SchedulersRule(true)

    @Test fun initialState() {

        val loading = LoadingOrdinary(source)
        val testObserver = loading.state.test()

        testObserver.assertValues(
            Loading.State(
                content = null,
                error = null,
                loading = false
            )
        )
    }

    @Test fun loadingSuccess() {

        val loading = LoadingOrdinary(source)
        val testObserver = loading.state.test()

        loading.actions.accept(Loading.Action.REFRESH)

        testObserver.assertValues(

            Loading.State(
                content = null,
                error = null,
                loading = false
            ),

            Loading.State(
                content = null,
                error = null,
                loading = true
            ),

            Loading.State(
                content = "foo",
                error = null,
                loading = false
            )
        )
    }

    @Test fun loadingFail() {

        val loading = LoadingOrdinary(errorSource)
        val testObserver = loading.state.test()

        loading.actions.accept(Loading.Action.REFRESH)

        testObserver.assertValues(

            Loading.State(
                content = null,
                error = null,
                loading = false
            ),

            Loading.State(
                content = null,
                error = null,
                loading = true
            ),

            Loading.State(
                content = null,
                error = error,
                loading = false
            )
        )
    }

    @Test fun blockRepeatedRefreshing() {

        val loading = LoadingOrdinary(
            Single.just("foo").delay(1, TimeUnit.SECONDS)
        )

        val testObserver = loading.state.test()

        loading.actions.accept(Loading.Action.REFRESH)
        loading.actions.accept(Loading.Action.REFRESH)
        loading.actions.accept(Loading.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(3, TimeUnit.SECONDS)

        testObserver.assertValues(

            Loading.State(
                content = null,
                error = null,
                loading = false
            ),

            Loading.State(
                content = null,
                error = null,
                loading = true
            ),

            Loading.State(
                content = "foo",
                error = null,
                loading = false
            )
        )
    }
}