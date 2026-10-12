package dev.creativelogic.simulation

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class PrototypeSimulationSessionTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun id(n: Int) = "00000000-0000-0000-0000-${n.toString().padStart(12, '0')}"
    private fun device(n: Int, type: String) = catalog.createInstance(type, "$type-$n", id(n))
    private fun CreativeDeviceInstance.set(key: String, value: DeviceOptionValue) =
        copy(configuredOptions = configuredOptions + (key to value))
    private fun bind(n: Int, src: CreativeDeviceInstance, event: String, dst: CreativeDeviceInstance, fn: String) =
        LogicConnection(id(n), src.id, DeviceEventId(event), dst.id, DeviceFunctionId(fn))

    /** The Three-Key Vault's executable core. HUD Message is deliberately NOT present. */
    private fun vault(): Pair<MechanicGraph, List<CreativeDeviceInstance>> {
        val triggers = (1..3).map { device(it, "trigger").set("timesCanTrigger", DeviceOptionValue.IntegerValue(1)) }
        val tracker = device(4, "tracker")
            .set("sharing", DeviceOptionValue.EnumValue("All"))
            .set("assignOnGameStart", DeviceOptionValue.BooleanValue(true))
        val barrier = device(5, "barrier")
        val bindings = triggers.mapIndexed { index, trigger -> bind(11 + index, trigger, "onTriggered", tracker, "incrementProgress") } +
            bind(14, tracker, "onComplete", barrier, "disable")
        return MechanicGraph(triggers + tracker + barrier, bindings) to (triggers + tracker + barrier)
    }

    @Test fun threeKeyVaultCoreProgressesAndDisablesOnlyAfterThird() {
        val (graph, devices) = vault()
        val session = PrototypeSimulationSession(graph)
        val (first, second, third, tracker, barrier) = devices
        assertEquals(BarrierRuntime(true), session.states.getValue(barrier.id))
        session.trigger(first.id); assertEquals(SimulationStatus.READY, session.resume())
        assertEquals(1, (session.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(BarrierRuntime(true), session.states.getValue(barrier.id))
        session.trigger(second.id); session.resume()
        assertEquals(2, (session.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(BarrierRuntime(true), session.states.getValue(barrier.id))
        session.trigger(third.id); session.resume()
        assertEquals(TrackerRuntime(true, 3, true), session.states.getValue(tracker.id))
        assertEquals(BarrierRuntime(false), session.states.getValue(barrier.id))
        assertEquals(1, session.trace.count { it.kind == TraceKind.EVENT && it.deviceId == tracker.id && it.detail == "onComplete" })
        session.trigger(first.id); session.trigger(third.id); session.resume()
        assertEquals(3, (session.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(3, session.trace.count { it.kind == TraceKind.EVENT && it.detail == "onTriggered" })
        assertEquals(2, session.trace.count { it.kind == TraceKind.IGNORED && it.detail == "Trigger unavailable" })
    }

    @Test fun threeKeyVaultCoreMatchesCheckedInGoldenTrace() {
        val (graph, devices) = vault()
        val session = PrototypeSimulationSession(graph)
        devices.take(3).forEach { session.trigger(it.id); session.resume() }
        val actual = session.trace.joinToString("\n") { entry ->
            "${entry.timeMillis}|${entry.kind}|${devices.indexOfFirst { it.id == entry.deviceId } + 1}|${entry.detail}|${entry.actorId}"
        }.trim()
        val expected = requireNotNull(javaClass.getResourceAsStream("/fixtures/three-key-vault-core.trace")) {
            "Missing golden trace fixture"
        }.bufferedReader().use { it.readText().trim() }
        assertEquals(expected, actual)
    }

    @Test fun orderingAndTraceAreFullyRepeatableForSameGraphAndInputs() {
        val (graph, devices) = vault()
        fun run(): List<SimulationTraceEntry> = PrototypeSimulationSession(graph).run {
            trigger(devices[2].id); trigger(devices[0].id); trigger(devices[1].id)
            resume()
            trace
        }
        assertEquals(run(), run())
        val orderedEvents = run().filter { it.kind == TraceKind.EVENT }.map { it.deviceId to it.detail }
        assertEquals(listOf(
            devices[2].id to "onTriggered", devices[0].id to "onTriggered",
            devices[1].id to "onTriggered", devices[3].id to "onComplete",
        ), orderedEvents)
    }

    @Test fun pauseStepResetAndVirtualTimeRemainIndependentOfWallClock() {
        val (graph, devices) = vault()
        val session = PrototypeSimulationSession(graph)
        session.trigger(devices[0].id, actorId = "builder", afterMillis = 20)
        session.trigger(devices[1].id, actorId = "builder", afterMillis = 10)
        assertEquals(2, session.pendingEvents)
        assertEquals(SimulationStatus.PAUSED, session.status)
        assertTrue(session.step())
        assertEquals(10L, session.clockMillis)
        assertEquals(devices[1].id, session.trace.first().deviceId)
        assertEquals("builder", session.trace.first().actorId)
        assertEquals(SimulationStatus.PAUSED, session.status)
        assertTrue(session.step()) // tracker function at t=10, before input at t=20
        assertEquals(1, (session.states.getValue(devices[3].id) as TrackerRuntime).progress)
        session.resume()
        assertEquals(20L, session.clockMillis)
        assertEquals(2, (session.states.getValue(devices[3].id) as TrackerRuntime).progress)
        assertEquals(0, session.pendingEvents)
        session.advanceTo(100)
        assertEquals(100L, session.clockMillis)
        session.reset()
        assertEquals(0L, session.clockMillis)
        assertEquals(0, session.pendingEvents)
        assertTrue(session.trace.isEmpty())
        assertEquals(0, (session.states.getValue(devices[3].id) as TrackerRuntime).progress)
        assertEquals(BarrierRuntime(true), session.states.getValue(devices[4].id))
        assertEquals(SimulationStatus.PAUSED, session.status)
    }

    @Test fun cooldownAndOneUseAreEnforcedWithVirtualTime() {
        val trigger = device(1, "trigger").set("resetDelay", DeviceOptionValue.DurationValue(100))
        val session = PrototypeSimulationSession(MechanicGraph(devices = listOf(trigger)))
        session.trigger(trigger.id); session.trigger(trigger.id)
        session.resume()
        assertEquals(1, (session.states.getValue(trigger.id) as TriggerRuntime).triggerCount)
        assertEquals(1, session.trace.count { it.kind == TraceKind.IGNORED })
        session.advanceTo(100)
        session.trigger(trigger.id); session.resume()
        assertEquals(2, (session.states.getValue(trigger.id) as TriggerRuntime).triggerCount)
    }

    @Test fun intentionalFeedbackLoopHaltsAndRetainsTrace() {
        val a = device(1, "trigger")
        val b = device(2, "trigger")
        val graph = MechanicGraph(listOf(a, b), listOf(
            bind(3, a, "onTriggered", b, "trigger"),
            bind(4, b, "onTriggered", a, "trigger"),
        ))
        val session = PrototypeSimulationSession(graph, limits = SimulationLimits(maxActionsPerTick = 5, maxPropagationDepth = 30, maxSessionActions = 100))
        session.trigger(a.id)
        assertEquals(SimulationStatus.POSSIBLE_EVENT_LOOP, session.resume())
        assertEquals("POSSIBLE EVENT LOOP", session.loopWarning?.reason)
        assertTrue(session.loopWarning!!.deviceIds.contains(a.id))
        assertTrue(session.trace.last().kind == TraceKind.WARNING)
        assertTrue(session.pendingEvents > 0)
        assertFalse(session.step())
        session.reset()
        assertNull(session.loopWarning)
        assertTrue(session.trace.isEmpty())
    }

    @Test fun depthAndSessionCapsIndependentlyStopFeedbackCycles() {
        val a = device(1, "trigger")
        val b = device(2, "trigger")
        val graph = MechanicGraph(listOf(a, b), listOf(
            bind(3, a, "onTriggered", b, "trigger"),
            bind(4, b, "onTriggered", a, "trigger"),
        ))
        val depthLimited = PrototypeSimulationSession(graph, limits = SimulationLimits(100, 1, 100))
        depthLimited.trigger(a.id)
        assertEquals(SimulationStatus.POSSIBLE_EVENT_LOOP, depthLimited.resume())
        assertEquals("POSSIBLE EVENT LOOP", depthLimited.trace.last().detail)
        val totalLimited = PrototypeSimulationSession(graph, limits = SimulationLimits(100, 100, 2))
        totalLimited.trigger(a.id)
        assertEquals(SimulationStatus.POSSIBLE_EVENT_LOOP, totalLimited.resume())
        assertEquals("POSSIBLE EVENT LOOP", totalLimited.trace.last().detail)
    }

    @Test fun queuedInputsHaveAMemorySafetyLimit() {
        val trigger = device(1, "trigger")
        val session = PrototypeSimulationSession(
            MechanicGraph(listOf(trigger)), limits = SimulationLimits(maxPendingActions = 2),
        )
        session.trigger(trigger.id)
        session.trigger(trigger.id)
        session.trigger(trigger.id)
        assertEquals(SimulationStatus.POSSIBLE_EVENT_LOOP, session.status)
        assertEquals(2, session.pendingEvents)
        assertEquals(TraceKind.WARNING, session.trace.single().kind)
    }

    @Test fun ambiguousOrUnsupportedPrototypeDeviceConfigurationsAreRejected() {
        val tracker = device(1, "tracker") // Individual sharing is NOT modeled.
        expectIllegal { PrototypeSimulationSession(MechanicGraph(devices = listOf(tracker))) }
        val barrier = device(2, "barrier").set("enabledDuringPhase", DeviceOptionValue.EnumValue("Gameplay Only"))
        expectIllegal { PrototypeSimulationSession(MechanicGraph(devices = listOf(barrier))) }
        val unknown = device(3, "tracker").set("sharing", DeviceOptionValue.EnumValue("All"))
            .set("targetValue", DeviceOptionValue.IntegerValue(0))
        expectIllegal { PrototypeSimulationSession(MechanicGraph(devices = listOf(unknown))) }
    }

    @Test fun disabledTriggerAndUnassignedTrackerDoNotInventSuccess() {
        val source = device(1, "trigger").set("enabledOnGameStart", DeviceOptionValue.BooleanValue(false))
        val tracker = device(2, "tracker").set("sharing", DeviceOptionValue.EnumValue("All"))
        val session = PrototypeSimulationSession(MechanicGraph(listOf(source, tracker), listOf(bind(3, source, "onTriggered", tracker, "incrementProgress"))))
        session.trigger(source.id); session.resume()
        assertEquals(0, (session.states.getValue(tracker.id) as TrackerRuntime).progress)
        assertEquals(0, session.trace.count { it.kind == TraceKind.EVENT })
    }

    @Test fun setupRejectsMalformedBindingsWithoutPersistingAnything() {
        val source = device(1, "trigger")
        val tracker = device(2, "tracker").set("sharing", DeviceOptionValue.EnumValue("All"))
        val graph = MechanicGraph(listOf(source, tracker), listOf(bind(3, source, "onTriggered", tracker, "disable")))
        expectIllegal { PrototypeSimulationSession(graph) }
    }

    private inline fun expectIllegal(action: () -> Unit) {
        try { action(); fail("Expected unsupported configuration to be rejected") }
        catch (_: IllegalArgumentException) { /* expected */ }
    }
}
