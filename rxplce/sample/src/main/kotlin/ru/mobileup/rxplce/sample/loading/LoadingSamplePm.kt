package ru.mobileup.rxplce.sample.loading

import me.dmdev.rxpm.PresentationModel
import ru.mobileup.rxplce.Emptyable
import ru.mobileup.rxplce.LoadingOrdinary
import ru.mobileup.rxplce.LoadingPm
import ru.mobileup.rxplce.LoadingPmImpl
import ru.mobileup.rxplce.sample.loading.LoadingSamplePm.ContentString

class LoadingSamplePm private constructor(
    private val lceScreenPm: LoadingPmImpl<ContentString>
) : PresentationModel(), LoadingPm<ContentString> by lceScreenPm {

    data class ContentString(val text: String) : Emptyable {
        override fun isEmpty(): Boolean {
            return text.isEmpty()
        }
    }

    companion object {
        fun createInstance(repository: DataRepository): LoadingSamplePm {
            return LoadingSamplePm(
                LoadingPmImpl(
                    LoadingOrdinary(
                        source = repository.loadData().map { ContentString(it) }
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