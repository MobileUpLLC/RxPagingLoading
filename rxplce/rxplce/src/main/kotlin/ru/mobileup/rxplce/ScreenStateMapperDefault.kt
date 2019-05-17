package ru.mobileup.rxplce

class ScreenStateMapperDefault<T> : ScreenStateMapper<T> {

    override fun mapToScreenState(loading: Boolean, content: T?, error: Throwable?): ScreenState<T> {

        val contentVisible = content != null && contentIsEmpty().not()
        val emptyViewVisible = content != null && contentIsEmpty()
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

    private fun <T> T?.contentIsEmpty(): Boolean {
        return when (this) {
            is Collection<*> -> this.isEmpty()
            is Array<*> -> this.isEmpty()
            is Emptyable -> this.isEmpty()
            else -> false
        }
    }
}