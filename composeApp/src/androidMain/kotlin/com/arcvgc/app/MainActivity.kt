package com.arcvgc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arcvgc.app.domain.model.DeepLink
import com.arcvgc.app.domain.model.parseDeepLink
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val deepLink = intent.data?.let { uri ->
            val pathAndQuery = buildString {
                append(uri.path ?: "")
                uri.query?.let { append("?$it") }
            }
            android.util.Log.d("DeepLink", "MainActivity: uri=$uri, pathAndQuery=$pathAndQuery")
            val parsed = parseDeepLink(pathAndQuery)
            android.util.Log.d("DeepLink", "MainActivity: parsed=$parsed")
            parsed
        }

        setContent {
            App(deepLink = deepLink)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
