package dev.dkarski.diceroller.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import dev.dkarski.diceroller.R
import dev.dkarski.diceroller.ui.theme.AppTheme

/** Replace the empty content with your app's domain UI. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSettings: () -> Unit, content: @Composable () -> Unit = {}) {
    Column(Modifier.fillMaxSize()) {
        TopAppBarMMD(
            title = { TextMMD(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = onSettings) {
                    Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.settings_title))
                }
            },
        )
        Box(Modifier.fillMaxWidth().weight(1f)) { content() }
    }
}

@Preview(widthDp = 360, heightDp = 600, showBackground = true)
@Composable
private fun HomePreview() { AppTheme { HomeScreen({}) } }
