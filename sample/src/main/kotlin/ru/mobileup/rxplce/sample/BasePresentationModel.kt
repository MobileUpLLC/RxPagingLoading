package ru.mobileup.rxplce.sample

import io.reactivex.Observable
import io.reactivex.functions.Consumer
import me.dmdev.rxpm.*

abstract class BasePresentationModel: PresentationModel() {

    fun <T> PresentationModel.stateOf(observable: Observable<T>): State<T> {
        val state = state<T>()
        observable.subscribe(state.consumer).untilDestroy()
        return state
    }

    fun <T, R> PresentationModel.actionTo(
        actionConsumer: Consumer<R>,
        actionChain: Observable<T>.() -> Observable<R>
    ): Action<T> {
        val action = action<T>()
        actionChain(action.observable)
            .retry()
            .subscribe(actionConsumer)
            .untilDestroy()
        return action
    }
}