package io.github.l2hyunwoo.rps

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
data class Record(val wins: Int = 0, val losses: Int = 0, val draws: Int = 0) {
    fun add(outcome: Outcome): Record = when (outcome) {
        Outcome.WIN -> copy(wins = wins + 1)
        Outcome.LOSE -> copy(losses = losses + 1)
        Outcome.DRAW -> copy(draws = draws + 1)
    }

    fun summary(): String = "${wins}승 ${losses}패 ${draws}무"
}

data class Round(val player: Hand, val computer: Hand, val outcome: Outcome)

interface KeyValueStorage {
    fun read(key: String): String?
    fun write(key: String, value: String)
}

class MemoryStorage : KeyValueStorage {
    private val values = mutableMapOf<String, String>()
    override fun read(key: String) = values[key]
    override fun write(key: String, value: String) {
        values[key] = value
    }
}

// Records are scoped to the anonymous user key; without a key nothing is persisted.
class RpsGame(
    private val storage: KeyValueStorage,
    private val userKey: String?,
    private val random: Random = Random.Default,
) {
    val persists: Boolean get() = userKey != null
    var saveFailed: Boolean = false
        private set
    var record: Record = load()
        private set

    fun play(player: Hand): Round {
        val computer = random.nextHand()
        val outcome = judge(player, computer)
        record = record.add(outcome)
        save()
        return Round(player, computer, outcome)
    }

    fun reset() {
        record = Record()
        save()
    }

    private val storageKey get() = "rps-record:$userKey"

    private fun load(): Record {
        if (userKey == null) return Record()
        val json = runCatching { storage.read(storageKey) }.getOrNull() ?: return Record()
        return runCatching { Json.decodeFromString<Record>(json) }.getOrDefault(Record())
    }

    private fun save() {
        if (userKey == null) return
        saveFailed = runCatching { storage.write(storageKey, Json.encodeToString(Record.serializer(), record)) }.isFailure
    }
}
