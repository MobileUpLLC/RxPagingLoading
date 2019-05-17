package ru.mobileup.rxplce

import me.dmdev.rxpm.PresentationModel

interface PagingScreenPm<T> : LcePm<List<T>> {

    val pageIsLoading: PresentationModel.State<Boolean>
    val pageErrorVisible: PresentationModel.State<Boolean>

    val scrollToTop: PresentationModel.Command<Unit>

    val nextPageAction: PresentationModel.Action<Unit>
    val retryLoadNextPageAction: PresentationModel.Action<Unit>
}

