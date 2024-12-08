package cz.frank.spacex.main

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import cz.frank.spacex.main.di.spaceXModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.loadKoinModules

class SpaceXApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SpaceXApplication)
            loadKoinModules(spaceXModule)
        }
        newImageLoader(applicationContext)
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, MEMORY_CACHE_MAX_SIZE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(DISK_CACHE_MAX_SIZE_BYTES)
                    .build()
            }
            .logger(DebugLogger())
            .crossfade(true)
            .build()
    }

    companion object {
       private const val MEMORY_CACHE_MAX_SIZE_PERCENT = 0.2
        private const val DISK_CACHE_MAX_SIZE_BYTES: Long = 5 * 1024 * 1024
    }
}
