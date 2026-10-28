package io.github.l2hyunwoo.rps

import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class Hand(val label: String) {
    ROCK("바위"),
    PAPER("보"),
    SCISSORS("가위"),
    ;

    // Each hand beats the one declared just before it, wrapping around.
    fun beats(other: Hand): Boolean = (ordinal - other.ordinal + 3) % 3 == 1
}

enum class Outcome(val label: String) { WIN("이겼어요"), LOSE("졌어요"), DRAW("비겼어요") }

fun judge(player: Hand, computer: Hand): Outcome = when {
    player == computer -> Outcome.DRAW
    player.beats(computer) -> Outcome.WIN
    else -> Outcome.LOSE
}

fun Random.nextHand(): Hand = Hand.entries[nextInt(Hand.entries.size)]

@Serializable
data class Record(val wins: Int = 0, val losses: Int = 0, val draws: Int = 0, val streak: Int = 0, val bestStreak: Int = 0) {
    // A draw keeps the current win streak; only a loss resets it.
    fun add(outcome: Outcome): Record = when (outcome) {
        Outcome.WIN -> (streak + 1).let { copy(wins = wins + 1, streak = it, bestStreak = maxOf(bestStreak, it)) }
        Outcome.LOSE -> copy(losses = losses + 1, streak = 0)
        Outcome.DRAW -> copy(draws = draws + 1)
    }

    fun summary(): String = "${wins}승 ${losses}패 ${draws}무"
}

data class Round(val player: Hand, val computer: Hand, val outcome: Outcome)

interface KeyValueStorage {
    suspend fun read(key: String): String?
    suspend fun write(key: String, value: String)
}

class MemoryStorage : KeyValueStorage {
    private val values = mutableMapOf<String, String>()
    override suspend fun read(key: String) = values[key]
    override suspend fun write(key: String, value: String) {
        values[key] = value
    }
}

// Records are scoped to the anonymous user key; without a key nothing is persisted.
class RpsGame private constructor(
    private val storage: KeyValueStorage,
    private val userKey: String?,
    private val random: Random,
    initial: Record,
) {
    val persists: Boolean get() = userKey != null
    var saveFailed: Boolean = false
        private set
    var record: Record = initial
        private set

    suspend fun play(player: Hand): Round {
        val computer = random.nextHand()
        val outcome = judge(player, computer)
        record = record.add(outcome)
        save()
        return Round(player, computer, outcome)
    }

    suspend fun reset() {
        record = Record()
        save()
    }

    private suspend fun save() {
        if (userKey == null) return
        saveFailed = try {
            storage.write(storageKey(userKey), Json.encodeToString(Record.serializer(), record))
            false
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            true
        }
    }

    companion object {
        suspend fun load(storage: KeyValueStorage, userKey: String?, random: Random = Random.Default): RpsGame {
            val record = userKey?.let { key ->
                val json = try {
                    storage.read(storageKey(key))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
                json?.let { runCatching { Json.decodeFromString<Record>(it) }.getOrNull() }
            } ?: Record()
            return RpsGame(storage, userKey, random, record)
        }

        private fun storageKey(userKey: String) = "rps-record:$userKey"
    }
}
