package dev.creativelogic.simulation

/** An explicitly supplied test input, not evidence that a Fortnite event occurred. */
data class ManualScenarioSignal(
    val deviceId: String,
    val eventId: String,
    val actor: String,
    val value: String,
)

/**
 * Repeat a small user-written scenario from a clean state. Each input is run to idle
 * before the next one, making trigger order explicit and repeatable for debugging.
 * The blueprint and Room database are never modified.
 */
object ValueScenarioReplay {
    const val MAX_SIGNALS = 128

    fun replay(session: ValueSignalSession, signals: List<ManualScenarioSignal>) {
        require(signals.size <= MAX_SIGNALS) { "Scenario contains too many inputs" }
        session.reset()
        for (signal in signals) {
            if (session.halted) break
            session.inject(signal.deviceId, signal.eventId, signal.actor, signal.value)
            session.run()
        }
    }
}
