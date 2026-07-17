package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.None
import xyz.junerver.compose.hooks.Tuple
import xyz.junerver.compose.hooks.Tuple0
import xyz.junerver.compose.hooks.plus
import xyz.junerver.compose.hooks.to
import xyz.junerver.compose.hooks.toList
import xyz.junerver.compose.hooks.tuple

/*
  Description: Tuple public behavior tests.
  Author: Junerver
  Date: 2026/7/17
  Email: junerver@gmail.com
  Version: v1.0
*/

class TupleTest {
    @Test
    fun tupleZeroIsTheSingleEmptyValue() {
        val empty: Tuple0 = tuple()

        assertSame(None, empty)
        assertTrue(empty.isEmpty())
        assertEquals(emptyList(), empty.toList<Any?>())
        assertTrue(empty.toString().isNotEmpty())
        assertEquals(None.toString(), empty.toString())
    }

    @Test
    fun tupleFactoriesPreserveValuesForEveryArity() {
        assertSame(None, tuple())
        assertEquals(listOf(1), tuple(1).toList())
        assertEquals(listOf(1, 2), tuple(1, 2).toList())
        assertEquals(listOf(1, 2, 3), tuple(1, 2, 3).toList())
        assertEquals(listOf(1, 2, 3, 4), tuple(1, 2, 3, 4).toList())
        assertEquals(listOf(1, 2, 3, 4, 5), tuple(1, 2, 3, 4, 5).toList())
        assertEquals(listOf(1, 2, 3, 4, 5, 6), tuple(1, 2, 3, 4, 5, 6).toList())
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), tuple(1, 2, 3, 4, 5, 6, 7).toList())
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), tuple(1, 2, 3, 4, 5, 6, 7, 8).toList())
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9).toList())
    }

    @Test
    fun tupleStringsExposeArityAndOrderedValues() {
        assertEquals("(1)", tuple(1).toString())
        assertEquals("(1, 2)", tuple(1, 2).toString())
        assertEquals("(1, 2, 3)", tuple(1, 2, 3).toString())
        assertEquals("(1, 2, 3, 4)", tuple(1, 2, 3, 4).toString())
        assertEquals("(1, 2, 3, 4, 5)", tuple(1, 2, 3, 4, 5).toString())
        assertEquals("(1, 2, 3, 4, 5, 6)", tuple(1, 2, 3, 4, 5, 6).toString())
        assertEquals("(1, 2, 3, 4, 5, 6, 7)", tuple(1, 2, 3, 4, 5, 6, 7).toString())
        assertEquals("(1, 2, 3, 4, 5, 6, 7, 8)", tuple(1, 2, 3, 4, 5, 6, 7, 8).toString())
        assertEquals("(1, 2, 3, 4, 5, 6, 7, 8, 9)", tuple(1, 2, 3, 4, 5, 6, 7, 8, 9).toString())
    }

    @Test
    fun tupleIsEmptyOnlyWhenEveryFieldIsNone() {
        val emptyTuples: List<Tuple> = listOf(
            tuple(None),
            tuple(None, None),
            tuple(None, None, None),
            tuple(None, None, None, None),
            tuple(None, None, None, None, None),
            tuple(None, None, None, None, None, None),
            tuple(None, None, None, None, None, None, None),
            tuple(None, None, None, None, None, None, None, None),
            tuple(None, None, None, None, None, None, None, None, None),
        )
        val partiallyFilledTuples: List<Tuple> = listOf(
            tuple("value"),
            tuple(None, "value"),
            tuple(None, None, "value"),
            tuple(None, None, None, "value"),
            tuple(None, None, None, None, "value"),
            tuple(None, None, None, None, None, "value"),
            tuple(None, None, None, None, None, None, "value"),
            tuple(None, None, None, None, None, None, None, "value"),
            tuple(None, None, None, None, None, None, None, None, "value"),
        )

        emptyTuples.forEach { assertTrue(it.isEmpty()) }
        partiallyFilledTuples.forEach { assertFalse(it.isEmpty()) }
    }

    @Test
    fun mixedFactoryValuesRemainInTheirNamedPositions() {
        val mixed = tuple("first", 2, true, 4.5, 'f', 6L, listOf(7), null, None)

        assertEquals(
            listOf<Any?>("first", 2, true, 4.5, 'f', 6L, listOf(7), null, None),
            listOf(
                mixed.first,
                mixed.second,
                mixed.third,
                mixed.fourth,
                mixed.fifth,
                mixed.sixth,
                mixed.seventh,
                mixed.eighth,
                mixed.ninth,
            ),
        )
        assertFalse(mixed.isEmpty())
    }

    @Test
    fun continuousInfixToBuildsEveryTupleArityInOrder() {
        val mixed = None to "first" to 2 to true to 4.5 to 'f' to 6L to listOf(7) to null to None

        assertEquals(
            listOf<Any?>("first", 2, true, 4.5, 'f', 6L, listOf(7), null, None),
            listOf(
                mixed.first,
                mixed.second,
                mixed.third,
                mixed.fourth,
                mixed.fifth,
                mixed.sixth,
                mixed.seventh,
                mixed.eighth,
                mixed.ninth,
            ),
        )
    }

    @Test
    fun plusAppendsWithoutChangingTheExistingTuple() {
        val original = tuple("first", 2)
        val mixed = original + true + 4.5 + 'f' + 6L + listOf(7) + null + None

        assertEquals(tuple("first", 2), original)
        assertEquals(
            listOf<Any?>("first", 2, true, 4.5, 'f', 6L, listOf(7), null, None),
            listOf(
                mixed.first,
                mixed.second,
                mixed.third,
                mixed.fourth,
                mixed.fifth,
                mixed.sixth,
                mixed.seventh,
                mixed.eighth,
                mixed.ninth,
            ),
        )
    }

    @Test
    fun tupleDataClassesCompareByOrderedContents() {
        val equalPairs: List<Pair<Tuple, Tuple>> = listOf(
            Pair(tuple(1), tuple(1)),
            Pair(tuple(1, 2), tuple(1, 2)),
            Pair(tuple(1, 2, 3), tuple(1, 2, 3)),
            Pair(tuple(1, 2, 3, 4), tuple(1, 2, 3, 4)),
            Pair(tuple(1, 2, 3, 4, 5), tuple(1, 2, 3, 4, 5)),
            Pair(tuple(1, 2, 3, 4, 5, 6), tuple(1, 2, 3, 4, 5, 6)),
            Pair(tuple(1, 2, 3, 4, 5, 6, 7), tuple(1, 2, 3, 4, 5, 6, 7)),
            Pair(tuple(1, 2, 3, 4, 5, 6, 7, 8), tuple(1, 2, 3, 4, 5, 6, 7, 8)),
            Pair(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9)),
        )
        val unequalPairs: List<Pair<Tuple, Tuple>> = listOf(
            Pair(tuple(1), tuple(2)),
            Pair(tuple(1, 2), tuple(1, 3)),
            Pair(tuple(1, 2, 3), tuple(1, 2, 4)),
            Pair(tuple(1, 2, 3, 4), tuple(1, 2, 3, 5)),
            Pair(tuple(1, 2, 3, 4, 5), tuple(1, 2, 3, 4, 6)),
            Pair(tuple(1, 2, 3, 4, 5, 6), tuple(1, 2, 3, 4, 5, 7)),
            Pair(tuple(1, 2, 3, 4, 5, 6, 7), tuple(1, 2, 3, 4, 5, 6, 8)),
            Pair(tuple(1, 2, 3, 4, 5, 6, 7, 8), tuple(1, 2, 3, 4, 5, 6, 7, 9)),
            Pair(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9), tuple(1, 2, 3, 4, 5, 6, 7, 8, 10)),
        )

        equalPairs.forEach { (first, second) -> assertEquals(first, second) }
        unequalPairs.forEach { (first, second) -> assertNotEquals(first, second) }
    }
}
