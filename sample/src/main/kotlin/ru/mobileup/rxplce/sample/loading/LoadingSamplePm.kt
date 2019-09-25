package ru.mobileup.rxplce.sample.loading

import ru.mobileup.rxplce.*
import ru.mobileup.rxplce.pm.LoadingPm
import ru.mobileup.rxplce.sample.BasePresentationModel
import ru.mobileup.rxplce.sample.loading.LoadingSamplePm.ContentString

class LoadingSamplePm(
    repository: DataRepository
) : BasePresentationModel(), LoadingPm<ContentString> {

    private val loader = LoadingOrdinary<ContentString>(
        source = repository.loadData().map { ContentString(it) }
    )

    override val content = stateOf(loader.contentChanges())

    override val isLoading = stateOf(loader.isLoading())
    override val isRefreshing = stateOf(loader.isRefreshing())

    override val refreshEnabled = stateOf(loader.refreshEnabled())

    override val contentViewVisible = stateOf(loader.contentViewVisible())
    override val emptyViewVisible = stateOf(loader.emptyViewVisible())
    override val errorViewVisible = stateOf(loader.errorViewVisible())

    override val refreshAction = actionTo<Unit, Loading.Action>(loader.actions) {
        startWith(Unit).map { Loading.Action.REFRESH }
    }

    override val retryAction = actionTo<Unit, Loading.Action>(loader.actions) {
        map { Loading.Action.REFRESH }
    }

    data class ContentString(val text: String) : Emptyable {
        override fun isEmpty(): Boolean {
            return text.isEmpty()
        }
    }
}

