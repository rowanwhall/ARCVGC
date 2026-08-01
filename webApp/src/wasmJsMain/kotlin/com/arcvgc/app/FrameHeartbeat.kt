package com.arcvgc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { globalThis.__arcFrameTs = Date.now(); }")
private external fun stampFrameHeartbeat()

private const val HEARTBEAT_INTERVAL_MS = 3_000L

/**
 * Stamps a JS-visible timestamp (`globalThis.__arcFrameTs`) after each
 * successfully rendered frame, every ~3s. The watchdog in `index.html` reloads
 * the page when the stamp goes stale while the tab is visible — catching
 * render-loop death (killed rAF chain, failed canvas recreation) that fires no
 * `webglcontextlost` event. Awaiting a frame before stamping is what makes the
 * stamp meaningful: if Compose's frame clock is dead, the stamp stops.
 *
 * Depends on the frame clock servicing `withFrameNanos` awaiters even when the
 * UI is idle (BroadcastFrameClock schedules a frame on new awaiters — verified
 * live on an untouched page). Cost: one forced frame per interval, so the page
 * never fully idles. If a Compose upgrade ever parks the clock on idle pages,
 * an idle visible tab would false-positive the staleness watchdog — re-verify
 * after major Compose updates.
 */
@Composable
fun FrameHeartbeat() {
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { }
            stampFrameHeartbeat()
            delay(HEARTBEAT_INTERVAL_MS)
        }
    }
}
