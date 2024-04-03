package com.chiikawa.mvicore

import kotlinx.coroutines.flow.Flow

interface MviView<I : MviIntent, VS : MviViewState, E : MviSingleEvent> {

    fun render(viewState: VS)

    fun handleSingleEvent(event: E)

    fun viewIntents(): Flow<I>
}