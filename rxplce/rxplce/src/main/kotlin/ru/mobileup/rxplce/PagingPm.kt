package ru.mobileup.rxplce

import me.dmdev.rxpm.PresentationModel

interface PagingPm<T> {

    val content: PresentationModel.State<List<T>>

    val isLoading: PresentationModel.State<Boolean>
    val isRefreshing: PresentationModel.State<Boolean>
    val pageIsLoading: PresentationModel.State<Boolean>
    val pageErrorVisible: PresentationModel.State<Boolean>

    val refreshEnabled: PresentationModel.State<Boolean>

    val contentViewVisible: PresentationModel.State<Boolean>
    val emptyViewVisible: PresentationModel.State<Boolean>
    val errorViewVisible: PresentationModel.State<Boolean>

    val scrollToTop: PresentationModel.Command<Unit>

    val refreshAction: PresentationModel.Action<Unit>
    val retryAction: PresentationModel.Action<Unit>
    val nextPageAction: PresentationModel.Action<Unit>
    val retryNextPageAction: PresentationModel.Action<Unit>
}

