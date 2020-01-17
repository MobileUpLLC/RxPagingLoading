package ru.mobileup.rxplce.sample.loading

import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import ru.mobileup.rxplce.*

class LoadingPm(repository: DataRepository) : PresentationModel() {

    data class ContentString(val text: String) : Emptyable {
        override fun isEmpty(): Boolean {
            return text.isEmpty()
        }
    }

    private val loading = LoadingOrdinary<ContentString>(
        source = repository.loadData().map { ContentString(it) }
    )

    val content = state { loading.contentChanges() }

    val isLoading = state { loading.isLoading() }

    val contentViewVisible = state { loading.contentVisible() }
    val emptyViewVisible = state { loading.emptyVisible() }
    val errorViewVisible = state { loading.errorVisible() }

    val retryAction = action<Unit> {
        this.startWith(Unit)
            .map { Loading.Action.REFRESH }
            .doOnNext(loading.actions)
    }

    val forceRefreshAction = action<Unit> {
        this.startWith(Unit)
            .map { Loading.Action.FORCE_REFRESH }
            .doOnNext(loading.actions)
    }
}

