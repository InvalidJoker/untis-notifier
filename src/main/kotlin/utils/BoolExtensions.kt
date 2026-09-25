package utils

inline fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) {
        block.invoke()
    }
    return this
}

inline fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this) {
        block.invoke()
    }
    return this
}