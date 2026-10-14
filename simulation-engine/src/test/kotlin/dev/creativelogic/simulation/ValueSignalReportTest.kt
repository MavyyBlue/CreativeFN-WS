package dev.creativelogic.simulation

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

/** Pure JVM verification for Phase 5.2 readable outputs and repeatable manual input sequences. */
class ValueSignalReportTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun id(n: Int) = "00000000-0000-0000-0000-${n.toString().padStart(12, '0')}"
    private fun device(n: Int, type: String) = catalog.createInstance(type, "$type-$n", id(n))
    private fun bind(n: Int, a: CreativeDeviceInstance, event: String, b: CreativeDeviceInstance, function: String) =
        LogicConnection(id(n), a.id, DeviceEventId(event), b.id, DeviceFunctionId(function))
    private fun tracker(n: Int) = device(n, "tracker").let { d ->
        d.copy(configuredOptions = d.configuredOptions +
            ("assignOnGameStart" to DeviceOptionValue.BooleanValue(true)) +
            ("targetValue" to DeviceOptionValue.IntegerValue(2)))
    }

    @Test fun reportSeparatesInjectedInputsFromCalculatedValues() {
        val zone = device(1, "mutator-zone"); val target = tracker(2)
        val graph = MechanicGraph(listOf(zone, target), listOf(bind(4, zone,
            "on_player_entering_zone_send_event_to", target, "incrementProgress")))
        val session = ValueSignalSession(graph)
        val initially = ValueSignalReport.summarize(session)
        assertEquals(0, initially.injectedInputs)
        assertTrue(ValueSignalReport.explain(initially).contains("No events injected"))
        session.inject(zone.id, "on_player_entering_zone_send_event_to", "p1", "entered")
        assertEquals(1, ValueSignalReport.summarize(session).queuedActions)
        session.run()
        val summary = ValueSignalReport.summarize(session)
        assertEquals(1, summary.injectedInputs)
        assertEquals(1, summary.receivedFunctions)
        assertTrue(summary.changedValues > 0)
        assertEquals(0, summary.unmodeledEffects)
        val text = ValueSignalReport.export(graph, session)
        assertTrue(text.contains("NOT a Fortnite gameplay"))
        assertTrue(text.contains("mutator-zone-1"))
        assertTrue(text.contains("actor=p1"))
        assertTrue(text.contains("progress=1"))
    }

    @Test fun unsupportedFunctionsAndIncompleteResultsRemainVisible() {
        val zone = device(1, "mutator-zone"); val item = device(2, "item-granter")
        val graph = MechanicGraph(listOf(zone, item), listOf(bind(5, zone,
            "on_player_entering_zone_send_event_to", item, "grant_item_when_receiving_from")))
        val session = ValueSignalSession(graph)
        session.inject(zone.id, "on_player_entering_zone_send_event_to"); session.run()
        val summary = ValueSignalReport.summarize(session)
        assertEquals(1, summary.unmodeledEffects)
        assertTrue(summary.hasUnknownOutcomes)
        assertTrue(ValueSignalReport.explain(summary).contains("unmodeled"))
        assertTrue(ValueSignalReport.export(graph, session).contains("UNSUPPORTED"))
    }

    @Test fun replayRunsDeclaredEventsInSequenceWithoutSavingThem() {
        val zone = device(1, "mutator-zone"); val target = tracker(2)
        val graph = MechanicGraph(listOf(zone, target), listOf(bind(4, zone,
            "on_player_entering_zone_send_event_to", target, "incrementProgress")))
        val inputs = listOf(
            ManualScenarioSignal(zone.id, "on_player_entering_zone_send_event_to", "p1", "first"),
            ManualScenarioSignal(zone.id, "on_player_entering_zone_send_event_to", "p2", "second"),
        )
        val s = ValueSignalSession(graph)
        ValueScenarioReplay.replay(s, inputs)
        assertEquals("2", s.values.getValue(target.id)["progress"])
        assertEquals("true", s.values.getValue(target.id)["completed"])
        assertEquals(2, ValueSignalReport.summarize(s).injectedInputs)
        val first = ValueSignalReport.export(graph, s)
        ValueScenarioReplay.replay(s, inputs)
        assertEquals(first, ValueSignalReport.export(graph, s))
        assertEquals(0, s.pending)
    }

    @Test fun replayRejectsUnboundedScenarioSize() {
        val s = ValueSignalSession(MechanicGraph())
        val tooMany = List(ValueScenarioReplay.MAX_SIGNALS + 1) { ManualScenarioSignal("id", "event", "actor", "true") }
        try { ValueScenarioReplay.replay(s, tooMany); fail("Unbounded replay accepted") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun reportDistinguishesSafetyStop() {
        val trigger = device(1, "trigger")
        val graph = MechanicGraph(listOf(trigger), listOf(bind(8, trigger, "onTriggered", trigger, "trigger")))
        val s = ValueSignalSession(graph, maxActions = 10, maxDepth = 20, maxPending = 10)
        s.inject(trigger.id, "onTriggered"); s.run()
        assertTrue(s.halted)
        val summary = ValueSignalReport.summarize(s)
        assertTrue(summary.stopped)
        assertTrue(summary.safetyWarnings > 0)
        assertTrue(ValueSignalReport.explain(summary).contains("Safety stop"))
    }

    @Test fun largeReferenceGraphReportIsBoundedAndReadable() {
        val devices = (1..208).map { device(it, "mutator-zone") }
        val graph = MechanicGraph(devices)
        val session = ValueSignalSession(graph)
        session.inject(devices.first().id, "on_player_entering_zone_send_event_to", "p1", "enter")
        session.run()
        val text = ValueSignalReport.export(graph, session)
        assertEquals(208, graph.devices.size)
        assertEquals(1, ValueSignalReport.summarize(session).injectedInputs)
        assertTrue(text.contains("mutator-zone-208"))
        assertFalse(session.halted)
    }

}
