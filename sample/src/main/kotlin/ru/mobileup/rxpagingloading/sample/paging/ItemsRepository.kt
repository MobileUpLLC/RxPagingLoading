package ru.mobileup.rxpagingloading.sample.paging

import io.reactivex.Single
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class Item(val number: Int)
data class ItemsPage(val list: List<Item>, val totalCount: Int)

class ItemsRepository {

    private val random = Random(System.currentTimeMillis())
    private val totalCount: Int = 100

    enum class Mode { NORMAL, ERROR, EMPTY_DATA, RANDOM_ERROR }

    var mode: Mode = Mode.NORMAL

    fun loadPage(limit: Int = 20, last: Item?): Single<ItemsPage> {
        return Single.just(Unit)
            .delay(3, TimeUnit.SECONDS)
            .map {
                if (mode == Mode.NORMAL || mode == Mode.RANDOM_ERROR) {
                    List(limit) { index ->
                        Item(number = (last?.number ?: 0) + index + 1)
                    }.filter { it.number <= totalCount }
                } else {
                    listOf()
                }
            }
            .flatMap {
                when (mode) {
                    Mode.ERROR -> {
                        Single.error(IOException())
                    }
                    Mode.RANDOM_ERROR -> {
                        if (random.nextBoolean()) {
                            Single.error(IOException())
                        } else {
                            Single.just(ItemsPage(it, totalCount))
                        }
                    }
                    else -> Single.just(ItemsPage(it, totalCount))
                }
            }
    }
}