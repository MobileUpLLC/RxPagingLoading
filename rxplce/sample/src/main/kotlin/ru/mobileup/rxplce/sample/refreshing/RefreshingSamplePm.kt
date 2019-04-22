package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.LceScreenPm
import ru.mobileup.rxplce.LceScreenPmImpl

class RefreshingSamplePm private constructor(
    private val lceScreenPm: LceScreenPmImpl<Array<Int>>
) : PresentationModel(), LceScreenPm<Array<Int>> by lceScreenPm {

    companion object {
        fun createInstance(repository: RandomNumbersRepository): RefreshingSamplePm {
            return RefreshingSamplePm(
                LceScreenPmImpl(
                    refreshData = repository.refreshNumbers(),
                    dataChanges = repository.numbersChanges()
                )
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        lceScreenPm.attachToParent(this)
    }
}