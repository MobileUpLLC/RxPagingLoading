package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.widget.dialogControl
import ru.mobileup.rxplce.LceImpl
import ru.mobileup.rxplce.LcePm
import ru.mobileup.rxplce.LcePmImpl

class RefreshingSamplePm private constructor(
    private val lceScreenPm: LcePmImpl<Array<Int>>
) : PresentationModel(), LcePm<Array<Int>> by lceScreenPm {

    companion object {
        fun createInstance(repository: RandomNumbersRepository): RefreshingSamplePm {
            return RefreshingSamplePm(
                LcePmImpl(
                    LceImpl(
                        refreshData = repository.refreshNumbers(),
                        dataChanges = repository.numbersChanges()
                    )
                )
            )
        }
    }

    val errorDialog = dialogControl<String, Unit>()

    override fun onCreate() {
        super.onCreate()

        lceScreenPm.attachToParent(this)

        lceScreenPm.showError.observable
            .subscribe { errorDialog.show("Refreshing Error") }
            .untilDestroy()
    }
}