package org.mint.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <A, B> Map<A, B>.pForEach(f: suspend (key: A, value: B) -> Unit): Unit = coroutineScope {
    map { async { f(it.key, it.value) } }.awaitAll()
}

suspend fun <A> Iterable<A>.pForEach(f: suspend (it: A) -> Unit): Unit = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

suspend fun <A, B> Iterable<A>.pMap(f: suspend (it: A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}
