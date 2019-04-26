package ru.mobileup.rxplce

import me.dmdev.rxpm.PresentationModel

interface LcePm<T> {

    val dataState: PresentationModel.State<DataState<T>>
    val refreshes: PresentationModel.Action<Unit>

    interface DataMaybeEmpty {
        fun isEmpty(): Boolean
    }

    data class DataState<T>(
        val data: T? = null,
        val refreshingError: Throwable? = null,
        val refreshing: Boolean = false
    ) {

        fun dataIsEmpty(): Boolean {
            return when (data) {
                is Collection<*> -> data.isEmpty()
                is Array<*> -> data.isEmpty()
                is DataMaybeEmpty -> data.isEmpty()
                else -> false
            }
        }
    }
}