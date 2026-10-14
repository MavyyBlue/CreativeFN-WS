package dev.creativelogic.simulation

import dev.creativelogic.catalog.DeviceCatalog
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import java.util.ArrayDeque

/**
 * Offline EVENT/VALUE sketchpad, not an emulator of Fortnite's game world.
 * Injected events bypass the source device's actual trigger conditions on purpose.
 * Only explicit, narrow function effects below change values. Everything else is traced as unmodeled.
 */
class ValueSignalSession(
    val graph: MechanicGraph,
    private val catalog: DeviceCatalog = InitialDeviceCatalog.catalog,
    private val maxActions: Int = 512,
    private val maxDepth: Int = 48,
    private val maxPending: Int = 512,
) {
    enum class Kind { INPUT, EVENT, FUNCTION, VALUE, UNSUPPORTED, WARNING }
    data class Entry(val kind: Kind, val deviceId: String, val detail: String, val actor: String, val value: String)
    private sealed interface Work {
        data class Inject(val id: String, val event: String, val actor: String, val value: String, val depth: Int) : Work
        data class Receive(val id: String, val function: String, val actor: String, val value: String, val depth: Int) : Work
    }
    private val devices = graph.devices.associateBy { it.id }
    private val bindingIndex: Map<Pair<String, String>, List<LogicConnection>>
    private val queue = ArrayDeque<Work>()
    private val mutableTrace = mutableListOf<Entry>()
    private var mutableValues: Map<String, Map<String, String>> = emptyMap()
    private var processed = 0
    var halted = false
        private set
    val pending get() = queue.size
    val trace: List<Entry> get() = mutableTrace.toList()
    val values: Map<String, Map<String, String>> get() = mutableValues.toMap()

    init {
        require(maxActions > 0 && maxDepth > 0 && maxPending > 0)
        require(devices.size == graph.devices.size) { "Duplicate device IDs" }
        require(graph.connections.map { it.id }.distinct().size == graph.connections.size) { "Duplicate connection ID" }
        graph.devices.forEach(catalog::validate)
        graph.connections.forEach { catalog.validateBinding(it, graph.devices) }
        require(graph.connections.map { listOf(it.sourceDeviceId,it.sourceEventId.value,it.targetDeviceId,it.targetFunctionId.value) }.distinct().size == graph.connections.size)
        bindingIndex = graph.connections.sortedBy { it.id }.groupBy { it.sourceDeviceId to it.sourceEventId.value }
        reset()
    }

    fun inject(deviceId: String, eventId: String, actor: String = "player-1", value: String = "true") {
        require(!halted) { "Reset after the safety stop" }
        require(actor.isNotBlank()) { "Enter an actor name" }
        require(devices[deviceId]?.let { d -> catalog.definition(d.deviceTypeId).events.any { it.id.value == eventId } } == true) {
            "Choose a documented event for an existing device"
        }
        add(Work.Inject(deviceId,eventId,actor,value,0))
    }

    fun reset() {
        queue.clear(); mutableTrace.clear(); processed = 0; halted = false
        mutableValues = graph.devices.associate { d ->
            val state = mutableMapOf<String,String>()
            when(d.deviceTypeId) {
                "tracker" -> {
                    state["progress"] = "0"
                    state["assigned"] = (d.configuredOptions["assignOnGameStart"] as? DeviceOptionValue.BooleanValue)?.value.toString()
                    state["completed"] = "false"
                }
                "barrier" -> state["enabled"] = ((d.configuredOptions["enabledDuringPhase"] as? DeviceOptionValue.EnumValue)?.value == "Always").toString()
                "button" -> state["enabled"] = (d.configuredOptions["enabledAtGameStart"] as? DeviceOptionValue.BooleanValue)?.value.toString()
                "trigger", "creature-manager" -> state["enabled"] = (d.configuredOptions["enabledOnGameStart"] as? DeviceOptionValue.BooleanValue)?.value.toString()
                "elimination-manager" -> state["enabled"] = (d.configuredOptions["enabledAtGameStart"] as? DeviceOptionValue.BooleanValue)?.value.toString()
                "hud-message" -> state["visible"] = "false"
            }
            d.id to state.toMap()
        }
    }

    private fun record(kind: Kind, id: String, detail: String, actor: String, value: String) {
        if(mutableTrace.size < 1200) mutableTrace += Entry(kind,id,detail,actor,value)
    }
    private fun stop(id: String) {
        halted = true; queue.clear()
        record(Kind.WARNING,id,"Possible event loop or action limit. Reset to test again.","system","")
    }
    private fun add(work: Work) {
        val id = when(work) { is Work.Inject -> work.id; is Work.Receive -> work.id }
        val depth = when(work) { is Work.Inject -> work.depth; is Work.Receive -> work.depth }
        if(depth > maxDepth || queue.size >= maxPending) { stop(id); return }
        queue.addLast(work)
    }
    private fun set(id: String, key: String, newValue: String, actor: String) {
        if (mutableValues[id]?.get(key) == newValue) return
        mutableValues = mutableValues + (id to (mutableValues.getValue(id) + (key to newValue)))
        record(Kind.VALUE,id,"$key = $newValue",actor,newValue)
    }
    private fun emit(id: String, eventId: String, actor: String, value: String, depth: Int) {
        record(Kind.EVENT,id,eventId,actor,value)
        bindingIndex[id to eventId].orEmpty().forEach { link ->
            add(Work.Receive(link.targetDeviceId,link.targetFunctionId.value,actor,value,depth+1))
        }
    }
    /** Run exactly one queued event/function. Safe from recursive stack growth. */
    fun step(): Boolean {
        if(halted || queue.isEmpty()) return false
        val work = queue.removeFirst()
        val id = when(work) { is Work.Inject -> work.id; is Work.Receive -> work.id }
        if(processed++ >= maxActions) { stop(id); return false }
        when(work) {
            is Work.Inject -> {
                record(Kind.INPUT, work.id, "Manually injected ${work.event}; game conditions not checked", work.actor, work.value)
                emit(work.id,work.event,work.actor,work.value,work.depth)
            }
            is Work.Receive -> receive(work)
        }
        return true
    }
    fun run() { while(step()) { /* Bounded by maxActions, maxDepth and maxPending. */ } }

    private fun receive(work: Work.Receive) {
        val d = devices.getValue(work.id)
        val fn = work.function
        record(Kind.FUNCTION,d.id,fn,work.actor,work.value)
        set(d.id,"lastFunction",fn,work.actor)
        set(d.id,"lastInputValue",work.value,work.actor)
        // Legacy IDs retain narrow value semantics to let new reference devices connect to them.
        when (d.deviceTypeId) {
            "tracker" -> {
                when(fn) {
                    "assign" -> set(d.id,"assigned","true",work.actor)
                    "remove" -> set(d.id,"assigned","false",work.actor)
                    "resetProgress" -> { set(d.id,"progress","0",work.actor); set(d.id,"completed","false",work.actor) }
                    "incrementProgress", "complete" -> {
                        val current = mutableValues.getValue(d.id)
                        if(current["assigned"] != "true" || current["completed"] == "true") {
                            record(Kind.UNSUPPORTED,d.id,"Tracker not assigned or already completed; no progress change",work.actor,work.value)
                            return
                        }
                        val target = (d.configuredOptions["targetValue"] as DeviceOptionValue.IntegerValue).value
                        val n = if(fn == "complete") target else (current["progress"]?.toIntOrNull() ?: 0)+1
                        set(d.id,"progress",n.toString(),work.actor)
                        if (n >= target && current["completed"] != "true") {
                            set(d.id,"completed","true",work.actor)
                            emit(d.id,"onComplete",work.actor,n.toString(),work.depth)
                        }
                    }
                    else -> unsupported(work)
                }
                return
            }
            "hud-message" -> {
                when(fn) {
                    "show" -> set(d.id,"visible","true",work.actor)
                    "hide" -> set(d.id,"visible","false",work.actor)
                    else -> unsupported(work)
                }; return
            }
            "trigger" -> {
                if(fn == "trigger") { emit(d.id,"onTriggered",work.actor,work.value,work.depth);return }
                if(fn == "resetTimesTriggered") { set(d.id,"triggerCount","0",work.actor);return }
            }
        }
        // Bounded reference-level operations, never physical/world state.
        if(d.deviceTypeId == "channel" && fn == "transmit_when_receiving_from") {
            set(d.id,"relayRequested","true",work.actor)
            emit(d.id,"on_received_transmit",work.actor,work.value,work.depth)
            return
        }
        if(d.deviceTypeId == "timer") {
            when(fn) {
                "start_when_receiving_from" -> { set(d.id,"timerState","started (virtual countdown not modeled)",work.actor); return }
                "pause_when_receiving_from" -> { set(d.id,"timerState","paused",work.actor); return }
                "reset_when_receiving_from" -> { set(d.id,"timerState","reset",work.actor); return }
                "complete_when_receiving_from" -> {
                    set(d.id,"timerState","manually completed",work.actor)
                    emit(d.id,"on_success_send_event_to",work.actor,work.value,work.depth)
                    return
                }
            }
        }
        if(d.deviceTypeId == "teleporter" && fn == "teleport_when_receiving_from") {
            set(d.id,"teleportRequested",work.actor,work.actor)
            record(Kind.UNSUPPORTED,d.id,"Teleport requested. Destination/arrival is not modeled; inject On Teleported after verifying success.",work.actor,work.value)
            return
        }
        if(d.deviceTypeId == "end-game" && fn == "activate_when_receiving_from") {
            set(d.id,"endGameRequested",work.actor,work.actor)
            record(Kind.UNSUPPORTED,d.id,"End Game requested; actual win/team/round results are not modeled.",work.actor,work.value)
            return
        }
        // Generic literal on/off operations; everything else remains explicitly UNSUPPORTED.
        val normalized = fn.lowercase().replace("_when_receiving_from","").replace("_", "")
        when(normalized) {
            "enable" -> set(d.id,"enabled","true",work.actor)
            "disable" -> set(d.id,"enabled","false",work.actor)
            "turnon" -> changeSwitch(d, true,work)
            "turnoff" -> changeSwitch(d, false,work)
            "togglestate", "toggle" -> {
                val old = mutableValues[d.id]?.get("on")
                if(old == null) record(Kind.UNSUPPORTED,d.id,"Toggle requires an explicit initial On/Off state. Use Turn On/Off first.",work.actor,work.value)
                else changeSwitch(d,old != "true",work)
            }
            else -> unsupported(work)
        }
    }
    private fun changeSwitch(d: CreativeDeviceInstance, on: Boolean, work: Work.Receive) {
        val previous = mutableValues[d.id]?.get("on")
        set(d.id,"on",on.toString(),work.actor)
        if(d.deviceTypeId == "switch" && previous != on.toString()) {
            emit(d.id,if(on) "on_turned_on_send_event_to" else "on_turned_off_send_event_to",work.actor,work.value,work.depth)
            emit(d.id,"on_state_changes_send_event_to",work.actor,work.value,work.depth)
        }
    }
    private fun unsupported(work: Work.Receive) {
        record(Kind.UNSUPPORTED,work.id,"Function ${work.function} received, but its gameplay/value effect is not modeled. Inject its resulting event manually if appropriate.",work.actor,work.value)
    }
}
