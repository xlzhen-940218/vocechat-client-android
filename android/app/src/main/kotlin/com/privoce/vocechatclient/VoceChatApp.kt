package com.privoce.vocechatclient

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.privoce.vocechatclient.data.repository.VoceChatRepository

class VoceChatApp : Application() {

    lateinit var repository: VoceChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = VoceChatRepository(this)

        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(repository.apiClient.okHttpClient)
            .components { add(VideoFrameDecoder.Factory()) }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    companion object {
        lateinit var instance: VoceChatApp
            private set
    }
}
