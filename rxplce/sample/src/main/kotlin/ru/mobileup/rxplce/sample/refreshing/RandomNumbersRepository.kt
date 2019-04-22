package ru.mobileup.rxplce.sample.refreshing

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RandomNumbersRepository {

    private val relay = BehaviorRelay.create<Array<Int>>().toSerialized()
    private val random = Random(System.currentTimeMillis())

    enum class Mode { NORMAL, ERROR, EMPTY_DATA, RANDOM_ERROR }

    var mode: Mode = Mode.NORMAL

    fun numbersChanges(): Observable<Array<Int>> {
        return relay.hide()
    }

    fun refreshNumbers(): Completable {
        return Single.just(Unit)
            .delay(3, TimeUnit.SECONDS)
            .flatMap {
                when (mode) {
                    Mode.NORMAL -> Single.just(generateRandomNumbers())
                    Mode.EMPTY_DATA -> Single.just(arrayOf())
                    Mode.ERROR -> {
                        Single.error(IOException())
                    }
                    Mode.RANDOM_ERROR -> {
                        if (random.nextBoolean()) {
                            Single.error(IOException())
                        } else {
                            Single.just(generateRandomNumbers())
                        }
                    }
                }
            }
            .doOnSuccess {
                relay.accept(it)
            }
            .ignoreElement()
    }

    fun updateNumbers(numbers: Array<Int>) {
        relay.accept(numbers)
    }

    fun generateRandomNumbers(): Array<Int> {
        return Array(10) {
            random.nextInt()
        }
    }

}