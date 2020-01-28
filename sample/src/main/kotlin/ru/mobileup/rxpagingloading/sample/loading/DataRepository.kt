package ru.mobileup.rxpagingloading.sample.loading

import io.reactivex.Single
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DataRepository {

    private val random = Random(System.currentTimeMillis())

    fun loadData(): Single<String> {
        return Single.just(Unit)
            .delay(3, TimeUnit.SECONDS)
            .flatMap {
                when (random.nextInt(3)) {
                    0 -> Single.just(CONTENT_STRING)
                    1 -> Single.just("")
                    else -> Single.error(IOException())
                }
            }
    }
}

private const val CONTENT_STRING =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."