package ru.mobileup.rxplce.sample.loading

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.LceScreenPm
import ru.mobileup.rxplce.LceScreenPmImpl

class LoadingSamplePm private constructor(
    private val lceScreenPm: LceScreenPmImpl<String>
) : PresentationModel(), LceScreenPm<String> by lceScreenPm {

    companion object {
        fun createInstance(repository: DataRepository): LoadingSamplePm {
            return LoadingSamplePm(
                LceScreenPmImpl(repository.loadData())
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        lceScreenPm.attachToParent(this)
    }
}