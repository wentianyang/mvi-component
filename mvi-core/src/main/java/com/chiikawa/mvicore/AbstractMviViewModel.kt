package com.chiikawa.mvicore

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractMviViewModel<I : MviIntent, VS : MviViewState, E : MviSingleEvent> :
    ViewModel(),
    MviViewModel<I, VS, E> {

    protected open var rawLogTag: String? = null

    protected val logTag by lazy(LazyThreadSafetyMode.PUBLICATION) {
        (rawLogTag ?: this::class.java.simpleName).let { tag: String ->
            if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
                tag
            } else {
                tag.take(MAX_TAG_LENGTH)
            }
        }
    }

    private val eventChannel = Channel<E>(Channel.UNLIMITED)
    private val intentMutableFlow = MutableSharedFlow<I>(extraBufferCapacity = SubscriberBufferSize)

    final override val singleEvent: Flow<E> = eventChannel.receiveAsFlow()

    final override suspend fun processIntent(intent: I) = intentMutableFlow.emit(intent)

    override fun onCleared() {
        super.onCleared()
        eventChannel.close()
        Timber.tag(logTag).d("onCleared")
    }

    protected suspend fun sendEvent(event: E) {

        debugCheckImmediateMainDispatcher()

        eventChannel
            .trySend(event)
            .onFailure {
                Timber
                    .tag(logTag)
                    .e(it, "Error sending event")
            }.getOrThrow()
    }

    protected val intentSharedFlow: SharedFlow<I> get() = intentMutableFlow

    protected fun <T> Flow<T>.debugLog(subject: String): Flow<T> = if (BuildConfig.DEBUG) {
        onEach { Timber.tag(logTag).d(">>> $subject: $it") }
    } else {
        this
    }

    protected fun <T> SharedFlow<T>.debugLog(subject: String): SharedFlow<T> =
        if (BuildConfig.DEBUG) {
            val self = this

            object : SharedFlow<T> by self {
                val subscriberCount = AtomicInteger(0)

                override suspend fun collect(collector: FlowCollector<T>): Nothing {
                    val count = subscriberCount.getAndIncrement()
                    self.collect {
                        Timber.tag(logTag).d(">>> $subject ~ $count: $it")
                    }
                }
            }
        } else {
            this
        }

    protected fun <T> Flow<T>.shareWhileSubscribed(): SharedFlow<T> = shareIn(
        viewModelScope, replay = 1, started = SharingStarted.WhileSubscribed()
    )

    companion object {
        private const val MAX_TAG_LENGTH = 23
        private const val SubscriberBufferSize = 64
    }
}