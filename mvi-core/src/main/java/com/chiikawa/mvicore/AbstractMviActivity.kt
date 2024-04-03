package com.chiikawa.mvicore

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class AbstractMviActivity<
        I : MviIntent,
        VS : MviViewState,
        S : MviSingleEvent,
        VM : MviViewModel<I, VS, S>>
    (@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId), MviView<I, VS, S> {

    protected abstract val vm: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViews()
        bindVM()
    }

    private fun bindVM() {
        vm.viewState.collectIn(this) { render(it) }
        vm.singleEvent.collectIn(this) { handleSingleEvent(it) }

        viewIntents().onEach { vm.processIntent(it) }.launchIn(lifecycleScope)
    }

    protected abstract fun setupViews()
}