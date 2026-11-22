package dev.dkarski.diceroller.data

import kotlin.random.Random

enum class DieKind(val defaultSides: Int) {
    D4(4), D6(6), D8(8), D10(10), D12(12), D20(20), Coin(2), Custom(100),
}

data class DieSpec(val kind: DieKind, val sides: Int = kind.defaultSides) {
    init {
        require(if (kind == DieKind.Custom) sides in 2..9999 else sides == kind.defaultSides)
    }
}

data class RolledDie(val spec: DieSpec, val value: Int) {
    init { require(value in 1..spec.sides) }
}

data class TransientRoll(val die: RolledDie, val expiresAtMillis: Long)

/** Session state only: no preferences, Android dependencies or disk writes. */
data class RollSession(
    val combining: Boolean = false,
    val customSides: Int = 100,
    val entries: List<RolledDie> = emptyList(),
    val latest: TransientRoll? = null,
) {
    init {
        require(customSides in 2..9999)
        require(combining || entries.isEmpty())
    }
    val total: Long get() = entries.sumOf { it.value.toLong() }
    val count: Int get() = entries.size
    val canReroll: Boolean get() = combining && entries.isNotEmpty()

    fun roll(kind: DieKind, now: Long, random: DiceRandom = DiceRandom.Default): RollSession {
        val spec = DieSpec(kind, if (kind == DieKind.Custom) customSides else kind.defaultSides)
        val result = RolledDie(spec, random.next(spec.sides))
        return copy(
            entries = if (combining) entries + result else emptyList(),
            latest = TransientRoll(result, now + RESULT_DURATION_MILLIS),
        )
    }

    fun setCombining(enabled: Boolean): RollSession =
        if (enabled == combining) this else copy(combining = enabled, entries = emptyList(), latest = null)

    fun clear(): RollSession = copy(entries = emptyList(), latest = null)

    fun reroll(random: DiceRandom = DiceRandom.Default): RollSession =
        if (!canReroll) this else copy(
            entries = entries.map { old -> RolledDie(old.spec, random.next(old.spec.sides)) },
            latest = null,
        )

    /** Existing Custom entries retain the range they were rolled with. Editing never rolls. */
    fun editCustom(sides: Int): RollSession {
        require(sides in 2..9999)
        return copy(customSides = sides)
    }

    /** Leaving the dice screen hides the transient result at once; a combined set is kept. */
    fun dismissLatest(): RollSession = if (latest == null) this else copy(latest = null)

    /** A superseded timer cannot clear a newer result. Uses a monotonic clock supplied by the UI. */
    fun expire(expected: TransientRoll, now: Long): RollSession =
        if (latest == expected && now >= expected.expiresAtMillis) copy(latest = null) else this

    companion object { const val RESULT_DURATION_MILLIS = 5_000L }
}

fun interface DiceRandom {
    /** Uniform integer in 1..sides. Injected sequences make session tests deterministic. */
    fun next(sides: Int): Int
    companion object {
        val Default = DiceRandom { sides -> Random.Default.nextInt(1, sides + 1) }
    }
}

object CoinPresentation {
    /** Display only: the stored value and contribution to the sum never depend on this preference. */
    fun text(value: Int, numeric: Boolean, heads: String, tails: String): String {
        require(value in 1..2)
        return if (numeric) value.toString() else if (value == 1) heads else tails
    }
}
