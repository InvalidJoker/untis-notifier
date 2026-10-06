package utils

import java.util.Properties

private const val RESET = "\u001B[0m"
private const val BOLD = "\u001B[1m"
private const val DIM = "\u001B[2m"
private const val CYAN = "\u001B[36m"
private const val MAGENTA = "\u001B[35m"

private val logo = """
       __  __      __  _
      / / / /___  / /_(_)____
     / / / / __ \/ __/ / ___/
    / /_/ / / / / /_/ (__  )
    \____/_/ /_/\__/_/____/
""".trimIndent().lines()

val appVersion: String by lazy {
    object {}.javaClass.getResourceAsStream("/version.properties")
        ?.use { Properties().apply { load(it) }.getProperty("version") }
        ?: "unknown"
}

fun printBanner() {
    val colors = System.getenv("NO_COLOR").isNullOrEmpty()
    fun style(text: String, vararg codes: String) =
        if (colors) codes.joinToString("") + text + RESET else text

    println()
    logo.forEach { println("  " + style(it, BOLD, CYAN)) }
    println("      " + style("N O T I F I E R", BOLD, MAGENTA) + "  " + style("v$appVersion", DIM))
    println()
}
