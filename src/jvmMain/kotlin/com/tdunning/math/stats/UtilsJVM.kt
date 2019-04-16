package com.tdunning.math.stats

actual fun mpassert(value: Boolean, lazyMessage: () -> Any) {
    assert(value,lazyMessage)
}