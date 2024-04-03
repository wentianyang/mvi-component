package com.chiikawa.mvicore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MviViewModel<I : MviIntent, VS : MviViewState, E : MviSingleEvent> {

    val viewState: StateFlow<VS>

    val singleEvent: Flow<E>

    suspend fun processIntent(intent: I)
}