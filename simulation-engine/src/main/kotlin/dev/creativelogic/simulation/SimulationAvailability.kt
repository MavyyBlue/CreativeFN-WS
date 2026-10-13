package dev.creativelogic.simulation

/** Narrow in-app test scene exists; complete Fortnite-verified simulation does not. */
object SimulationAvailability {
    const val isPrototypeAvailable: Boolean = true
    const val isImplemented: Boolean = false
    const val explanation: String = "A local deterministic test scene is available for a limited Epic-documented device subset. Fortnite 1:1 fidelity is not certified."
}
