package dev.creativelogic.simulation

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

/** Precise Epic port names + deterministic explicit user scenarios; not gameplay fidelity certification. */
class ScenarioActionsTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun id(n: Int) = "00000000-0000-0000-0000-${n.toString().padStart(12, '0')}"
    private fun device(n: Int, type: String) = catalog.createInstance(type, type, id(n))
    private fun CreativeDeviceInstance.opt(name: String, value: DeviceOptionValue) =
        copy(configuredOptions = configuredOptions + (name to value))
    private fun bind(n: Int, a: CreativeDeviceInstance, e: String, b: CreativeDeviceInstance, f: String) =
        LogicConnection(id(n), a.id, DeviceEventId(e), b.id, DeviceFunctionId(f))

    @Test fun fullVaultCoreAndHudSuccessHaveOneCompletionAndNoDuplicate() {
        val keys = (1..3).map { device(it, "trigger").opt("timesCanTrigger", DeviceOptionValue.IntegerValue(1)) }
        val tracker = device(4, "tracker").opt("sharing", DeviceOptionValue.EnumValue("All"))
            .opt("assignOnGameStart", DeviceOptionValue.BooleanValue(true))
        val barrier = device(5, "barrier")
        val hud = device(6, "hud-message").opt("message", DeviceOptionValue.TextValue("Vault open!"))
        val graph = MechanicGraph(keys + tracker + barrier + hud,
            keys.mapIndexed { index, key -> bind(10 + index, key, "onTriggered", tracker, "incrementProgress") } +
                bind(14, tracker, "onComplete", barrier, "disable") +
                bind(15, tracker, "onComplete", hud, "show"))
        val s = PrototypeSimulationSession(graph)
        s.startGame(); s.resume()
        keys.forEachIndexed { idx, key ->
            s.trigger(key.id, "player-1"); s.resume()
            assertEquals(idx + 1, (s.states.getValue(tracker.id) as TrackerRuntime).progress)
            assertEquals(idx < 2, (s.states.getValue(barrier.id) as BarrierRuntime).enabled)
        }
        assertEquals(HudMessageRuntime(true), s.states.getValue(hud.id))
        assertEquals(1, s.trace.count { it.kind == TraceKind.EVENT && it.detail == "onComplete" })
        assertEquals(1, s.trace.count { it.kind == TraceKind.FUNCTION && it.detail == "show" })
        s.trigger(keys[0].id); s.resume()
        assertEquals(3, (s.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(1, s.trace.count { it.kind == TraceKind.EVENT && it.detail == "onComplete" })
        val actual = s.trace.joinToString("\n") {
            "${it.timeMillis}|${it.kind}|${it.deviceId}|${it.detail}|${it.actorId}"
        }.trim()
        val fixture = requireNotNull(javaClass.getResourceAsStream("/fixtures/three-key-vault-phase32.trace"))
            .bufferedReader().use { it.readText().trim() }
        assertEquals(fixture, actual)
        s.reset()
        assertEquals(HudMessageRuntime(false), s.states.getValue(hud.id))
        assertEquals(BarrierRuntime(true), s.states.getValue(barrier.id))
        assertTrue(s.trace.isEmpty())
    }

    @Test fun roundStartAndHudAutomaticShowRequireExplicitGameStart() {
        val round = device(1, "round-settings")
        val message = device(2, "hud-message").opt("showOnRoundStart", DeviceOptionValue.BooleanValue(true))
        val button = device(3, "button")
        val graph = MechanicGraph(listOf(round, message, button), listOf(bind(4, round, "onRoundStart", button, "disable")))
        val s = PrototypeSimulationSession(graph)
        assertFalse(s.gameStarted)
        assertEquals(HudMessageRuntime(false), s.states.getValue(message.id))
        s.startGame(); s.resume()
        assertTrue(s.gameStarted)
        assertEquals(HudMessageRuntime(true), s.states.getValue(message.id))
        assertEquals(ButtonRuntime(false), s.states.getValue(button.id))
        assertEquals(1, s.trace.count { it.kind == TraceKind.EVENT && it.detail == "onRoundStart" })
        s.startGame(); s.resume()
        assertEquals(1, s.trace.count { it.kind == TraceKind.EVENT && it.detail == "onRoundStart" })
    }

    @Test fun simulatedPlayerEliminationAndCreatureEliminationDoNotCrossRoute() {
        val elim = device(1, "elimination-manager")
        val creature = device(2, "creature-manager")
        val tracker = device(3, "tracker").opt("sharing", DeviceOptionValue.EnumValue("All"))
            .opt("assignOnGameStart", DeviceOptionValue.BooleanValue(true))
        val graph = MechanicGraph(listOf(elim, creature, tracker), listOf(
            bind(4, elim, "onElimination", tracker, "incrementProgress"),
            bind(5, creature, "onMatchingCreatureTypeEliminated", tracker, "incrementProgress"),
        ))
        val s = PrototypeSimulationSession(graph)
        s.playerEliminated("player-2", "player-1"); s.resume()
        assertEquals(1, (s.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(listOf("onEliminated", "onElimination"), s.trace.filter { it.kind == TraceKind.EVENT }.map { it.detail })
        assertEquals("player-2", s.trace.first { it.detail == "onEliminated" }.actorId)
        assertEquals("player-1", s.trace.first { it.detail == "onElimination" }.actorId)
        s.creatureEliminated("Fiend", "player-1"); s.resume()
        assertEquals(2, (s.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(1, s.trace.count { it.detail == "onMatchingCreatureTypeEliminated" })
        try { s.creatureEliminated("ImaginaryCreature"); fail("Unexpected type accepted") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun buttonInteractionRespectsEnabledAndOneUse() {
        val button = device(1, "button").opt("timesCanTrigger", DeviceOptionValue.IntegerValue(1))
        val tracker = device(2, "tracker").opt("sharing", DeviceOptionValue.EnumValue("All"))
            .opt("assignOnGameStart", DeviceOptionValue.BooleanValue(true))
        val s = PrototypeSimulationSession(MechanicGraph(listOf(button, tracker),
            listOf(bind(3, button, "onInteract", tracker, "incrementProgress"))))
        s.interactButton(button.id); s.interactButton(button.id); s.resume()
        assertEquals(ButtonRuntime(false, 1), s.states.getValue(button.id))
        assertEquals(1, (s.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(1, s.trace.count { it.kind == TraceKind.IGNORED && it.detail == "Button unavailable" })
    }

    @Test fun sameScenarioAndGraphYieldIdenticalTrace() {
        val elim = device(1, "elimination-manager")
        val creature = device(2, "creature-manager")
        val graph = MechanicGraph(listOf(elim, creature))
        fun run(): List<SimulationTraceEntry> = PrototypeSimulationSession(graph).run {
            startGame(); playerEliminated(); creatureEliminated(); resume(); trace
        }
        assertEquals(run(), run())
    }
}
