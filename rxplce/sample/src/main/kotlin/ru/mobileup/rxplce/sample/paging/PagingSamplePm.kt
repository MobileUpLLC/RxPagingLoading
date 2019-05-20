package ru.mobileup.rxplce.sample.paging

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.Paging
import ru.mobileup.rxplce.PagingImpl
import ru.mobileup.rxplce.PagingPm
import ru.mobileup.rxplce.PagingPmImpl

class PagingSamplePm private constructor(
    private val pagingPm: PagingPmImpl<Item>
) : PresentationModel(), PagingPm<Item> by pagingPm {

    class PageInfo(
        override val items: List<Item>,
        override val isEndReached: Boolean
    ) : Paging.Page<Item>

    companion object {
        fun createInstance(repository: ItemsRepository): PagingSamplePm {
            return PagingSamplePm(
                PagingPmImpl(
                    PagingImpl(
                        pageSource = { offset, lastPage ->
                            repository
                                .loadPage(last = lastPage?.lastItem)
                                .map {
                                    PageInfo(
                                        items = it.list,
                                        isEndReached = (offset + it.list.size) == it.totalCount
                                    )
                                }
                        }
                    )
                )
            )
        }
    }

    val showError = Command<String>()

    override fun onCreate() {
        super.onCreate()
        pagingPm.attachToParent(this)

        pagingPm.errorNoticeObservable
            .subscribe { showError.consumer.accept("Refreshing Error") }
            .untilDestroy()
    }
}