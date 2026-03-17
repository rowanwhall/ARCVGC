package com.arcvgc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arcvgc.app.domain.model.parseDeepLink
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val deepLinkTarget = intent.data?.path?.let { parseDeepLink(it) }

        setContent {
            App(deepLinkTarget = deepLinkTarget)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
