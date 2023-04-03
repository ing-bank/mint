package org.mint.android

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

object FlowTools {
    fun <T> Flow<T>.toListFlow(): Flow<List<T>> = flow {
        val list = toList(mutableListOf())
        emit(list)
    }
}
