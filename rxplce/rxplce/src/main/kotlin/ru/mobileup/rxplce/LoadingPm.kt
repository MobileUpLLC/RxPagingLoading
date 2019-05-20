package ru.mobileup.rxplce

import me.dmdev.rxpm.PresentationModel

interface LoadingPm<T> {

    val content: PresentationModel.State<T>

    val isLoading: PresentationModel.State<Boolean>
    val isRefreshing: PresentationModel.State<Boolean>

    val refreshEnabled: PresentationModel.State<Boolean>

    val contentViewVisible: PresentationModel.State<Boolean>
    val emptyViewVisible: PresentationModel.State<Boolean>
    val errorViewVisible: PresentationModel.State<Boolean>

    val refreshAction: PresentationModel.Action<Unit>
    val retryAction: PresentationModel.Action<Unit>
}