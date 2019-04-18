package ru.mobileup.rxplce.sample.paging

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.PagingScreenPm
import ru.mobileup.rxplce.PagingScreenPmImpl

class PagingSamplePm(
    private val pagingScreenPm: PagingScreenPmImpl<Item>
) : PresentationModel(), PagingScreenPm<Item> by pagingScreenPm {

    companion object {
        fun createInstance(repository: ItemsRepository): PagingSamplePm {
            return PagingSamplePm(
                PagingScreenPmImpl(
                    pagingSource = { offset: Int, last: Item? ->
                        repository.loadPage(
                            last = last
                        )
                    }
                )
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        pagingScreenPm.attachToParent(this)
    }
}