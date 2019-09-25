package ru.mobileup.rxplce

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxplce.Paging.Page
import ru.mobileup.rxplce.Paging.State

/**
 * This class implements data [loading and paging][Paging].
 *
 * @param[pageSource] a lambda that returns the source to load the next page.
 */
class PagingImpl<T>(
    private val pageSource: ((offset: Int, lastPage: Page<T>?) -> Single<Page<T>>)
) : Paging<T> {

    private val stateSubject = BehaviorSubject.createDefault<State<T>>(State()).toSerialized()
    private val actionSubject = PublishSubject.create<Paging.Action>().toSerialized()

    override val state: Observable<State<T>>
    override val actions: Consumer<Paging.Action> = Consumer { actionSubject.onNext(it) }

    init {

        state = actionSubject
            .withLatestFrom(
                stateSubject,
                BiFunction { action: Paging.Action, state: State<T> ->
                    action to state
                }
            )
            .filter { (action, state) ->
                when (action) {
                    Paging.Action.LOAD_NEXT_PAGE -> {
                        state.loading.not()
                                && state.pageLoading.not()
                                && state.isEndReached.not()
                    }
                    Paging.Action.REFRESH -> state.loading.not()
                }
            }
            .switchMap { (action, state) ->

                when (action) {
                    Paging.Action.REFRESH -> {
                        pageSource(0, null)
                            .toObservable()
                            .map<InternalAction> { InternalAction.RefreshSuccess(it) }
                            .startWith(InternalAction.RefreshStart)
                            .onErrorReturn { InternalAction.RefreshFail(it) }
                    }

                    Paging.Action.LOAD_NEXT_PAGE -> {
                        pageSource(state.content?.size ?: 0, state.lastPage)
                            .toObservable()
                            .map<InternalAction> {
                                InternalAction.PageLoadingSuccess(it)
                            }
                            .startWith(InternalAction.PageLoadingStart)
                            .onErrorReturn { InternalAction.PageLoadingFail(it) }
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .scan(State<T>()) { state, action ->
                when (action) {
                    InternalAction.RefreshStart -> {
                        state.copy(
                            loading = true,
                            pageLoading = false,
                            error = null
                        )
                    }
                    is InternalAction.RefreshFail -> {
                        state.copy(
                            loading = false,
                            error = action.error
                        )
                    }
                    is InternalAction.RefreshSuccess<*> -> {

                        @Suppress("UNCHECKED_CAST")
                        val page = action.page as Page<T>

                        State(
                            content = page.items,
                            lastPage = page
                        )
                    }
                    InternalAction.PageLoadingStart -> {
                        state.copy(
                            pageLoading = true,
                            pagingError = null
                        )
                    }
                    is InternalAction.PageLoadingFail -> {
                        state.copy(
                            pageLoading = false,
                            pagingError = action.error
                        )
                    }
                    is InternalAction.PageLoadingSuccess<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val page = action.page as Page<T>

                        state.copy(
                            pageLoading = false,
                            content = state.content?.plus(page.items),
                            lastPage = page
                        )
                    }
                }
            }
            .doOnNext { stateSubject.onNext(it) }
            .share()

    }

    private sealed class InternalAction {
        object RefreshStart : InternalAction()
        class RefreshSuccess<T>(val page: Page<T>) : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()
        object PageLoadingStart : InternalAction()
        class PageLoadingSuccess<T>(val page: Page<T>) : InternalAction()
        class PageLoadingFail(val error: Throwable) : InternalAction()
    }
}