package dev.dkarski.diceroller.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.text_field.TextFieldDefaultsMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import dev.dkarski.diceroller.R
import dev.dkarski.diceroller.ui.components.SecondaryLayout
import dev.dkarski.diceroller.ui.theme.AppTheme

/**
 * Full-page editor in the native Mudita pattern (Soundscape "Export Mix", Pure Cycle "Update Period"):
 * back arrow cancels, a filled Save in the top bar confirms. Saving never rolls.
 * App clears this screen's saved state before opening it, so an earlier unsaved edit is never restored.
 */
@Composable
fun CustomDieScreen(sides: Int, onSave: (Int) -> Unit, onBack: () -> Unit) {
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(sides.toString(), TextRange(0, sides.toString().length)))
    }
    val selected = field.text.toIntOrNull()?.takeIf { it in 2..9999 }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    fun save() { selected?.let { keyboard?.hide(); onSave(it) } }
    DisposableEffect(Unit) { onDispose { keyboard?.hide() } }
    SecondaryLayout(
        title = stringResource(R.string.custom_dialog_title),
        onBack = { keyboard?.hide(); onBack() },
        actions = {
            ButtonMMD(
                onClick = { save() }, enabled = selected != null,
                modifier = Modifier.padding(end = 8.dp).height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) { TextMMD(stringResource(R.string.save_custom)) }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextFieldMMD(
                value = field,
                onValueChange = { new -> if (new.text.length <= 4 && new.text.all(Char::isDigit)) field = new },
                label = { TextMMD(stringResource(R.string.custom_sides_label)) },
                textStyle = MaterialTheme.typography.headlineMedium,
                singleLine = true,
                isError = selected == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                // A blinking cursor would refresh the E Ink panel continuously; the value opens selected instead.
                colors = TextFieldDefaultsMMD.colors(cursorColor = Color.Transparent, errorCursorColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            TextMMD(
                stringResource(if (selected == null) R.string.custom_invalid else R.string.custom_range_hint),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    LaunchedEffect(Unit) { focus.requestFocus(); keyboard?.show() }
}

@Preview(widthDp = 360, heightDp = 560, showBackground = true)
@Composable
private fun CustomDieScreenPreview() { AppTheme { CustomDieScreen(100, {}, {}) } }
