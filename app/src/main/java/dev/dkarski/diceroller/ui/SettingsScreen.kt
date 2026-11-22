package dev.dkarski.diceroller.ui

import androidx.annotation.DrawableRes
import com.mudita.mmd.components.switcher.SwitchMMD
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.diceroller.R
import dev.dkarski.diceroller.ui.components.DottedDivider
import dev.dkarski.diceroller.ui.theme.AppTheme

/** Common settings. App-specific controls precede the App section; actions stay below the scroll area. */
@Composable
fun SettingsScreen(
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    languageName: String,
    onLanguage: () -> Unit,
    onAbout: () -> Unit,
    onTipJar: (() -> Unit)?,
    contentPadding: PaddingValues = PaddingValues(),
    appSettings: (@Composable () -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                // Tapping empty space ends editing of the domain fields.
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            appSettings?.let { content ->
                content()
                Spacer(Modifier.height(32.dp))
            }
            SettingsSection(stringResource(R.string.settings_section_app))
            DottedDivider()
            SettingsSwitchRow(stringResource(R.string.settings_vibration), checked = vibration, onCheckedChange = onVibrationChange)
            DottedDivider()
            SettingsRow(stringResource(R.string.settings_language), onClick = onLanguage, value = languageName)
            DottedDivider()
            SettingsRow(stringResource(R.string.settings_about), onClick = onAbout)
            DottedDivider()
            if (onTipJar != null) {
                SettingsRow(stringResource(R.string.about_tip_jar), onClick = onTipJar)
                DottedDivider()
            }
        }
        bottomAction?.invoke()
    }
}

/** A row with a switch; the whole row toggles it. */
@Composable
fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
                interactionSource = null,
                indication = null,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        // The row handles the toggle (bigger touch area, one semantics node).
        SwitchMMD(checked = checked, onCheckedChange = null)
    }
}

/** Section heading (like "System" / "Information" in the system Settings). */
@Composable
fun SettingsSection(text: String) {
    TextMMD(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

@Composable
fun SettingsRow(
    label: String,
    onClick: () -> Unit,
    @DrawableRes chevron: Int = R.drawable.ic_chevron_right,
    value: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button, interactionSource = null, indication = null)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (value != null) {
            // Current value (e.g. the chosen language), like the system Settings rows.
            TextMMD(
                value,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp, end = 4.dp).widthIn(max = 170.dp),
            )
        }
        Icon(painterResource(chevron), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(widthDp = 360, heightDp = 600, showBackground = true)
@Composable
private fun SettingsPreview() {
    AppTheme { SettingsScreen(true, {}, "English", {}, {}, null) }
}
