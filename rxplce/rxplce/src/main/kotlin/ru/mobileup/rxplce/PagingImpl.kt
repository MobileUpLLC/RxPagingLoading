package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxplce.Paging.Page
import ru.mobileup.rxplce.Paging.PagingState

class PagingImpl<T>(
    private val pagingSource: ((offset: Int, lastPage: Page<T>?) -> Single<Page<T>>)
) : Paging<T> {

    private val stateSubject = BehaviorSubject.createDefault<PagingState<T>>(PagingState()).toSerialized()
    private val actionSubject = PublishSubject.create<Paging.Action>().toSerialized()

    override val actions: Consumer<Paging.Action>
        get() = Consumer { actionSubject.onNext(it) }

    override val state: Observable<PagingState<T>>

    init {

        state = actionSubject
            .withLatestFrom(
                stateSubject,
                BiFunction { action: Paging.Action, state: PagingState<T> ->
                    action to state
                }
            )
            .filter { (action, state) ->
                when (action) {
                    Paging.Action.LOAD_NEXT_PAGE -> {
                        state.refreshing.not()
                                && state.pageIsLoading.not()
                                && state.isReachedEnd.not()
                    }
                    Paging.Action.REFRESH -> state.refreshing.not()
                }
            }
            .switchMap { (action, state) ->

                when (action) {
                    Paging.Action.REFRESH -> {
                        pagingSource(0, null)
                            .toObservable()
                            .map<InternalAction> { InternalAction.RefreshSuccess(it) }
                            .startWith(InternalAction.StartRefresh)
                            .onErrorReturn { InternalAction.RefreshFail(it) }
                    }

                    Paging.Action.LOAD_NEXT_PAGE -> {
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
            .doOnNext { stateSubject.onNext(it) }
            .share()

    }

    private sealed class InternalAction {
        object StartRefresh : InternalAction()
        class RefreshSuccess<T>(val page: Page<T>) : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()
        object StartPageLoading : InternalAction()
        class PageLoadingSuccess<T>(val page: Page<T>) : InternalAction()
        class PageLoadingFail(val error: Throwable) : InternalAction()
    }
}