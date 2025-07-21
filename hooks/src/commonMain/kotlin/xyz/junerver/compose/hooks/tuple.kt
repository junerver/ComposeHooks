package xyz.junerver.compose.hooks

//region 类型别名
typealias Tuple0 = None
//endregion

//region 元组类与扩展函数
sealed interface Tuple {
    fun isEmpty(): Boolean
}

object None : Tuple {
    override fun isEmpty(): Boolean = true
}

public fun <T> None.toList(): List<T> = emptyList()

data class Tuple1<out A>(
    val first: A,
) : Tuple {
    override fun isEmpty(): Boolean = this.first is None

    public override fun toString(): String = "($first)"
}

public fun <T> Tuple1<T>.toList(): List<T> = listOf(first)

data class Tuple2<out A, out B>(
    val first: A,
    val second: B,
) : Tuple {
    override fun isEmpty(): Boolean = this.first is None && this.second is None

    public override fun toString(): String = "($first, $second)"
}

public fun <T> Tuple2<T, T>.toList(): List<T> = listOf(first, second)

data class Tuple3<out A, out B, out C>(
    val first: A,
    val second: B,
    val third: C,
) : Tuple {
    override fun isEmpty(): Boolean = this.first is None && this.second is None && this.third is None

    public override fun toString(): String = "($first, $second, $third)"
}

public fun <T> Tuple3<T, T, T>.toList(): List<T> = listOf(first, second, third)

data class Tuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
) : Tuple {
    override fun isEmpty(): Boolean = this.first is None && this.second is None && this.third is None && this.fourth is None

    public override fun toString(): String = "($first, $second, $third, $fourth)"
}

public fun <T> Tuple4<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)

data class Tuple5<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
) : Tuple {
    override fun isEmpty(): Boolean =
        this.first is None && this.second is None && this.third is None && this.fourth is None && this.fifth is None

    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

public fun <T> Tuple5<T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth)

data class Tuple6<out A, out B, out C, out D, out E, out F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
) : Tuple {
    override fun isEmpty(): Boolean =
        this.first is None && this.second is None && this.third is None && this.fourth is None && this.fifth is None && this.sixth is None

    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}

public fun <T> Tuple6<T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth)

data class Tuple7<out A, out B, out C, out D, out E, out F, out G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
) : Tuple {
    override fun isEmpty(): Boolean =
        this.first is None && this.second is None && this.third is None && this.fourth is None && this.fifth is None && this.sixth is None && this.seventh is None

    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh)"
}

public fun <T> Tuple7<T, T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth, seventh)

data class Tuple8<out A, out B, out C, out D, out E, out F, out G, out H>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
) : Tuple {
    override fun isEmpty(): Boolean =
        this.first is None && this.second is None && this.third is None && this.fourth is None && this.fifth is None && this.sixth is None && this.seventh is None && this.eighth is None

    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth)"
}

public fun <T> Tuple8<T, T, T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth, seventh, eighth)

data class Tuple9<out A, out B, out C, out D, out E, out F, out G, out H, out I>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
    val ninth: I,
) : Tuple {
    override fun isEmpty(): Boolean =
        this.first is None && this.second is None && this.third is None && this.fourth is None && this.fifth is None && this.sixth is None && this.seventh is None && this.eighth is None && this.ninth is None

    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth, $ninth)"
}

public fun <T> Tuple9<T, T, T, T, T, T, T, T, T>.toList(): List<T> =
    listOf(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth)

/** 元组扩展，可以使用 `a to b to c` 这样的连续中缀函数创建更多元素的元组 */
infix fun <A> None.to(a: A): Tuple1<A> = Tuple1(a)

infix fun <A, B> Tuple1<A>.to(b: B): Tuple2<A, B> = Tuple2(this.first, b)

infix fun <A, B, C> Tuple2<A, B>.to(c: C): Tuple3<A, B, C> = Tuple3(this.first, this.second, c)

infix fun <A, B, C, D> Tuple3<A, B, C>.to(d: D): Tuple4<A, B, C, D> = Tuple4(this.first, this.second, this.third, d)

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
    h,
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
    i,
)

public operator fun <A, B, C> Tuple2<A, B>.plus(c: C): Tuple3<A, B, C> = Tuple3(this.first, this.second, c)

public operator fun <A, B, C, D> Tuple3<A, B, C>.plus(d: D): Tuple4<A, B, C, D> = Tuple4(this.first, this.second, this.third, d)

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
    h,
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
    i,
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
