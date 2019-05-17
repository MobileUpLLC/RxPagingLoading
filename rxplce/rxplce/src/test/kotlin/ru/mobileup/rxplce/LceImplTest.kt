package ru.mobileup.rxplce

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import ru.mobileup.rxplce.util.SchedulersRule
import java.io.IOException
import java.util.concurrent.TimeUnit

class LceImplTest {

    private val loadingDataSource = Single.just("foo")
    private val error = IOException()
    private val loadingErrorSource = Single.error<String>(error)

    @get:Rule
    val schedulers = SchedulersRule(true)

    @Test fun initialState() {

        val lce = LceImpl(loadingData = loadingDataSource)
        val testObserver = lce.state.test()

        testObserver.assertValues(
            Lce.State(
                content = null,
                loadingError = null,
                loading = false
            )
        )
    }

    @Test fun loadingSuccess() {

        val lce = LceImpl(loadingData = loadingDataSource)
        val testObserver = lce.state.test()

        lce.actions.accept(Lce.Action.REFRESH)

        testObserver.assertValues(

            Lce.State(
                content = null,
                loadingError = null,
                loading = false
            ),

            Lce.State(
                content = null,
                loadingError = null,
                loading = true
            ),

            Lce.State(
                content = "foo",
                loadingError = null,
                loading = false
            )
        )
    }

    @Test fun loadingFail() {


        val lce = LceImpl(loadingData = loadingErrorSource)
        val testObserver = lce.state.test()

        lce.actions.accept(Lce.Action.REFRESH)

        testObserver.assertValues(

            Lce.State(
                content = null,
                loadingError = null,
                loading = false
            ),

            Lce.State(
                content = null,
                loadingError = null,
                loading = true
            ),

            Lce.State(
                content = null,
                loadingError = error,
                loading = false
            )
        )
    }

    @Test fun refreshingSuccess() {

        val relay = BehaviorRelay.create<String>()
        val lce = LceImpl(
            refreshData = Completable.create {
                relay.accept("foo")
                it.onComplete()
            },
            dataChanges = relay
        )

        val testObserver = lce.state.test()

        lce.actions.accept(Lce.Action.REFRESH)

        testObserver.assertValues(

            Lce.State(
                content = null,
                loadingError = null,
                loading = false
            ),

            Lce.State(
                content = null,
                loadingError = null,
                loading = true
            ),

            Lce.State(
                content = "foo",
                loadingError = null,
                loading = true
            ),

            Lce.State(
                content = "foo",
                loadingError = null,
                loading = false
            )
        )
    }

    @Test fun refreshingFail() {

        val relay = BehaviorRelay.create<String>()
        val lce = LceImpl(
            refreshData = Completable.create {
                it.onError(error)
            },
            dataChanges = relay
        )

        val testObserver = lce.state.test()

        lce.actions.accept(Lce.Action.REFRESH)

        testObserver.assertValues(

            Lce.State(
                content = null,
                loadingError = null,
                loading = false
            ),

            Lce.State(
                content = null,
                loadingError = null,
                loading = true
            ),

            Lce.State(
                content = null,
                loadingError = error,
                loading = false
            )
        )
    }

    @Test fun externalUpdateData() {

        val relay = BehaviorRelay.create<String>()
        val lce = LceImpl(
            refreshData = Completable.create {
                relay.accept("foo")
                it.onComplete()
            },
            dataChanges = relay
        )

        val testObserver = lce.state.test()

        relay.accept("bar")

        testObserver.assertValues(

            Lce.State(
                content = null,
                loadingError = null,
                loading = false
            ),

            Lce.State(
                content = "bar",
                loadingError = null,
                loading = false
            )
        )
    }

    @Test fun blockRepeatedRefreshing() {

        val relay = BehaviorRelay.create<String>()
        val lce = LceImpl(
            refreshData = Single.just("foo")
                .delay(1, TimeUnit.SECONDS)
                .doOnSuccess {
                    relay.accept("foo")
                }
                .ignoreElement(),
            dataChanges = relay
        )

        val testObserver = lce.state.test()

        lce.actions.accept(Lce.Action.REFRESH)
        lce.actions.accept(Lce.Action.REFRESH)
        lce.actions.accept(Lce.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(3, TimeUnit.SECONDS)

        testObserver.assertValues(

            Lce.State(
                content = null,
                loadingError = null,
                loading = false
            ),

            Lce.State(
                content = null,
                loadingError = null,
                loading = true
            ),

            Lce.State(
                content = "foo",
                loadingError = null,
                loading = true
            ),

            Lce.State(
                content = "foo",
                loadingError = null,
                loading = false
            )
        )
    }
}