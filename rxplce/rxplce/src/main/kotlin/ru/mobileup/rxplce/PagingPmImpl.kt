package ru.mobileup.rxplce

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import me.dmdev.rxpm.PresentationModel

class PagingPmImpl<T>(
    private val pagingSource: ((offset: Int, last: T?) -> Single<List<T>>)
) : PresentationModel() {

    val pagingState = State<PagingState<T>>(PagingState())
    val actions = Action<ActionType>()

    data class PagingState<T>(
        val internalAction: InternalAction? = null,
        val data: List<T>? = null,
        val refreshingError: Throwable? = null,
        val pageLoadingError: Throwable? = null,
        val refreshing: Boolean = false,
        val pageIsLoading: Boolean = false
    )

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
                    ActionType.LOAD_PAGE -> state.refreshing.not() && state.pageIsLoading.not()
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
                        pagingSource(state.data?.size ?: 0, state.data?.lastOrNull())
                            .toObservable()
                            .map<InternalAction> {
                                InternalAction.PageLoadingSuccess(
                                    state.data?.plus(it).orEmpty()
                                )
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
                            internalAction = action,
                            refreshing = true,
                            pageIsLoading = false,
                            refreshingError = null
                        )
                    }
                    is InternalAction.RefreshFail -> {
                        state.copy(
                            internalAction = action,
                            refreshing = false,
                            refreshingError = action.error
                        )
                    }
                    is InternalAction.RefreshSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            internalAction = action,
                            refreshing = false,
                            data = action.list as List<T>
                        )
                    }
                    InternalAction.StartPageLoading -> {
                        state.copy(
                            internalAction = action,
                            pageIsLoading = true,
                            pageLoadingError = null
                        )
                    }
                    is InternalAction.PageLoadingFail -> {
                        state.copy(
                            internalAction = action,
                            pageIsLoading = false,
                            pageLoadingError = action.error
                        )
                    }
                    is InternalAction.PageLoadingSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        state.copy(
                            internalAction = action,
                            pageIsLoading = false,
                            data = action.list as List<T>
                        )
                    }
                }
            }
            .distinctUntilChanged()
            .subscribe(pagingState.consumer)
            .untilDestroy()
    }

    enum class ActionType { REFRESH, LOAD_PAGE }

    sealed class InternalAction {

        object StartRefresh : InternalAction()
        class RefreshSuccess<T>(val list: List<T>) : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()

        object StartPageLoading : InternalAction()
        class PageLoadingSuccess<T>(val list: List<T>) : InternalAction()
        class PageLoadingFail(val error: Throwable) : InternalAction()

        override fun toString(): String {
            return javaClass.simpleName
        }

    }
}