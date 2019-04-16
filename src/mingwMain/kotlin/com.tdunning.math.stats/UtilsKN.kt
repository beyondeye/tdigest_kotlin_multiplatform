package com.tdunning.math.stats

actual fun mpassert(value: Boolean, lazyMessage: () -> Any) {
     if (!value) {
            val message = lazyMessage()
            throw AssertionError(message)
        }
}