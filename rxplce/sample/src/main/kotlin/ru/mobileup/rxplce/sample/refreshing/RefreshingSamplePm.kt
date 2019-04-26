package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.widget.dialogControl
import ru.mobileup.rxplce.LcePmImpl
import ru.mobileup.rxplce.LceScreenPm
import ru.mobileup.rxplce.LceScreenPmImpl

class RefreshingSamplePm private constructor(
    private val lceScreenPm: LceScreenPmImpl<Array<Int>>
) : PresentationModel(), LceScreenPm<Array<Int>> by lceScreenPm {

    companion object {
        fun createInstance(repository: RandomNumbersRepository): RefreshingSamplePm {
            return RefreshingSamplePm(
                LceScreenPmImpl(
                    LcePmImpl(
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