package ru.mobileup.rxplce

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import me.dmdev.rxpm.PresentationModel

class PagingPmImpl<T>(
    private val pagingSource: ((offset: Int, lastPage: Page<T>?) -> Single<Page<T>>)
) : PresentationModel() {

    val pagingState = State<PagingState<T>>(PagingState())
    val actions = Action<ActionType>()

    enum class ActionType { REFRESH, LOAD_PAGE }

    data class PagingState<T>(
        val data: List<T>? = null,
        val refreshingError: Throwable? = null,
        val pageLoadingError: Throwable? = null,
        val refreshing: Boolean = false,
        val pageIsLoading: Boolean = false,
        val lastPage: Page<T>? = null
    ) {
        val isReachedEnd: Boolean get() = lastPage?.isReachedEnd ?: false
    }

    interface Page<T> {
        val list: List<T>
        val lastItem: T? get() = list.lastOrNull()
        val isReachedEnd: Boolean
    }

    override fun onCreate() {
        super.onCreate()

        actions.observable
            .withLatestFrom(
                pagingState.observable,
                BiFunction { action: ActionType, state: PagingState<T> ->
                    action to state
                }
            )
            .filter { (action, state) ->
                when (action) {
                    ActionType.LOAD_PAGE -> {
                        state.refreshing.not()
                                && state.pageIsLoading.not()
                                && state.isReachedEnd.not()
                    }
                    ActionType.REFRESH -> state.refreshing.not()
                }
            }
            .switchMap { (action, state) ->

                when (action) {
                    ActionType.REFRESH -> {
                        pagingSource(0, null)
                            .toObservable()
                            .map<InternalAction> { InternalAction.RefreshSuccess(it) }
                            .startWith(InternalAction.StartRefresh)
                            .onErrorReturn { InternalAction.RefreshFail(it) }
                    }

                    ActionType.LOAD_PAGE -> {
                        pagingSource(state.data?.size ?: 0, state.lastPage)
                            .toObservable()
                            .map<InternalAction> {
                                InternalAction.PageLoadingSuccess(it)
                            }
                            .startWith(InternalAction.StartPageLoading)
                            .onErrorReturn { InternalAction.PageLoadingFail(it) }
                    }
                }
            }
            .scan(PagingState<T>()) { state, action ->
                when (action) {
                    InternalAction.StartRefresh -> {
                        state.copy(
                            refreshing = true,
                            pageIsLoading = false,
                            refreshingError = null
                        )
                    }
                    is InternalAction.RefreshFail -> {
                        state.copy(
                            refreshing = false,
                            refreshingError = action.error
                        )
                    }
                    is InternalAction.RefreshSuccess<*> -> {

                        @Suppress("UNCHECKED_CAST")
                        val page = action.page as Page<T>

                        PagingState(
                            data = page.list,
                            lastPage = page
                        )
                    }
                    InternalAction.StartPageLoading -> {
                        state.copy(
                            pageIsLoading = true,
                            pageLoadingError = null
                        )
                    }
                    is InternalAction.PageLoadingFail -> {
                        state.copy(
                            pageIsLoading = false,
                            pageLoadingError = action.error
                        )
                    }
                    is InternalAction.PageLoadingSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val page = action.page as Page<T>

                        state.copy(
                            pageIsLoading = false,
                            data = state.data?.plus(page.list),
                            lastPage = page
                        )
                    }
                }
            }
            .distinctUntilChanged()
            .subscribe(pagingState.consumer)
            .untilDestroy()
    }

    private sealed class InternalAction {

        object StartRefresh : InternalAction()
        class RefreshSuccess<T>(val page: Page<T>) : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()

        object StartPageLoading : InternalAction()
        class PageLoadingSuccess<T>(val page: Page<T>) : InternalAction()
        class PageLoadingFail(val error: Throwable) : InternalAction()

        override fun toString(): String {
            return javaClass.simpleName
        }

    }
}