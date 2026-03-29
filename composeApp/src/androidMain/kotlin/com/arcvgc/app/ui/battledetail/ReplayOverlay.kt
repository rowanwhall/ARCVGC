package com.arcvgc.app.ui.battledetail

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import com.arcvgc.app.ui.model.ReplayGame
import com.arcvgc.app.ui.model.ReplayNavState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReplayOverlay(
    navState: ReplayNavState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    statusBarPadding: Dp = 0.dp
) {
    BackHandler { onDismiss() }

    var currentIndex by remember { mutableIntStateOf(navState.initialIndex) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUrl = navState.games[currentIndex].replayUrl
    val hasMultipleGames = navState.games.size > 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = statusBarPadding)
    ) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isLoading) 0f else 1f),
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        loadUrl(currentUrl)
                    }
                },
                update = { webView ->
                    if (webView.url != currentUrl) {
                        webView.loadUrl(currentUrl)
                    }
                }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasMultipleGames) {
                IconButton(
                    onClick = { currentIndex-- },
                    enabled = currentIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous game"
                    )
                }

                val currentGame = navState.games[currentIndex]
                val label = currentGame.positionInSet?.let { "Game $it" }
                    ?: "Game ${currentIndex + 1}"
                Text(
                    text = "$label of ${navState.games.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { currentIndex++ },
                    enabled = currentIndex < navState.games.lastIndex
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next game"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReplayOverlayPreview() {
    MaterialTheme {
        ReplayOverlay(
            navState = ReplayNavState(
                games = listOf(
                    ReplayGame(positionInSet = 1, replayUrl = "https://replay.pokemonshowdown.com/example1"),
                    ReplayGame(positionInSet = 2, replayUrl = "https://replay.pokemonshowdown.com/example2"),
                    ReplayGame(positionInSet = 3, replayUrl = "https://replay.pokemonshowdown.com/example3")
                ),
                initialIndex = 1
            ),
            onDismiss = {}
        )
    }
}
