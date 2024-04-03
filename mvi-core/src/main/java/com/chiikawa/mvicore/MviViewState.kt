package com.chiikawa.mvicore

import android.os.Bundle

interface MviViewState

interface MviViewStateSaver<VS : MviViewState> {
    fun VS.toBundle(): Bundle

    fun restore(bundle: Bundle?): VS
}