package ru.mobileup.rxplce

import me.dmdev.rxpm.PresentationModel

interface PagingScreenPm<T> {

    val data: PresentationModel.State<List<T>>

    val isLoading: PresentationModel.State<Boolean>
    val isRefreshing: PresentationModel.State<Boolean>
    val pageIsLoading: PresentationModel.State<Boolean>

    val refreshEnabled: PresentationModel.State<Boolean>

    val contentVisible: PresentationModel.State<Boolean>
    val emptyViewVisible: PresentationModel.State<Boolean>
    val errorViewVisible: PresentationModel.State<Boolean>
    val pageErrorVisible: PresentationModel.State<Boolean>

    val refreshAction: PresentationModel.Action<Unit>
    val nextPageAction: PresentationModel.Action<Unit>
    val retryLoadAction: PresentationModel.Action<Unit>
    val retryLoadNextPageAction: PresentationModel.Action<Unit>
}

