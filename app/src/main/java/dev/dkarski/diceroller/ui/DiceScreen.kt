package dev.dkarski.diceroller.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.nav_bar.NavigationBarDefaultsMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import dev.dkarski.diceroller.R
import dev.dkarski.diceroller.data.*
import dev.dkarski.diceroller.ui.components.AppOutlinedButton
import dev.dkarski.diceroller.ui.theme.AppTheme

private val rows = listOf(
    listOf(DieKind.D4, DieKind.D6, DieKind.D8),
    listOf(DieKind.D10, DieKind.D12, DieKind.D20),
)

@DrawableRes
private fun DieKind.icon(): Int = when (this) {
    DieKind.D4 -> R.drawable.die_d4
    DieKind.D6 -> R.drawable.die_d6
    DieKind.D8 -> R.drawable.die_d8
    DieKind.D10 -> R.drawable.die_d10
    DieKind.D12 -> R.drawable.die_d12
    DieKind.D20 -> R.drawable.die_d20
    DieKind.Coin -> R.drawable.die_coin
    DieKind.Custom -> error("Custom is an editable control")
}

/**
 * Native-style header (title + Settings), the dice grid and a fixed bottom bar. A live result replaces
 * the tapped icon in a slot of the same size, so no tile moves. No animation or moving tiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen(
    session: RollSession,
    coinAsNumbers: Boolean,
    onRoll: (DieKind) -> Unit,
    onCombiningChange: (Boolean) -> Unit,
    onEditCustom: () -> Unit,
    onReroll: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBarMMD(
            // MMD 1.0.2 passes its 3 dp divider to width(), so it draws 1 px; native Mudita apps show 3 dp.
            showDivider = false,
            title = { TextMMD(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
            actions = {
                Box(
                    Modifier.size(48.dp).noRippleClick(stringResource(R.string.settings_title), onSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_settings), contentDescription = null, modifier = Modifier.size(24.dp))
                }
            },
        )
        HorizontalDividerMMD()
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {
            val iconSize = ((maxHeight / 3.3f) - 52.dp).coerceIn(36.dp, 60.dp)
            Column(Modifier.fillMaxSize()) {
                rows.forEach { kinds ->
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        kinds.forEach { kind ->
                            Column(
                                Modifier.weight(1f).fillMaxHeight().noRippleClick(
                                    stringResource(R.string.roll_die, kind.name),
                                ) { onRoll(kind) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                IconOrResult(kind, session, coinAsNumbers, iconSize) {
                                    Icon(painterResource(kind.icon()), null, Modifier.size(iconSize))
                                }
                                TextMMD(kind.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().weight(1.3f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(
                        Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        IconOrResult(DieKind.Custom, session, coinAsNumbers, iconSize, onEditCustom) {
                            CustomIcon(session.customSides, iconSize, onEditCustom)
                        }
                        Box(
                            Modifier.widthIn(min = 96.dp).height(48.dp).noRippleClick(
                                stringResource(R.string.roll_custom, session.customSides),
                            ) { onRoll(DieKind.Custom) },
                            contentAlignment = Alignment.Center,
                        ) {
                            TextMMD(stringResource(R.string.custom_label), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(
                        Modifier.weight(1f).fillMaxHeight().noRippleClick(stringResource(R.string.flip_coin)) { onRoll(DieKind.Coin) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        IconOrResult(DieKind.Coin, session, coinAsNumbers, iconSize) {
                            Icon(painterResource(DieKind.Coin.icon()), null, Modifier.size(iconSize))
                        }
                        Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                            TextMMD(stringResource(R.string.coin_label), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        CombineBar(session, onCombiningChange, onReroll)
    }
}

/**
 * Fixed-height bottom bar. Default: one "Combine rolls" button. Combining: total + count, "Done"
 * (discards the set, back to single rolls; replaces a separate Clear) and "Reroll". The bar keeps
 * its height in both modes, so the grid never moves.
 */
