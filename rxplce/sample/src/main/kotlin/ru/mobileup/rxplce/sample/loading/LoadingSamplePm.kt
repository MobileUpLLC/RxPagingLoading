package ru.mobileup.rxplce.sample.loading

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.LceImpl
import ru.mobileup.rxplce.LcePm
import ru.mobileup.rxplce.LcePmImpl
import ru.mobileup.rxplce.MaybeEmpty
import ru.mobileup.rxplce.sample.loading.LoadingSamplePm.ContentString

class LoadingSamplePm private constructor(
    private val lceScreenPm: LcePmImpl<ContentString>
) : PresentationModel(), LcePm<ContentString> by lceScreenPm {

    data class ContentString(val text: String) : MaybeEmpty {
        override fun isEmpty(): Boolean {
            return text.isEmpty()
        }
    }

    companion object {
        fun createInstance(repository: DataRepository): LoadingSamplePm {
            return LoadingSamplePm(
                LcePmImpl(
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