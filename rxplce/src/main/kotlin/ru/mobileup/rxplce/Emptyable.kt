package ru.mobileup.rxplce

interface Emptyable {
    fun isEmpty(): Boolean
}

fun contentIsEmpty(content: Any?): Boolean {
    return when (content) {
        is Collection<*> -> content.isEmpty()
        is Array<*> -> content.isEmpty()
        is Emptyable -> content.isEmpty()
        else -> false
    }
}