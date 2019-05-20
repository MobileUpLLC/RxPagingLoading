package ru.mobileup.rxplce

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

    private fun contentIsEmpty(content: Any?): Boolean {
        return when (content) {
            is Collection<*> -> content.isEmpty()
            is Array<*> -> content.isEmpty()
            is Emptyable -> content.isEmpty()
            else -> false
        }
    }
}