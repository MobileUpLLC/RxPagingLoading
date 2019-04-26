package ru.mobileup.rxplce.sample.loading

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.LcePm
import ru.mobileup.rxplce.LcePmImpl
import ru.mobileup.rxplce.LceScreenPm
import ru.mobileup.rxplce.LceScreenPmImpl
import ru.mobileup.rxplce.sample.loading.LoadingSamplePm.ContentString

class LoadingSamplePm private constructor(
    private val lceScreenPm: LceScreenPmImpl<ContentString>
) : PresentationModel(), LceScreenPm<ContentString> by lceScreenPm {

    data class ContentString(val text: String) : LcePm.DataMaybeEmpty {
        override fun isEmpty(): Boolean {
            return text.isEmpty()
        }
    }

    companion object {
        fun createInstance(repository: DataRepository): LoadingSamplePm {
            return LoadingSamplePm(
                LceScreenPmImpl(
                    LcePmImpl(
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