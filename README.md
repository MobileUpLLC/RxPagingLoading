# Reactive Paging Loading Content Error (PLCE)

This library implements reactive paging and data loading.
It solves a LCE-state problem and implements a pagination. The basis of this solution is the usage of Unidirectional Data Flow pattern. This library depends on RxJava and uses its interfaces like `Observable` and `Consumer` to represent API.

## Simple data loading

There is an interface `Loading`, that looks as follows:

```Kotlin
interface Loading<T> {

    enum class Action { REFRESH }

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
- `State` — represents LCE-state.
- `Observable` - observes changes of the LCE-state.
- `Consumer` - receives appropriate Action.

There are two implementations:

- LoadingOrdinary

This class takes a data source as a Single into the constructor:

```Kotlin
LoadingOrdinary(
	source = Single.just("Content string")
)
```

- LoadingAssembled

This implementation is used when there is a `Сompletable` for updating data and a separate stream for receiving data:

```Kotlin
LoadingAssembled(
	refresh = repository.refreshDataCompletable(),
    updates = repository.dataChangesObservable()
)
```

## Paging

The `Paging` interface looks a bit more complicated. In addition to the LCE-state, it includes a paging state:

```Kotlin
interface Paging<T> {

    enum class Action { REFRESH, LOAD_NEXT_PAGE }

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

Note, the `State` also stores the last loaded page. It is needed to download the following page, as well as to determine the end of the list.

`Page` is an interface which is made for flexibility. Your data source can map a page data to the own class. For example, you can wish to store an identifier of the last entity, or a link to the next page, or any data depending on your back-end requirements. The last page will be passed to a lambda `pageSource`, which should be passed to the constructor of the `PagingImpl`:

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
We recommend converting the resulting PLCE or LCE state to the corresponding state for the screen. In the PLCE-pm module, we use the RxPM library integration to map the current state to the screen state. More details you can see in the sample.

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