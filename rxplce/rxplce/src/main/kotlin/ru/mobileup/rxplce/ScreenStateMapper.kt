package ru.mobileup.rxplce

interface ScreenStateMapper<T> {
    fun mapLceStateToScreenState(loading: Boolean, content: T?, error: Throwable?): ScreenState<T>
}