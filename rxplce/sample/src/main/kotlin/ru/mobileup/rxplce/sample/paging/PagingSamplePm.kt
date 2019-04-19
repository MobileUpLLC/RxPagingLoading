package ru.mobileup.rxplce.sample.paging

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.PagingPmImpl
import ru.mobileup.rxplce.PagingScreenPm
import ru.mobileup.rxplce.PagingScreenPmImpl

class PagingSamplePm(
    private val pagingScreenPm: PagingScreenPmImpl<Item>
) : PresentationModel(), PagingScreenPm<Item> by pagingScreenPm {

    class PageInfo(
        override val list: List<Item>,
        override val isReachedEnd: Boolean
    ) : PagingPmImpl.Page<Item>

    companion object {
        fun createInstance(repository: ItemsRepository): PagingSamplePm {
            return PagingSamplePm(
                PagingScreenPmImpl(
                    pagingSource = { offset, lastPage ->
                        repository
                            .loadPage(last = lastPage?.lastItem)
                            .map {
                                PageInfo(
                                    list = it.list,
                                    isReachedEnd = (offset + it.list.size) == it.totalCount
                                )
                            }
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