@Composable
private fun CombineBar(session: RollSession, onCombiningChange: (Boolean) -> Unit, onReroll: () -> Unit) {
    val totalDescription = stringResource(R.string.total_description, session.total)
    // Same top line as MMD's bottom navigation bar (2 dp), as in Mudita Focus Timer and Steps.
    HorizontalDividerMMD(thickness = NavigationBarDefaultsMMD.topDividerHeight, color = MaterialTheme.colorScheme.onSurface)
    Row(
        Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!session.combining) {
            AppOutlinedButton({ onCombiningChange(true) }, Modifier.fillMaxWidth().height(48.dp)) {
                TextMMD(stringResource(R.string.combine_rolls))
            }
        } else {
            Column(Modifier.weight(1f)) {
                TextMMD(
                    session.total.toString(), fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { contentDescription = totalDescription },
                )
                TextMMD(
                    pluralStringResource(R.plurals.dice_count, session.count, session.count),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            AppOutlinedButton({ onCombiningChange(false) }, Modifier.height(48.dp)) {
                TextMMD(stringResource(R.string.combine_done))
            }
            AppOutlinedButton(onReroll, Modifier.height(48.dp), enabled = session.canReroll) {
                TextMMD(stringResource(R.string.reroll_set))
            }
        }
    }
}

@Composable
private fun CustomIcon(sides: Int, size: Dp, onEdit: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier.size(size).clip(shape).border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface), shape)
            .noRippleClick(stringResource(R.string.edit_custom, sides), onEdit),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            TextMMD(
                sides.toString(), fontSize = if (sides >= 1000) 20.sp else 24.sp,
                lineHeight = 26.sp, fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDividerMMD(thickness = 1.5.dp, color = MaterialTheme.colorScheme.onSurface)
        TextMMD(stringResource(R.string.roll_label), fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
    }
}

/** Latest result text for [kind], or "" when the latest roll belongs to another die or has expired. */
@Composable
private fun resultText(kind: DieKind, session: RollSession, coinAsNumbers: Boolean): String {
    val latest = session.latest?.die?.takeIf { it.spec.kind == kind } ?: return ""
    return if (kind == DieKind.Coin) CoinPresentation.text(
        latest.value, coinAsNumbers,
        stringResource(R.string.coin_heads), stringResource(R.string.coin_tails),
    ) else latest.value.toString()
}

/**
 * While a result is live (5 s), it replaces the icon in the same fixed-size slot, so nothing else moves. The Custom slot keeps its edit action ([onSlotClick]) during that time.
 */
@Composable
private fun IconOrResult(
    kind: DieKind,
    session: RollSession,
    coinAsNumbers: Boolean,
    size: Dp,
    onSlotClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val text = resultText(kind, session, coinAsNumbers)
    if (text.isEmpty()) { icon(); return }
    val resultDescription = stringResource(R.string.result_description, kind.name, text)
    val click = if (onSlotClick != null) Modifier.noRippleClick(resultDescription, onSlotClick)
    else Modifier.semantics { contentDescription = resultDescription }
    Box(Modifier.fillMaxWidth().height(size).then(click), contentAlignment = Alignment.Center) {
        TextMMD(
            text, fontSize = if (kind == DieKind.Coin && text.length > 1) 24.sp else 44.sp,
            lineHeight = 48.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Modifier.noRippleClick(label: String, onClick: () -> Unit): Modifier =
    semantics { contentDescription = label }.clickable(
        role = Role.Button, interactionSource = null, indication = null, onClick = onClick,
    )

@Preview(widthDp = 360, heightDp = 560, showBackground = true)
@Composable
private fun DicePreview() {
    AppTheme { DiceScreen(RollSession(), false, {}, {}, {}, {}, {}) }
}

@Preview(widthDp = 360, heightDp = 560, showBackground = true)
@Composable
private fun CombinedDicePreview() {
    val session = RollSession().setCombining(true).roll(DieKind.D6, 0, DiceRandom { 4 })
    AppTheme { DiceScreen(session, false, {}, {}, {}, {}, {}) }
}

@Preview(widthDp = 360, heightDp = 560, showBackground = true)
@Composable
private fun ResultInIconPreview() {
    val session = RollSession().roll(DieKind.D20, 0, DiceRandom { 17 })
    AppTheme { DiceScreen(session, false, {}, {}, {}, {}, {}) }
}
