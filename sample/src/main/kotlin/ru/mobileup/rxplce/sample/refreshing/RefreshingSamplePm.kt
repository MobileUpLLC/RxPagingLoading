package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.widget.dialogControl
import ru.mobileup.rxplce.*
import ru.mobileup.rxplce.pm.LoadingPm
import ru.mobileup.rxplce.sample.BasePresentationModel

class RefreshingSamplePm(
    repository: RandomNumbersRepository
) : BasePresentationModel(), LoadingPm<Array<Int>> {

    private val loader = LoadingAssembled(
        refresh = repository.refreshNumbers(),
        updates = repository.numbersChanges()
    )

    override val content = stateOf(loader.contentChanges())

    override val isLoading = stateOf(loader.isLoading())
    override val isRefreshing = stateOf(loader.isRefreshing())

    override val refreshEnabled = stateOf(loader.refreshEnabled())

    override val contentViewVisible = stateOf(loader.contentViewVisible())
    override val emptyViewVisible = stateOf(loader.emptyViewVisible())
    override val errorViewVisible = stateOf(loader.errorViewVisible())

    val errorDialog = dialogControl<String, Unit>()

    override val refreshAction = actionTo<Unit, Loading.Action>(loader.actions) {
        startWith(Unit).map { Loading.Action.REFRESH }
    }

    override val retryAction = actionTo<Unit, Loading.Action>(loader.actions) {
        map { Loading.Action.REFRESH }
    }

    override fun onCreate() {
        super.onCreate()

        loader.errorChanges()
            .subscribe { errorDialog.show("Refreshing Error") }
            .untilDestroy()
    }
}