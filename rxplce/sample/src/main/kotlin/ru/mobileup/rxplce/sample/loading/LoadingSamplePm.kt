package ru.mobileup.rxplce.sample.loading

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.Lce
import ru.mobileup.rxplce.LceImpl
import ru.mobileup.rxplce.LceScreenPm
import ru.mobileup.rxplce.LceScreenPmImpl
import ru.mobileup.rxplce.sample.loading.LoadingSamplePm.ContentString

class LoadingSamplePm private constructor(
    private val lceScreenPm: LceScreenPmImpl<ContentString>
) : PresentationModel(), LceScreenPm<ContentString> by lceScreenPm {

    data class ContentString(val text: String) : Lce.DataMaybeEmpty {
        override fun isEmpty(): Boolean {
            return text.isEmpty()
        }
    }

    companion object {
        fun createInstance(repository: DataRepository): LoadingSamplePm {
            return LoadingSamplePm(
                LceScreenPmImpl(
                    LceImpl(
                        loadingData = repository.loadData().map { ContentString(it) }
                    )
                )
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        lceScreenPm.attachToParent(this)
    }
}