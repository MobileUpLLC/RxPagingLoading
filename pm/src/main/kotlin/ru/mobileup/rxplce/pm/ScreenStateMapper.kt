package ru.mobileup.rxplce.pm

interface ScreenStateMapper<T> {
    fun mapToScreenState(loading: Boolean, content: T?, error: Throwable?): ScreenState<T>
}