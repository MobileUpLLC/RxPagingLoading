package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.widget.dialogControl
import ru.mobileup.rxplce.LoadingAssembled
import ru.mobileup.rxplce.LoadingPm
import ru.mobileup.rxplce.LoadingPmImpl

class RefreshingSamplePm private constructor(
    private val lceScreenPm: LoadingPmImpl<Array<Int>>
) : PresentationModel(), LoadingPm<Array<Int>> by lceScreenPm {

    companion object {
        fun createInstance(repository: RandomNumbersRepository): RefreshingSamplePm {
            return RefreshingSamplePm(
                LoadingPmImpl(
                    LoadingAssembled(
                        refresh = repository.refreshNumbers(),
                        updates = repository.numbersChanges()
                    )
                )
            )
        }
    }

    val errorDialog = dialogControl<String, Unit>()

    override fun onCreate() {
        super.onCreate()

        lceScreenPm.attachToParent(this)

        lceScreenPm.errorNoticeObservable
            .subscribe { errorDialog.show("Refreshing Error") }
            .untilDestroy()
    }
}