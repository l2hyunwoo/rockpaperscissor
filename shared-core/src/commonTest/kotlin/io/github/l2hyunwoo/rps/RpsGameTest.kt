package io.github.l2hyunwoo.rps

import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RpsGameTest {
    @Test fun judgesAllNineCombinations() {
        val expected = mapOf(
            (Hand.ROCK to Hand.ROCK) to Outcome.DRAW,
            (Hand.ROCK to Hand.PAPER) to Outcome.LOSE,
            (Hand.ROCK to Hand.SCISSORS) to Outcome.WIN,
            (Hand.PAPER to Hand.ROCK) to Outcome.WIN,
            (Hand.PAPER to Hand.PAPER) to Outcome.DRAW,
            (Hand.PAPER to Hand.SCISSORS) to Outcome.LOSE,
            (Hand.SCISSORS to Hand.ROCK) to Outcome.LOSE,
            (Hand.SCISSORS to Hand.PAPER) to Outcome.WIN,
            (Hand.SCISSORS to Hand.SCISSORS) to Outcome.DRAW,
        )
        assertEquals(9, expected.size)
        expected.forEach { (hands, outcome) -> assertEquals(outcome, judge(hands.first, hands.second), "$hands") }
    }

    @Test fun computerHandIsUniform() {
        val random = Random(42)
        val draws = 30_000
        val counts = List(draws) { random.nextHand() }.groupingBy { it }.eachCount()
        Hand.entries.forEach { hand -> assertTrue(abs(counts.getValue(hand).toDouble() / draws - 1.0 / 3) <= 0.02, "$hand: ${counts[hand]}") }
    }

    @Test fun injectedRandomDecidesComputerHand() {
        val round = RpsGame(MemoryStorage(), "user", Random(7)).play(Hand.ROCK)
        assertEquals(Random(7).nextHand(), round.computer)
        assertEquals(judge(Hand.ROCK, round.computer), round.outcome)
    }

    @Test fun recordsAccumulateResetAndStayPerUser() {
        val storage = MemoryStorage()
        val game = RpsGame(storage, "a", Random(1))
        repeat(5) { game.play(Hand.PAPER) }
        assertEquals(5, game.record.let { it.wins + it.losses + it.draws })
        assertEquals(game.record, RpsGame(storage, "a").record)
        assertEquals(Record(), RpsGame(storage, "b").record)

        game.reset()
        assertEquals(Record(), RpsGame(storage, "a").record)
    }

    @Test fun withoutUserKeyNothingIsStored() {
        val storage = MemoryStorage()
        val game = RpsGame(storage, null, Random(1))
        game.play(Hand.ROCK)
        assertFalse(game.persists)
        assertEquals(1, game.record.let { it.wins + it.losses + it.draws })
        assertNull(storage.read("rps-record:null"))
        assertEquals(Record(), RpsGame(storage, null).record)
    }

    @Test fun corruptOrFailingStorageFallsBackToEmptyRecord() {
        val storage = MemoryStorage().apply { write("rps-record:a", "{broken") }
        assertEquals(Record(), RpsGame(storage, "a").record)

        val failing = object : KeyValueStorage {
            override fun read(key: String): String = error("blocked")
            override fun write(key: String, value: String) = error("blocked")
        }
        val game = RpsGame(failing, "a", Random(1))
        game.play(Hand.ROCK)
        assertTrue(game.saveFailed)
    }
}
