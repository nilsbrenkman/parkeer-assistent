package nl.parkeerassistent.android.util

import nl.parkeerassistent.android.ui.AppActivity

class ExceptionHandler(private val app: AppActivity) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        app.handleException(throwable)
    }
}