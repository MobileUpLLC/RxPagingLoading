package ru.mobileup.rxplce

class DefaultScreenStateMapper<T> : ScreenStateMapper<T> {
    
    override fun mapLceStateToScreenState(loading: Boolean, content: T?, error: Throwable?): ScreenState<T> {
        
        val contentIsVisible = content != null && contentIsEmpty().not()
        val emptyViewIsVisible = content != null && contentIsEmpty()
        val errorViewIsVisible = content == null && error != null

        return ScreenState(
            data = content,
            isLoading = content == null && loading,
            isRefreshing = content != null && loading,
            contentIsVisible = contentIsVisible,
            emptyViewIsVisible = emptyViewIsVisible,
            errorViewIsVisible = errorViewIsVisible,
            refreshEnabled = contentIsVisible || emptyViewIsVisible || errorViewIsVisible
        )
    }

    private fun <T> T?.contentIsEmpty(): Boolean {
        return when (this) {
            is Collection<*> -> this.isEmpty()
            is Array<*> -> this.isEmpty()
            is MaybeEmpty -> this.isEmpty()
            else -> false
        }
    }
}