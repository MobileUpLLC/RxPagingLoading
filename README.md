# Reactive Paging and Loading

This library implements reactive paging and loading. 
It helps to handle the states of loading a simple data (LCE, loading/content/error) or the complex states of lists with pagination (PLCE, paging/loading/content/error). The solution is based on the usage of Unidirectional Data Flow pattern.

The library depends on RxJava, so you will find familiar interfaces in it's API.

## Loading a simple data

`Loading` interface looks as follows:

```Kotlin
interface Loading<T> {

    enum class Action { REFRESH, FORCE_REFRESH }

    val state: Observable<State<T>>

    val actions: Consumer<Action>

    data class State<T>(
        val content: T? = null,
        val loading: Boolean = false,
        val error: Throwable? = null
    )
}
```

It includes:
- `State` class — represents LCE state.
- `Action` enum - the possible actions.
- `state: Observable` - observes changes of the LCE state.
- `actions: Consumer` - receives performed Action.

There are two implementations of this interface:

### LoadingOrdinary

This class is for simple case when at first load or after refresh content comes from `Single` data source, passed into the constructor:

```Kotlin
LoadingOrdinary(
    source = Single.just("Content string")
)
```

### LoadingAssembled

This implementation is for case when there is a separate `Сompletable` to refresh the content and an `Observable` stream for receiving this content updates:

```Kotlin
LoadingAssembled(
    refresh = repository.refreshDataCompletable(),
    updates = repository.dataChangesObservable()
)
```

## Paging

The `Paging` interface looks a bit more complicated. In addition to the LCE, it has the paging states:

```Kotlin
interface Paging<T> {

    enum class Action { REFRESH, FORCE_REFRESH, LOAD_NEXT_PAGE }

    val state: Observable<State<T>>

    val actions: Consumer<Action>

    data class State<T>(
        val content: List<T>? = null,
        val loading: Boolean = false,
        val error: Throwable? = null,
        val pageLoading: Boolean = false,
        val pageError: Throwable? = null,
        val lastPage: Page<T>? = null
    ) {
        val isEndReached: Boolean get() = lastPage?.isEndReached ?: false
    }

    interface Page<T> {
        val items: List<T>
        val lastItem: T? get() = items.lastOrNull()
        val isEndReached: Boolean
    }
}
```

Note, the `State` also stores the last loaded page. It is used to download the following page, as well as to determine the end of the list.

`Page` is an interface made for flexibility. Your data source can map a page data to it's own class. For example, you can store an identifier of the last entity, or a link to the next page, or any data depending on your back-end requirements. The last page will be passed to a lambda `pageSource` from the constructor of the `PagingImpl`:

```Kotlin
class PageInfo(
    override val items: List<Item>,
    override val isEndReached: Boolean
    lastItemId: Int
) : Paging.Page<Item>

PagingImpl(
    pageSource = { offset, lastPage ->
        repository
            .loadPage(lastItemId = lastPage?.lastItemId)
            .map {
                PageInfo(
                    items = it.list,
                    isEndReached = (offset + it.list.size) == it.totalCount
                    it.lastItemId
                )
            }
    }
)
```

## Display the state
You can just use the resulting PLCE or LCE state to render your screen UI. Or you can use extensions from `LoadingExtensions.kt` and `PagingExtensions.kt` to observe individual state parts changes. It's helpful when you don't need all of the states or use with MVVM-like pattern. 

In the [sample](https://github.com/MobileUpLLC/RxPagingLoading/tree/develop/sample) we use the [RxPM](https://github.com/dmdevgo/RxPM/tree/develop/rxpm) library and extensions to split resulting state to the Presentation Model states.

## License
```
MIT License

Copyright (c) 2019 MobileUp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
