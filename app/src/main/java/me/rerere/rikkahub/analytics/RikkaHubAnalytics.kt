package me.rerere.rikkahub.analytics

/**
 * Host-provided analytics boundary. The library never initializes Firebase
 * and does not require a Firebase configuration in the consuming application.
 */
fun interface RikkaHubAnalytics {
    fun logEvent(name: String)
}

object NoOpRikkaHubAnalytics : RikkaHubAnalytics {
    override fun logEvent(name: String) = Unit
}
