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

    private val loader = LoadingOrdinary<ContentString>(
        source = repository.loadData().map { ContentString(it) }
    )

    val content = state { loader.contentChanges() }

    val isLoading = state { loader.isLoading() }

    val contentViewVisible = state { loader.contentVisible() }
    val emptyViewVisible = state { loader.emptyVisible() }
    val errorViewVisible = state { loader.errorVisible() }

    val retryAction = action<Unit> {
        this.startWith(Unit)
            .map { Loading.Action.REFRESH }
            .doOnNext(loader.actions)
    }
}

