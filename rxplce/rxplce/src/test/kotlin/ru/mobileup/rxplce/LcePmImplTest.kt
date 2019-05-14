package ru.mobileup.rxplce

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Single
import me.dmdev.rxpm.PresentationModel.Lifecycle.CREATED
import me.dmdev.rxpm.test.PmTestHelper
import org.junit.Rule
import org.junit.Test
import ru.mobileup.rxplce.util.SchedulersRule
import java.io.IOException
import java.util.concurrent.TimeUnit

class LcePmImplTest {

    private val loadingDataSource = Single.just("foo")
    private val error = IOException()
    private val loadingErrorSource = Single.error<String>(error)

    @get:Rule
    val schedulers = SchedulersRule(true)

    @Test fun initialState() {

        val lcePm = LcePmImpl(loadingData = loadingDataSource)
        val pmTestHelper = PmTestHelper(lcePm)
        pmTestHelper.setLifecycleTo(CREATED)

        val testObserver = lcePm.dataState.observable.test()

        testObserver.assertValues(
            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = false
            )
        )
    }

    @Test fun loadingSuccess() {

        val lcePm = LcePmImpl(loadingData = loadingDataSource)
        val pmTestHelper = PmTestHelper(lcePm)

        pmTestHelper.setLifecycleTo(CREATED)

        val testObserver = lcePm.dataState.observable.test()

        lcePm.refreshes.consumer.accept(Unit)

        testObserver.assertValues(

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = false
            ),

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = true
            ),

            LcePm.DataState(
                data = "foo",
                refreshingError = null,
                refreshing = false
            )
        )
    }

    @Test fun loadingFail() {


        val lcePm = LcePmImpl(loadingData = loadingErrorSource)
        val pmTestHelper = PmTestHelper(lcePm)

        pmTestHelper.setLifecycleTo(CREATED)

        val testObserver = lcePm.dataState.observable.test()

        lcePm.refreshes.consumer.accept(Unit)

        testObserver.assertValues(

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = false
            ),

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = true
            ),

            LcePm.DataState(
                data = null,
                refreshingError = error,
                refreshing = false
            )
        )
    }

    @Test fun refreshingSuccess() {

        val relay = BehaviorRelay.create<String>()
        val lcePm = LcePmImpl(
            refreshData = Completable.create {
                relay.accept("foo")
                it.onComplete()
            },
            dataChanges = relay
        )

        val pmTestHelper = PmTestHelper(lcePm)

        pmTestHelper.setLifecycleTo(CREATED)

        val testObserver = lcePm.dataState.observable.test()

        lcePm.refreshes.consumer.accept(Unit)

        testObserver.assertValues(

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = false
            ),

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = true
            ),

            LcePm.DataState(
                data = "foo",
                refreshingError = null,
                refreshing = true
            ),

            LcePm.DataState(
                data = "foo",
                refreshingError = null,
                refreshing = false
            )
        )
    }

    @Test fun refreshingFail() {

        val relay = BehaviorRelay.create<String>()
        val lcePm = LcePmImpl(
            refreshData = Completable.create {
                it.onError(error)
            },
            dataChanges = relay
        )

        val pmTestHelper = PmTestHelper(lcePm)

        pmTestHelper.setLifecycleTo(CREATED)

        val testObserver = lcePm.dataState.observable.test()

        lcePm.refreshes.consumer.accept(Unit)

        testObserver.assertValues(

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = false
            ),

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = true
            ),

            LcePm.DataState(
                data = null,
                refreshingError = error,
                refreshing = false
            )
        )
    }

    @Test fun externalUpdateData() {

        val relay = BehaviorRelay.create<String>()
        val lcePm = LcePmImpl(
            refreshData = Completable.create {
                relay.accept("")
                it.onComplete()
            },
            dataChanges = relay
        )

        val pmTestHelper = PmTestHelper(lcePm)

        pmTestHelper.setLifecycleTo(CREATED)

        val testObserver = lcePm.dataState.observable.test()

        relay.accept("bar")

        testObserver.assertValues(

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = false
            ),

            LcePm.DataState(
                data = "bar",
                refreshingError = null,
                refreshing = false
            )
        )
    }

    @Test fun blockRepeatedRefreshing() {

        val relay = BehaviorRelay.create<String>()
        val lcePm = LcePmImpl(
            refreshData = Single.just("foo")
                .delay(1, TimeUnit.SECONDS)
                .doOnSuccess {
                    relay.accept("foo")
                }
                .ignoreElement(),
            dataChanges = relay
        )

        val pmTestHelper = PmTestHelper(lcePm)

        pmTestHelper.setLifecycleTo(CREATED)

        val testObserver = lcePm.dataState.observable.test()

        lcePm.refreshes.consumer.accept(Unit)
        lcePm.refreshes.consumer.accept(Unit)
        lcePm.refreshes.consumer.accept(Unit)

        schedulers.testScheduler.advanceTimeTo(3, TimeUnit.SECONDS)

        testObserver.assertValues(

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = false
            ),

            LcePm.DataState(
                data = null,
                refreshingError = null,
                refreshing = true
            ),

            LcePm.DataState(
                data = "foo",
                refreshingError = null,
                refreshing = true
            ),

            LcePm.DataState(
                data = "foo",
                refreshingError = null,
                refreshing = false
            )
        )
    }
}