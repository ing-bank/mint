package org.mint.util

// trick from Arrow, declare as extension function to prevent issues with in/out types
inline fun <A, B, C> Either<A, B>.flatMap(f: (B) -> Either<A, C>): Either<A, C> =
    when (this) {
        is Either.Right -> f(this.value)
        is Either.Left -> this
    }

inline fun <A, B, C, D> Either<A, B>.redeemWith(fa: (A) -> Either<C, D>, fb: (B) -> Either<C, D>): Either<C, D> =
    when (this) {
        is Either.Left -> fa(this.value)
        is Either.Right -> fb(this.value)
    }

inline fun <A, C, D> Either<A, A>.redeemWith(f: (A) -> Either<C, D>): Either<C, D> = redeemWith(f, f)

fun <A, B> Either<A, B>.orNull(): B? =
    when (this) {
        is Either.Right -> this.value
        is Either.Left -> null
    }

inline fun <T, R, A, B> Either<T, A>.zip(b: Either<*, B>, f: (A, B) -> R): Either<T, R> =
    when (this) {
        is Either.Right -> @Suppress("UNCHECKED_CAST")
        b.map { f(this.value, it) } as Either<T, R>
        is Either.Left -> this
    }

inline fun <T, R, A, B, C> Either<T, A>.zip(b: Either<*, B>, c: Either<*, C>, f: (A, B, C) -> R): Either<T, R> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> @Suppress("UNCHECKED_CAST")
        b.flatMap {
                _b ->
            c.map { _c ->
                f(this.value, _b, _c)
            }
        } as Either<T, R>
    }

inline fun <T, R, A, B, C, D> Either<T, A>.zip(b: Either<*, B>, c: Either<*, C>, d: Either<*, D>, f: (A, B, C, D) -> R): Either<T, R> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> @Suppress("UNCHECKED_CAST")
        b.flatMap {
                _b ->
            c.flatMap { _c ->
                d.map { _d ->
                    f(this.value, _b, _c, _d)
                }
            }
        } as Either<T, R>
    }

sealed class Either<out A, out B> {
    abstract val isLeft: Boolean
    abstract val isRight: Boolean

    inline fun <C> map(f: (B) -> C): Either<A, C> =
        flatMap { Right(f(it)) }

    abstract fun tap(f: (B) -> Unit): Either<A, B>
    abstract fun tapLeft(f: (A) -> Unit): Either<A, B>

    data class Left<out A> constructor(val value: A) : Either<A, Nothing>() {
        override val isLeft = true
        override val isRight = false
        override fun tap(f: (Nothing) -> Unit): Either<A, Nothing> = this
        override fun tapLeft(f: (A) -> Unit): Either<A, Nothing> {
            f(value)
            return this
        }
    }

    data class Right<out B> constructor(val value: B) : Either<Nothing, B>() {
        override val isLeft: Boolean = false
        override val isRight: Boolean = true

        override fun tap(f: (B) -> Unit): Either<Nothing, B> {
            f(value)
            return this
        }
        override fun tapLeft(f: (Nothing) -> Unit): Either<Nothing, B> = this
    }
}
