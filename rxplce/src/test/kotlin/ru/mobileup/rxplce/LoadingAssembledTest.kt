package ru.mobileup.rxplce

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import ru.mobileup.rxplce.util.SchedulersRule
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoadingAssembledTest {

    private val error = IOException()

    @get:Rule
    val schedulers = SchedulersRule(true)

    @Test fun initialState() {

        val relay = BehaviorRelay.create<String>()
        val loading = LoadingAssembled(
            refresh = Completable.create {
                relay.accept("foo")
                it.onComplete()
            },
            updates = relay
        )
        val testObserver = loading.state.test()

        testObserver.assertValues(
            Loading.State(
                content = null,
                error = null,
                loading = false
            )
        )
    }

    @Test fun refreshingSuccess() {

        val relay = BehaviorRelay.create<String>()
        val loading = LoadingAssembled(
            refresh = Completable.create {
                relay.accept("foo")
                it.onComplete()
            },
            updates = relay
        )

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
                loading = true
            ),

            Loading.State(
                content = "foo",
                error = null,
                loading = false
            )
        )
    }

    @Test fun refreshingFail() {

        val relay = BehaviorRelay.create<String>()
        val loading = LoadingAssembled(
            refresh = Completable.create {
                it.onError(error)
            },
            updates = relay
        )

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

    @Test fun externalUpdateData() {

        val relay = BehaviorRelay.create<String>()
        val loading = LoadingAssembled(
            refresh = Completable.create {
                relay.accept("foo")
                it.onComplete()
            },
            updates = relay
        )

        val testObserver = loading.state.test()

        relay.accept("bar")

        testObserver.assertValues(

            Loading.State(
                content = null,
                error = null,
                loading = false
            ),

            Loading.State(
                content = "bar",
                error = null,
                loading = false
            )
        )
    }

    @Test fun blockRepeatedRefreshing() {

        val relay = BehaviorRelay.create<String>()
        val loading = LoadingAssembled(
            refresh = Single.just("foo")
                .delay(1, TimeUnit.SECONDS)
                .doOnSuccess {
                    relay.accept("foo")
                }
                .ignoreElement(),
            updates = relay
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
                loading = true
            ),

            Loading.State(
                content = "foo",
                error = null,
                loading = false
            )
        )
    }

    @Test fun forceRefreshing() {

        val relay = BehaviorRelay.create<String>()
        val loading = LoadingAssembled(
            refresh = Completable.create {
                relay.accept("foo")
                it.onComplete()
            },
            updates = relay
        )

        val testObserver = loading.state.test()

        loading.actions.accept(Loading.Action.REFRESH)
        loading.actions.accept(Loading.Action.FORCE_REFRESH)

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
                loading = true
            ),

            Loading.State(
                content = "foo",
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
                loading = true
            ),

            Loading.State(
                content = "foo",
                error = null,
                loading = false
            )
        )
    }

    @Test fun forceRefreshOnLoading() {

        val relay = BehaviorRelay.create<String>()
        val loading = LoadingAssembled(
            refresh = Single.just("foo")
                .delay(3, TimeUnit.SECONDS)
                .doOnSuccess {
                    relay.accept("foo")
                }
                .ignoreElement(),
            updates = relay
        )

        val testObserver = loading.state.test()

        loading.actions.accept(Loading.Action.REFRESH)

        schedulers.testScheduler.advanceTimeTo(1, TimeUnit.SECONDS)

        loading.actions.accept(Loading.Action.FORCE_REFRESH)

        schedulers.testScheduler.advanceTimeTo(4, TimeUnit.SECONDS)

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