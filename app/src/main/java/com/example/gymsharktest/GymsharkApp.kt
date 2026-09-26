package com.example.gymsharktest

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class GymsharkApp : Application(), SingletonImageLoader.Factory {

    /**
     * A [Provider] rather than the loader itself: Coil asks for the loader lazily on the first
     * image request, so building the OkHttp stack eagerly during [onCreate] would only slow
     * startup down.
     */
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()
}
