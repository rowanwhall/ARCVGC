package com.arcvgc.app

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.arcvgc.app.data.initializeSentry
import com.arcvgc.app.di.CatalogInitializer
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath
import javax.inject.Inject

@HiltAndroidApp
class ShowdownApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var catalogInitializer: CatalogInitializer

    override fun onCreate() {
        super.onCreate()
        initializeSentry()
    }

    override fun newImageLoader(context: coil3.PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(this@ShowdownApplication, 0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
