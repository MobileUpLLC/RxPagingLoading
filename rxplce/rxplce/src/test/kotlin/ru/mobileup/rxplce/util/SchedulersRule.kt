package ru.mobileup.rxplce.util

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.rules.ExternalResource

class SchedulersRule(private val useTestScheduler: Boolean = false) : ExternalResource() {

    private lateinit var _testScheduler: TestScheduler

    val testScheduler: TestScheduler
        get() {
            if (!useTestScheduler) throw IllegalStateException("TestScheduler is switched off.")
            return _testScheduler
        }

    override fun before() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        val computationScheduler = if (useTestScheduler) {
            _testScheduler = TestScheduler()
            _testScheduler
        } else {
            Schedulers.trampoline()
        }
        RxJavaPlugins.setComputationSchedulerHandler { computationScheduler }
    }

    override fun after() {
        RxJavaPlugins.reset()
    }
}