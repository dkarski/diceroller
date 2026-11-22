package dev.dkarski.diceroller.ui

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import dev.dkarski.diceroller.data.DieKind
import dev.dkarski.diceroller.data.DieSpec
import dev.dkarski.diceroller.data.RollSession
import dev.dkarski.diceroller.data.RolledDie
import dev.dkarski.diceroller.data.TransientRoll

/** Android saved view state, never written to app preferences or a session file. */
val RollSessionSaver = Saver<RollSession, Bundle>(
    save = { state ->
        Bundle().apply {
            putBoolean("combining", state.combining)
            putInt("custom", state.customSides)
            putIntArray("entries", state.entries.flatMap { listOf(it.spec.kind.ordinal, it.spec.sides, it.value) }.toIntArray())
            state.latest?.let {
                putIntArray("last", intArrayOf(it.die.spec.kind.ordinal, it.die.spec.sides, it.die.value))
                putLong("deadline", it.expiresAtMillis)
            }
        }
    },
    restore = { saved ->
        runCatching {
            fun die(values: List<Int>) = RolledDie(DieSpec(DieKind.entries[values[0]], values[1]), values[2])
            RollSession(
                combining = saved.getBoolean("combining"),
                customSides = saved.getInt("custom", 100),
                entries = saved.getIntArray("entries")!!.toList().chunked(3).map(::die),
                latest = saved.getIntArray("last")?.let { TransientRoll(die(it.toList()), saved.getLong("deadline")) },
            )
        }.getOrDefault(RollSession())
    },
)
