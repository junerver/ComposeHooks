package xyz.junerver.compose.hooks

//region 类型别名，不应该用这个名称
typealias Tuple0 = None
typealias Tuple1<A> = Single<A>
typealias Tuple2<A, B> = Pair<A, B>
typealias Tuple3<A, B, C> = Triple<A, B, C>
typealias Tuple4<A, B, C, D> = arrow.core.Tuple4<A, B, C, D>
typealias Tuple5<A, B, C, D, E> = arrow.core.Tuple5<A, B, C, D, E>
typealias Tuple6<A, B, C, D, E, F> = arrow.core.Tuple6<A, B, C, D, E, F>
typealias Tuple7<A, B, C, D, E, F, G> = arrow.core.Tuple7<A, B, C, D, E, F, G>
typealias Tuple8<A, B, C, D, E, F, G, H> = arrow.core.Tuple8<A, B, C, D, E, F, G, H>
typealias Tuple9<A, B, C, D, E, F, G, H, I> = arrow.core.Tuple9<A, B, C, D, E, F, G, H, I>
//endregion

//region 元组类与扩展函数
object None

public fun <T> None.toList(): List<T> = emptyList()

data class Single<out A>(
    val first: A,
) {
    public override fun toString(): String = "($first)"
}

public fun <T> Single<T>.toList(): List<T> = listOf(first)

public fun <T> Tuple4<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)

public fun <T> Tuple5<T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth)

public fun <T> Tuple6<T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth)

public fun <T> Tuple7<T, T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth, seventh)

public fun <T> Tuple8<T, T, T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth, seventh, eighth)

public fun <T> Tuple9<T, T, T, T, T, T, T, T, T>.toList(): List<T> =
    listOf(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth)

/** 元组扩展，可以使用 `a to b to c` 这样的连续中缀函数创建更多元素的元组 */
infix fun <A, B> Single<A>.to(b: B): Pair<A, B> = Pair(this.first, b)

infix fun <A, B, C> Pair<A, B>.to(c: C): Triple<A, B, C> = Triple(this.first, this.second, c)

infix fun <A, B, C, D> Triple<A, B, C>.to(d: D): Tuple4<A, B, C, D> = Tuple4(this.first, this.second, this.third, d)

infix fun <A, B, C, D, E> Tuple4<A, B, C, D>.to(e: E): Tuple5<A, B, C, D, E> = Tuple5(this.first, this.second, this.third, this.fourth, e)

infix fun <A, B, C, D, E, F> Tuple5<A, B, C, D, E>.to(f: F): Tuple6<A, B, C, D, E, F> =
    Tuple6(this.first, this.second, this.third, this.fourth, this.fifth, f)

infix fun <A, B, C, D, E, F, G> Tuple6<A, B, C, D, E, F>.to(g: G): Tuple7<A, B, C, D, E, F, G> =
    Tuple7(this.first, this.second, this.third, this.fourth, this.fifth, this.sixth, g)

infix fun <A, B, C, D, E, F, G, H> Tuple7<A, B, C, D, E, F, G>.to(h: H): Tuple8<A, B, C, D, E, F, G, H> = Tuple8(
    this.first,
    this.second,
    this.third,
    this.fourth,
    this.fifth,
    this.sixth,
    this.seventh,
    h
)

infix fun <A, B, C, D, E, F, G, H, I> Tuple8<A, B, C, D, E, F, G, H>.to(i: I): Tuple9<A, B, C, D, E, F, G, H, I> = Tuple9(
    this.first,
    this.second,
    this.third,
    this.fourth,
    this.fifth,
    this.sixth,
    this.seventh,
    this.eighth,
    i
)

public operator fun <A, B, C> Pair<A, B>.plus(c: C): Triple<A, B, C> = Triple(this.first, this.second, c)

public operator fun <A, B, C, D> Triple<A, B, C>.plus(d: D): Tuple4<A, B, C, D> = Tuple4(this.first, this.second, this.third, d)

public operator fun <A, B, C, D, E> Tuple4<A, B, C, D>.plus(e: E): Tuple5<A, B, C, D, E> =
    Tuple5(this.first, this.second, this.third, this.fourth, e)

public operator fun <A, B, C, D, E, F> Tuple5<A, B, C, D, E>.plus(f: F): Tuple6<A, B, C, D, E, F> =
    Tuple6(this.first, this.second, this.third, this.fourth, this.fifth, f)

public operator fun <A, B, C, D, E, F, G> Tuple6<A, B, C, D, E, F>.plus(g: G): Tuple7<A, B, C, D, E, F, G> =
    Tuple7(this.first, this.second, this.third, this.fourth, this.fifth, this.sixth, g)

public operator fun <A, B, C, D, E, F, G, H> Tuple7<A, B, C, D, E, F, G>.plus(h: H): Tuple8<A, B, C, D, E, F, G, H> = Tuple8(
    this.first,
    this.second,
    this.third,
    this.fourth,
    this.fifth,
    this.sixth,
    this.seventh,
    h
)

public operator fun <A, B, C, D, E, F, G, H, I> Tuple8<A, B, C, D, E, F, G, H>.plus(i: I): Tuple9<A, B, C, D, E, F, G, H, I> = Tuple9(
    this.first,
    this.second,
    this.third,
    this.fourth,
    this.fifth,
    this.sixth,
    this.seventh,
    this.eighth,
    i
)
//endregion

//region tuple函数
fun tuple() = None

fun <A> tuple(first: A): Tuple1<A> = Tuple1(first)

fun <A, B> tuple(first: A, second: B): Tuple2<A, B> = Tuple2(first, second)

fun <A, B, C> tuple(first: A, second: B, third: C): Tuple3<A, B, C> = Tuple3(first, second, third)

fun <A, B, C, D> tuple(
    first: A,
    second: B,
    third: C,
    fourth: D,
): Tuple4<A, B, C, D> = Tuple4(first, second, third, fourth)

fun <A, B, C, D, E> tuple(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
): Tuple5<A, B, C, D, E> = Tuple5(first, second, third, fourth, fifth)

fun <A, B, C, D, E, F> tuple(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
): Tuple6<A, B, C, D, E, F> = Tuple6(first, second, third, fourth, fifth, sixth)

fun <A, B, C, D, E, F, G> tuple(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G,
): Tuple7<A, B, C, D, E, F, G> = Tuple7(first, second, third, fourth, fifth, sixth, seventh)

fun <A, B, C, D, E, F, G, H> tuple(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G,
    eighth: H,
): Tuple8<A, B, C, D, E, F, G, H> = Tuple8(first, second, third, fourth, fifth, sixth, seventh, eighth)

fun <A, B, C, D, E, F, G, H, I> tuple(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G,
    eighth: H,
    ninth: I,
): Tuple9<A, B, C, D, E, F, G, H, I> = Tuple9(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth)
//endregion
