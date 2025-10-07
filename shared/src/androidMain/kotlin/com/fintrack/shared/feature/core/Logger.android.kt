package com.fintrack.shared.feature.core

actual class KMPLogger actual constructor() {
    actual fun debug(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    actual fun info(tag: String, message: String) {
        android.util.Log.i(tag, message)
    }

    actual fun warning(tag: String, message: String) {
        android.util.Log.w(tag, message)
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        android.util.Log.e(tag, message, throwable)
    }
}