package ru.mobileup.rxplce.sample.refreshing

import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import ru.mobileup.rxplce.*

class RefreshingPm(repository: RandomNumbersRepository) : PresentationModel() {

    private val loading = LoadingAssembled(
        refresh = repository.refreshNumbers(),
        updates = repository.numbersChanges()
    )

    val content = state(diffStrategy = null) { loading.contentChanges() }

    val isLoading = state { loading.isLoading() }
    val isRefreshing = state { loading.isRefreshing() }

    val refreshEnabled = state { loading.refreshEnabled() }

    val contentViewVisible = state { loading.contentVisible() }
    val emptyViewVisible = state { loading.emptyVisible() }
    val errorViewVisible = state { loading.errorVisible() }

    val errorDialog = dialogControl<String, Unit>()

    val refreshAction = action<Unit> {
        this.startWith(Unit)
            .map { Loading.Action.REFRESH }
            .doOnNext(loading.actions)
    }

    val retryAction = action<Unit> {
        this.map { Loading.Action.REFRESH }
            .doOnNext(loading.actions)
    }

    val forceRefreshAction = action<Unit> {
        this.startWith(Unit)
            .map { Loading.Action.FORCE_REFRESH }
            .doOnNext(loading.actions)
    }

    override fun onCreate() {
        super.onCreate()

        loading.errorChanges()
            .subscribe { errorDialog.show("Refreshing Error") }
            .untilDestroy()
    }
}