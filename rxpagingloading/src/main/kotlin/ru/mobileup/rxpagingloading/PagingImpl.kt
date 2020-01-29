package ru.mobileup.rxpagingloading

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.mobileup.rxpagingloading.Paging.*
import ru.mobileup.rxpagingloading.Paging.Action.*
import ru.mobileup.rxpagingloading.PagingImpl.InternalAction.*

/**
 * This class implements data [loading and paging][Paging].
 *
 * @param[pageSource] a lambda that returns the source to load the next page.
 */
class PagingImpl<T>(
    private val pageSource: ((offset: Int, lastPage: Page<T>?) -> Single<Page<T>>)
) : Paging<T> {

    private val stateSubject = BehaviorSubject.create<State<T>>().toSerialized()
    private val actionSubject = PublishSubject.create<Action>().toSerialized()

    override val state: Observable<State<T>>
    override val actions: Consumer<Action> = Consumer { actionSubject.onNext(it) }

    init {

        state = actionSubject
            .withLatestFrom(
                stateSubject,
                BiFunction { action: Action, state: State<T> ->
                    action to state
                }
            )
            .filter { (action, state) ->
                when (action) {
                    LOAD_NEXT_PAGE -> {
                        state.loading.not()
                                && state.pageLoading.not()
                                && state.isEndReached.not()
                    }
                    REFRESH -> state.loading.not()
                    FORCE_REFRESH -> true
                }
            }
            .switchMap { (action, state) ->

                when (action) {
                    REFRESH,
                    FORCE_REFRESH -> {
                        pageSource(0, null)
                            .toObservable()
                            .map<InternalAction> { RefreshSuccess(it) }
                            .startWith(RefreshStart(force = action == FORCE_REFRESH))
                            .onErrorReturn { RefreshFail(it) }
                    }

                    LOAD_NEXT_PAGE -> {
                        pageSource(state.content?.size ?: 0, state.lastPage)
                            .toObservable()
                            .map<InternalAction> {
                                PageLoadingSuccess(it)
                            }
                            .startWith(PageLoadingStart)
                            .onErrorReturn { PageLoadingFail(it) }
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .scan(State<T>()) { state, action ->
                when (action) {
                    is RefreshStart -> {
                        if (action.force) {
                            state.copy(
                                content = null,
                                loading = true,
                                pageLoading = false,
                                error = null,
                                pagingError = null,
                                lastPage = null
                            )
                        } else {
                            state.copy(
                                loading = true,
                                pageLoading = false,
                                error = null,
                                pagingError = null
                            )
                        }
                    }
                    is RefreshFail -> {
                        state.copy(
                            loading = false,
                            error = action.error
                        )
                    }
                    is RefreshSuccess<*> -> {

                        @Suppress("UNCHECKED_CAST")
                        val page = action.page as Page<T>

                        State(
                            content = page.items,
                            lastPage = page
                        )
                    }
                    PageLoadingStart -> {
                        state.copy(
                            pageLoading = true,
                            pagingError = null
                        )
                    }
                    is PageLoadingFail -> {
                        state.copy(
                            pageLoading = false,
                            pagingError = action.error
                        )
                    }
                    is PageLoadingSuccess<*> -> {
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
            .distinctUntilChanged { s1, s2 ->
                s1.content === s2.content
                        && s1.loading == s2.loading
                        && s1.error === s2.error
                        && s1.pageLoading == s2.pageLoading
                        && s1.pagingError === s2.pagingError
                        && s1.lastPage === s2.lastPage
            }
            .doOnNext { stateSubject.onNext(it) }
            .replay(1)
            .refCount()

    }

    private sealed class InternalAction {
        class RefreshStart(val force: Boolean) : InternalAction()
        class RefreshSuccess<T>(val page: Page<T>) : InternalAction()
        class RefreshFail(val error: Throwable) : InternalAction()
        object PageLoadingStart : InternalAction()
        class PageLoadingSuccess<T>(val page: Page<T>) : InternalAction()
        class PageLoadingFail(val error: Throwable) : InternalAction()
    }
}