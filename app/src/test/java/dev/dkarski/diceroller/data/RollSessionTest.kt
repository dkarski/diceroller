package dev.dkarski.diceroller.data

import org.junit.Assert.*
import org.junit.Test

class RollSessionTest {
    private fun sequence(vararg values: Int): DiceRandom {
        val iterator = values.iterator()
        return DiceRandom { sides -> iterator.nextInt().also { require(it in 1..sides) } }
    }

    @Test fun `single rolls replace the transient result without building a set`() {
        val random = sequence(4, 7)
        val first = RollSession().roll(DieKind.D6, 100, random)
        val next = first.roll(DieKind.D8, 400, random)
        assertEquals(0, next.count)
        assertEquals(0L, next.total)
        assertEquals(DieKind.D8, next.latest!!.die.spec.kind)
        assertEquals(7, next.latest.die.value)
        assertEquals(5_400L, next.latest.expiresAtMillis)
    }

    @Test fun `combine counts every entry and rerolls the same recipe without accumulating totals`() {
        val random = sequence(2, 5, 6, 1, 8, 3)
        var state = RollSession().setCombining(true)
        listOf(DieKind.D6, DieKind.D8, DieKind.D6).forEach { state = state.roll(it, 0, random) }
        assertEquals(13L, state.total)
        assertEquals(3, state.count)
        val specs = state.entries.map { it.spec }
        val rerolled = state.reroll(random)
        assertEquals(12L, rerolled.total)
        assertEquals(specs, rerolled.entries.map { it.spec })
        assertEquals(3, rerolled.count)
        assertNull(rerolled.latest)
    }

    @Test fun `coin and distinct custom ranges remain numeric rerollable entries`() {
        val requests = mutableListOf<Int>()
        val random = DiceRandom { sides -> requests += sides; sides }
        val state = RollSession().setCombining(true)
            .roll(DieKind.Coin, 0, random)
            .roll(DieKind.Custom, 0, random)
            .editCustom(3).roll(DieKind.Custom, 0, random)
        assertEquals(105L, state.total)
        val edited = state.editCustom(9999)
        assertEquals(state.entries, edited.entries)
        assertEquals(state.latest, edited.latest)
        val rerolled = edited.reroll(random)
        assertEquals(listOf(2, 100, 3, 2, 100, 3), requests)
        assertEquals(105L, rerolled.total)
        assertEquals(3, rerolled.count)
    }

    @Test fun `clear and disabling combine discard set and transient result`() {
        val state = RollSession().setCombining(true).roll(DieKind.D20, 0, sequence(17))
        assertTrue(state.canReroll)
        val cleared = state.clear()
        assertTrue(cleared.combining)
        assertFalse(cleared.canReroll)
        assertNull(cleared.latest)
        val disabled = state.setCombining(false)
        assertEquals(0, disabled.count)
        assertNull(disabled.latest)
        assertEquals(0L, disabled.total)
        assertEquals(0, disabled.setCombining(true).count)
        assertSame(disabled, disabled.reroll())
    }

    @Test fun `results expire at five seconds and a stale timer cannot erase a newer roll`() {
        val first = RollSession().roll(DieKind.D6, 0, sequence(2))
        val firstTimer = first.latest!!
        assertNotNull(first.expire(firstTimer, 4_999).latest)
        assertNull(first.expire(firstTimer, 5_000).latest)
        val second = first.roll(DieKind.D6, 4_000, sequence(2))
        assertNotNull(second.expire(firstTimer, 5_000).latest)
        val secondTimer = second.latest!!
        assertNotNull(second.expire(secondTimer, 8_999).latest)
        assertNull(second.expire(secondTimer, 9_000).latest)
        // A return from Settings after the deadline must not restart the display interval.
        assertNull(second.expire(secondTimer, 50_000).latest)
    }

    @Test fun `coin presentation changes neither its arithmetic value nor session`() {
        assertEquals("Heads", CoinPresentation.text(1, false, "Heads", "Tails"))
        assertEquals("Tails", CoinPresentation.text(2, false, "Heads", "Tails"))
        assertEquals("1", CoinPresentation.text(1, true, "Heads", "Tails"))
        assertEquals("2", CoinPresentation.text(2, true, "Heads", "Tails"))
    }

    @Test fun `all built-in dice stay within bounds`() {
        DieKind.entries.forEach { kind ->
            repeat(100) {
                val result = RollSession().roll(kind, 0).latest!!.die
                assertTrue(result.value in 1..result.spec.sides)
            }
        }
    }

    @Test fun `custom bounds reject invalid values without changing a session`() {
        val state = RollSession()
        listOf(1, 0, -1, 10000).forEach { value ->
            try { state.editCustom(value); fail("Accepted $value") } catch (_: IllegalArgumentException) { }
        }
        assertEquals(2, state.editCustom(2).customSides)
        assertEquals(9999, state.editCustom(9999).customSides)
        assertEquals(100, state.customSides)
    }

    @Test fun `leaving the dice screen hides the transient result but keeps the combined set`() {
        val random = sequence(3, 5)
        val single = RollSession().roll(DieKind.D6, 0, random).dismissLatest()
        assertNull(single.latest)
        val set = RollSession().setCombining(true).roll(DieKind.D8, 0, random).dismissLatest()
        assertNull(set.latest)
        assertEquals(1, set.count)
        assertEquals(5L, set.total)
        assertTrue(set.combining)
    }
}
