package cz.frank.spacex.main

import android.app.Application
import cz.frank.spacex.main.di.spaceXModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.loadKoinModules

class SpaceXApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SpaceXApplication)
            loadKoinModules(spaceXModule)
        }
    }
}
