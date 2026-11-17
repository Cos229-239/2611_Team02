package com.example.bloopyblocks

import android.app.Application
import com.example.bloopyblocks.data.AppContainer

class BloopyBlocksApplication: Application() {
    lateinit var appContainer: AppContainer
    private set

    override fun onCreate() {
        super.onCreate()
        appContainer= AppContainer(this)
    }
}
