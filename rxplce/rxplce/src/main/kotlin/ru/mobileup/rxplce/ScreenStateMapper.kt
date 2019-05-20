package ru.mobileup.rxplce

interface ScreenStateMapper<T> {
    fun mapToScreenState(loading: Boolean, content: T?, error: Throwable?): ScreenState<T>
}