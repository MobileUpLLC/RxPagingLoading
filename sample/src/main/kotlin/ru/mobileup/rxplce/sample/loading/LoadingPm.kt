package ru.mobileup.rxplce.sample.loading

import ru.mobileup.rxplce.*
import ru.mobileup.rxplce.sample.BasePresentationModel

class LoadingPm(repository: DataRepository) : BasePresentationModel() {

    data class ContentString(val text: String) : Emptyable {
        override fun isEmpty(): Boolean {
            return text.isEmpty()
        }
    }

    private val loader = LoadingOrdinary<ContentString>(
        source = repository.loadData().map { ContentString(it) }
    )

    val content = stateOf(loader.contentChanges())

    val isLoading = stateOf(loader.isLoading())

    val contentViewVisible = stateOf(loader.contentVisible())
    val emptyViewVisible = stateOf(loader.emptyVisible())
    val errorViewVisible = stateOf(loader.errorVisible())

    val retryAction = actionTo<Unit, Loading.Action>(loader.actions) {
        startWith(Unit).map { Loading.Action.REFRESH }
    }
}

