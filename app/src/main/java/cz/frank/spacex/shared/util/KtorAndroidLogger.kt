package cz.frank.spacex.shared.util

import android.util.Log
import io.ktor.client.plugins.logging.Logger

class KtorAndroidLogger : Logger {
    override fun log(message: String) {
        Log.d("Ktor", message)
    }
}
