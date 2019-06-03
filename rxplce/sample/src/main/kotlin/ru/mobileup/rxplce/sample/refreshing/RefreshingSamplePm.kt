package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.widget.dialogControl
import ru.mobileup.rxplce.LoadingAssembled
import ru.mobileup.rxplce.pm.LoadingPm
import ru.mobileup.rxplce.pm.LoadingPmImpl

class RefreshingSamplePm private constructor(
    private val loadingPm: LoadingPmImpl<Array<Int>>
) : PresentationModel(), LoadingPm<Array<Int>> by loadingPm {

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

        loadingPm.attachToParent(this)

        loadingPm.errorNoticeObservable
            .subscribe { errorDialog.show("Refreshing Error") }
            .untilDestroy()
    }
}