package ru.mobileup.rxplce.pm

import ru.mobileup.rxplce.contentIsEmpty

class ScreenStateMapperDefault<T> : ScreenStateMapper<T> {

    override fun mapToScreenState(loading: Boolean, content: T?, error: Throwable?): ScreenState<T> {

        val contentVisible = content != null && contentIsEmpty(content).not()
        val emptyViewVisible = content != null && contentIsEmpty(content)
        val errorViewVisible = content == null && error != null

        return ScreenState(
            content = content,
            isLoading = content == null && loading,
            isRefreshing = content != null && loading,
            contentViewVisible = contentVisible,
            emptyViewVisible = emptyViewVisible,
            errorViewVisible = errorViewVisible,
            refreshEnabled = contentVisible || emptyViewVisible || errorViewVisible
        )
    }
}