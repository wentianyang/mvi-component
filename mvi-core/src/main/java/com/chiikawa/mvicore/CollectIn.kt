package com.chiikawa.mvicore

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber


inline fun <T> Flow<T>.collectIn(
    owner: LifecycleOwner,
    minActiveSate: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit,
): Job = owner.lifecycleScope.launch {
    owner.lifecycle.repeatOnLifecycle(state = minActiveSate) {
        Timber.d("Start collecting $owner $minActiveSate...")
        collect { action(it) }
    }
}

inline fun <T> Flow<T>.collectInViewLifecycle(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit,
): Job = collectIn(owner, minActiveState, action)