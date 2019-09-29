package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.widget.dialogControl
import ru.mobileup.rxplce.*
import ru.mobileup.rxplce.sample.BasePresentationModel

class RefreshingPm(repository: RandomNumbersRepository) : BasePresentationModel() {

    private val loader = LoadingAssembled(
        refresh = repository.refreshNumbers(),
        updates = repository.numbersChanges()
    )

    val content = stateOf(loader.contentChanges())

    val isLoading = stateOf(loader.isLoading())
    val isRefreshing = stateOf(loader.isRefreshing())

    val refreshEnabled = stateOf(loader.refreshEnabled())

    val contentViewVisible = stateOf(loader.contentVisible())
    val emptyViewVisible = stateOf(loader.emptyVisible())
    val errorViewVisible = stateOf(loader.errorVisible())

    val errorDialog = dialogControl<String, Unit>()

    val refreshAction = actionTo<Unit, Loading.Action>(loader.actions) {
        startWith(Unit).map { Loading.Action.REFRESH }
    }

    val retryAction = actionTo<Unit, Loading.Action>(loader.actions) {
        map { Loading.Action.REFRESH }
    }

    override fun onCreate() {
        super.onCreate()

        loader.errorChanges()
            .subscribe { errorDialog.show("Refreshing Error") }
            .untilDestroy()
    }
}