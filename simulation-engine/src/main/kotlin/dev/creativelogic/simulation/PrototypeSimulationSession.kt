package dev.creativelogic.simulation

import dev.creativelogic.catalog.DeviceCatalog
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import java.util.PriorityQueue

/**
 * Explicitly experimental, offline-only rules. These reproduce neither Fortnite's
 * internal scheduler nor unverified device defaults. No Android or wall clock input.
 * The UI does not expose this engine as a verified simulator.
 */
class PrototypeSimulationSession(
    val graph: MechanicGraph,
    private val catalog: DeviceCatalog = InitialDeviceCatalog.catalog,
    val limits: SimulationLimits = SimulationLimits(),
) {
    private sealed interface Action {
        data class ManualTrigger(val deviceId: String) : Action
        data class FunctionCall(val deviceId: String, val function: String) : Action
        data object GameStart : Action
        data class ButtonInteract(val deviceId: String) : Action
        data class PlayerElimination(val victimId: String, val eliminatorId: String) : Action
        data class CreatureElimination(val creatureType: String) : Action
    }

    private data class QueuedAction(
        val atMillis: Long,
        val sequence: Long,
        val depth: Int,
        val actorId: String,
        val action: Action,
    )

    private val deviceById: Map<String, CreativeDeviceInstance>
    private val bindings: Map<Pair<String, String>, List<LogicConnection>>
    private val queue = PriorityQueue<QueuedAction>(compareBy<QueuedAction> { it.atMillis }.thenBy { it.sequence })
    private var sequence: Long = 0
    private var processedTotal = 0
    private var processedInTick = 0
    private var lastTick = -1L
    private var mutableTrace = mutableListOf<SimulationTraceEntry>()
    private var mutableStates: Map<String, DeviceRuntimeState> = emptyMap()
    var gameStarted: Boolean = false
        private set

    var clockMillis: Long = 0
        private set
    var status: SimulationStatus = SimulationStatus.PAUSED
        private set
    var loopWarning: LoopWarning? = null
        private set

    val trace: List<SimulationTraceEntry> get() = mutableTrace.toList()
    val states: Map<String, DeviceRuntimeState> get() = mutableStates.toMap()
    val pendingEvents: Int get() = queue.size

    init {
        require(graph.devices.map { it.id }.distinct().size == graph.devices.size) { "Duplicate device IDs" }
        require(graph.connections.map { it.id }.distinct().size == graph.connections.size) { "Duplicate binding IDs" }
        deviceById = graph.devices.associateBy { it.id }
        graph.devices.forEach { device ->
            catalog.validate(device)
            when (device.deviceTypeId) {
                "trigger" -> Unit
                "tracker" -> {
                    require(device.optionEnum("sharing") == "All") { "Prototype tracker supports only Sharing = All; player/team tracking is not modeled" }
                    require(device.optionEnum("showOnHud") == "Off") { "HUD presentation is not implemented" }
                    require(device.optionInt("targetValue") >= 1) { "Prototype tracker target must be at least one" }
                }
                "barrier" -> require(device.optionEnum("enabledDuringPhase") in setOf("Always", "None")) {
                    "Prototype barrier supports only Always or None; game phases are not modeled"
                }
                "hud-message", "button", "round-settings", "creature-manager", "elimination-manager" -> Unit
                else -> throw IllegalArgumentException("Prototype runtime does not support ${device.deviceTypeId}")
            }
        }
        require(graph.connections.map { listOf(it.sourceDeviceId, it.sourceEventId.value, it.targetDeviceId, it.targetFunctionId.value) }.distinct().size == graph.connections.size) {
            "Duplicate semantic binding"
        }
        graph.connections.forEach { catalog.validateBinding(it, graph.devices) }
        // Connection UUIDs provide a stable order independent of input list iteration.
        bindings = graph.connections.sortedBy { it.id }.groupBy { it.sourceDeviceId to it.sourceEventId.value }
        reset()
    }

    /** Queue a builder action at virtual time. It does not run until step() or resume(). */
    fun trigger(deviceId: String, actorId: String = "manual", afterMillis: Long = 0) {
        require(status != SimulationStatus.POSSIBLE_EVENT_LOOP) { "Reset a halted session before adding inputs" }
        require(deviceById[deviceId]?.deviceTypeId == "trigger") { "Manual input requires an existing Trigger" }
        require(actorId.isNotBlank()) { "An actor ID is required" }
        require(afterMillis >= 0) { "A negative virtual delay is invalid" }
        enqueue(Math.addExact(clockMillis, afterMillis), 0, actorId, Action.ManualTrigger(deviceId))
    }

    /** External actions are events in a local, explicitly limited test world—not Fortnite state. */
    fun startGame() = externalAction("game-start", Action.GameStart)

    fun interactButton(deviceId: String, actorId: String = "player-1") {
        require(deviceById[deviceId]?.deviceTypeId == "button") { "Select a Button device" }
        externalAction(actorId, Action.ButtonInteract(deviceId))
    }

    fun playerEliminated(victimId: String = "player-2", eliminatorId: String = "player-1") {
        require(victimId.isNotBlank() && eliminatorId.isNotBlank())
        externalAction(eliminatorId, Action.PlayerElimination(victimId, eliminatorId))
    }

    fun creatureEliminated(creatureType: String = "Fiend", actorId: String = "player-1") {
        require(creatureType == "Fiend") { "Only the Fiend creature type is modeled" }
        externalAction(actorId, Action.CreatureElimination(creatureType))
    }

    private fun externalAction(actorId: String, action: Action) {
        require(status != SimulationStatus.POSSIBLE_EVENT_LOOP) { "Reset the halted session" }
        require(actorId.isNotBlank())
        enqueue(clockMillis, 0, actorId, action)
    }

    fun pause() { if (status != SimulationStatus.POSSIBLE_EVENT_LOOP) status = SimulationStatus.PAUSED }

    /** Run exactly one queued action even while paused. Returns false if no action ran. */
    fun step(): Boolean {
        if (status == SimulationStatus.POSSIBLE_EVENT_LOOP) return false
        val next = queue.peek() ?: return false
        val tickEvents = if (lastTick == next.atMillis) processedInTick else 0
        if (processedTotal >= limits.maxSessionActions || tickEvents >= limits.maxActionsPerTick || next.depth > limits.maxPropagationDepth) {
            val offending = actionDeviceId(next.action)
            halt(offending, next.actorId, next.atMillis)
            return false
        }
        queue.remove()
        clockMillis = next.atMillis
        if (lastTick != clockMillis) { lastTick = clockMillis; processedInTick = 0 }
        processedInTick++
        processedTotal++
        when (val action = next.action) {
            is Action.ManualTrigger -> fireTrigger(action.deviceId, next.actorId, next.depth, manual = true)
            is Action.FunctionCall -> invoke(action.deviceId, action.function, next.actorId, next.depth)
            Action.GameStart -> beginGame(next.actorId, next.depth)
            is Action.ButtonInteract -> interact(action.deviceId, next.actorId, next.depth)
            is Action.PlayerElimination -> eliminatePlayer(action.victimId, action.eliminatorId, next.depth)
            is Action.CreatureElimination -> eliminateCreature(action.creatureType, next.actorId, next.depth)
        }
        return true
    }

    /** Runs until idle, paused by a safety limit, or explicitly paused by caller. */
    fun resume(): SimulationStatus {
        if (status == SimulationStatus.POSSIBLE_EVENT_LOOP) return status
        status = SimulationStatus.READY
        while (status == SimulationStatus.READY && queue.isNotEmpty()) {
            if (!step()) break
        }
        return status
    }

    /** Set the virtual time while idle; cannot skip pending queued actions. */
    fun advanceTo(millis: Long) {
        require(queue.isEmpty()) { "Process queued actions before advancing time" }
        require(status != SimulationStatus.POSSIBLE_EVENT_LOOP) { "Reset a halted session" }
        require(millis >= clockMillis) { "Virtual time cannot go backwards" }
        clockMillis = millis
    }

    fun reset() {
        queue.clear()
        mutableTrace.clear()
        mutableStates = graph.devices.associate { device ->
            val state: DeviceRuntimeState = when (device.deviceTypeId) {
                "trigger" -> TriggerRuntime(enabled = device.optionBoolean("enabledOnGameStart"))
                "tracker" -> TrackerRuntime(assigned = device.optionBoolean("assignOnGameStart"))
                "barrier" -> BarrierRuntime(enabled = device.optionEnum("enabledDuringPhase") == "Always")
                "hud-message" -> HudMessageRuntime(visible = false)
                "button" -> ButtonRuntime(enabled = device.optionBoolean("enabledAtGameStart"))
                "round-settings" -> RoundSettingsRuntime(enabled = device.optionBoolean("enabledOnMinigameStart"))
                "creature-manager" -> CreatureManagerRuntime(enabled = device.optionBoolean("enabledOnGameStart"))
                "elimination-manager" -> EliminationManagerRuntime(enabled = device.optionBoolean("enabledAtGameStart"))
                else -> error("Validated constructor must reject this device")
            }
            device.id to state
        }
        clockMillis = 0
        sequence = 0
        processedTotal = 0
        processedInTick = 0
        lastTick = -1
        status = SimulationStatus.PAUSED
        gameStarted = false
        loopWarning = null
    }

    private fun enqueue(at: Long, depth: Int, actor: String, action: Action) {
        if (status == SimulationStatus.POSSIBLE_EVENT_LOOP) return
        if (queue.size >= limits.maxPendingActions) {
            halt(actionDeviceId(action), actor, at)
            return
        }
        queue.add(QueuedAction(at, sequence++, depth, actor, action))
    }

    private fun halt(deviceId: String, actor: String, at: Long) {
        if (status == SimulationStatus.POSSIBLE_EVENT_LOOP) return
        loopWarning = LoopWarning(
            reason = "POSSIBLE EVENT LOOP",
            deviceIds = (mutableTrace.takeLast(12).map { it.deviceId } + deviceId).distinct(),
            atMillis = at,
        )
        status = SimulationStatus.POSSIBLE_EVENT_LOOP
        record(at, TraceKind.WARNING, deviceId, "POSSIBLE EVENT LOOP", actor)
    }

    private fun actionDeviceId(action: Action) = when (action) {
        is Action.ManualTrigger -> action.deviceId
        is Action.FunctionCall -> action.deviceId
        Action.GameStart, is Action.PlayerElimination, is Action.CreatureElimination -> "scenario"
        is Action.ButtonInteract -> action.deviceId
    }

    private fun record(at: Long, kind: TraceKind, device: String, detail: String, actor: String) {
        mutableTrace.add(SimulationTraceEntry(at, mutableTrace.size.toLong(), kind, device, detail, actor))
    }

    private fun update(id: String, state: DeviceRuntimeState, actor: String, change: String) {
        if (mutableStates[id] == state) return
        mutableStates = mutableStates + (id to state)
        record(clockMillis, TraceKind.STATE_CHANGE, id, change, actor)
    }

    private fun emit(id: String, event: String, actor: String, depth: Int) {
        record(clockMillis, TraceKind.EVENT, id, event, actor)
        bindings[id to event].orEmpty().forEach { binding ->
            enqueue(clockMillis, depth + 1, actor, Action.FunctionCall(binding.targetDeviceId, binding.targetFunctionId.value))
        }
    }

    private fun beginGame(actor: String, depth: Int) {
        record(clockMillis, TraceKind.INPUT, "scenario", "Game Start (Round 1)", actor)
        if (gameStarted) {
            record(clockMillis, TraceKind.IGNORED, "scenario", "Game already started; Reset to restart", actor)
            return
        }
        gameStarted = true
        // The round-start event belongs to Round Settings, NOT to a fictional Game Start device.
        graph.devices.filter { it.deviceTypeId == "round-settings" }.forEach { device ->
            val state = mutableStates.getValue(device.id) as RoundSettingsRuntime
            if (state.enabled && device.optionEnum("round") in setOf("All", "1"))
                emit(device.id, "onRoundStart", actor, depth)
        }
        graph.devices.filter { it.deviceTypeId == "hud-message" && it.optionBoolean("showOnRoundStart") }.forEach { device ->
            enqueue(clockMillis, depth + 1, actor, Action.FunctionCall(device.id, "show"))
        }
    }

    private fun interact(id: String, actor: String, depth: Int) {
        record(clockMillis, TraceKind.INPUT, id, "Button interact", actor)
        val state = mutableStates.getValue(id) as ButtonRuntime
        val limit = deviceById.getValue(id).optionInt("timesCanTrigger")
        if (!state.enabled || (limit > 0 && state.interactions >= limit)) {
            record(clockMillis, TraceKind.IGNORED, id, "Button unavailable", actor)
            return
        }
        update(id, state.copy(interactions = state.interactions + 1,
            enabled = limit == 0 || state.interactions + 1 < limit), actor,
            "interactions=${state.interactions + 1}")
        emit(id, "onInteract", actor, depth)
    }

    private fun eliminatePlayer(victim: String, eliminator: String, depth: Int) {
        record(clockMillis, TraceKind.INPUT, "scenario", "Player Eliminated: $victim by $eliminator", eliminator)
        graph.devices.filter { it.deviceTypeId == "elimination-manager" }.forEach { device ->
            val enabled = (mutableStates.getValue(device.id) as EliminationManagerRuntime).enabled
            if (enabled && (victim != eliminator || device.optionBoolean("validOnSelfElimination"))) {
                emit(device.id, "onEliminated", victim, depth)
                if (victim != eliminator) emit(device.id, "onElimination", eliminator, depth)
            }
        }
    }

    private fun eliminateCreature(type: String, actor: String, depth: Int) {
        record(clockMillis, TraceKind.INPUT, "scenario", "Creature Eliminated: $type", actor)
        graph.devices.filter { it.deviceTypeId == "creature-manager" }.forEach { device ->
            val enabled = (mutableStates.getValue(device.id) as CreatureManagerRuntime).enabled
            if (enabled && device.optionEnum("creatureType") == type)
                emit(device.id, "onMatchingCreatureTypeEliminated", actor, depth)
        }
    }

    private fun fireTrigger(id: String, actor: String, depth: Int, manual: Boolean) {
        val config = deviceById.getValue(id)
        val state = mutableStates.getValue(id) as TriggerRuntime
        if (manual) record(clockMillis, TraceKind.INPUT, id, "manual trigger", actor)
        val limit = config.optionInt("timesCanTrigger")
        if ((manual && !config.optionBoolean("triggeredByPlayer")) || !state.enabled ||
            (limit > 0 && state.triggerCount >= limit) || clockMillis < state.nextAllowedAtMillis) {
            record(clockMillis, TraceKind.IGNORED, id, "Trigger unavailable", actor)
            return
        }
        val delay = config.optionDuration("resetDelay")
        val next = if (delay == 0L) clockMillis else Math.addExact(clockMillis, delay)
        update(id, state.copy(triggerCount = state.triggerCount + 1, nextAllowedAtMillis = next), actor, "triggerCount=${state.triggerCount + 1}")
        emit(id, "onTriggered", actor, depth)
    }

    private fun invoke(id: String, function: String, actor: String, depth: Int) {
        val config = deviceById.getValue(id)
        record(clockMillis, TraceKind.FUNCTION, id, function, actor)
        when (config.deviceTypeId) {
            "trigger" -> {
                val state = mutableStates.getValue(id) as TriggerRuntime
                when (function) {
                    "enable" -> update(id, state.copy(enabled = true), actor, "enabled=true")
                    "disable" -> update(id, state.copy(enabled = false), actor, "enabled=false")
                    "resetTimesTriggered" -> update(id, state.copy(triggerCount = 0, nextAllowedAtMillis = 0), actor, "triggerCount=0")
                    "trigger" -> fireTrigger(id, actor, depth, manual = false)
                    else -> error("Unknown Trigger function: $function")
                }
            }
            "tracker" -> {
                val state = mutableStates.getValue(id) as TrackerRuntime
                when (function) {
                    "assign" -> update(id, state.copy(assigned = true), actor, "assigned=true")
                    "remove" -> update(id, state.copy(assigned = false), actor, "assigned=false")
                    "resetProgress" -> update(id, state.copy(progress = 0, completed = false), actor, "progress=0;completed=false")
                    "incrementProgress", "complete" -> {
                        if (!state.assigned || state.completed) {
                            record(clockMillis, TraceKind.IGNORED, id, "Tracker unavailable", actor)
                            return
                        }
                        val progress = if (function == "complete") config.optionInt("targetValue") else state.progress + 1
                        val complete = progress >= config.optionInt("targetValue")
                        update(id, state.copy(progress = progress, completed = complete), actor, "progress=$progress;completed=$complete")
                        if (complete) emit(id, "onComplete", actor, depth)
                    }
                    else -> error("Unknown Tracker function: $function")
                }
            }
            "barrier" -> {
                val state = mutableStates.getValue(id) as BarrierRuntime
                when (function) {
                    "enable" -> update(id, state.copy(enabled = true), actor, "enabled=true")
                    "disable" -> update(id, state.copy(enabled = false), actor, "enabled=false")
                    else -> error("Unknown Barrier function: $function")
                }
            }
            "hud-message" -> {
                val state = mutableStates.getValue(id) as HudMessageRuntime
                when (function) {
                    "show" -> update(id, state.copy(visible = true), actor, "message shown: ${config.optionText("message")}")
                    "hide" -> update(id, state.copy(visible = false), actor, "message hidden")
                    else -> error("Unknown HUD Message function: $function")
                }
            }
            "button" -> {
                val state = mutableStates.getValue(id) as ButtonRuntime
                when (function) {
                    "enable" -> update(id, state.copy(enabled = true), actor, "enabled=true")
                    "disable" -> update(id, state.copy(enabled = false), actor, "enabled=false")
                    else -> error("Unknown Button function: $function")
                }
            }
            "creature-manager" -> {
                val state = mutableStates.getValue(id) as CreatureManagerRuntime
                when (function) {
                    "enable" -> update(id, state.copy(enabled = true), actor, "enabled=true")
                    "disable" -> update(id, state.copy(enabled = false), actor, "enabled=false")
                    else -> error("Unknown Creature Manager function: $function")
                }
            }
            "elimination-manager" -> {
                val state = mutableStates.getValue(id) as EliminationManagerRuntime
                when (function) {
                    "enable" -> update(id, state.copy(enabled = true), actor, "enabled=true")
                    "disable" -> update(id, state.copy(enabled = false), actor, "enabled=false")
                    else -> error("Unknown Elimination Manager function: $function")
                }
            }
            "round-settings" -> error("Round Settings subset has no callable functions")
            else -> error("Unsupported device escaped constructor validation")
        }
    }
}

private fun CreativeDeviceInstance.optionBoolean(key: String) = (configuredOptions.getValue(key) as DeviceOptionValue.BooleanValue).value
private fun CreativeDeviceInstance.optionInt(key: String) = (configuredOptions.getValue(key) as DeviceOptionValue.IntegerValue).value
private fun CreativeDeviceInstance.optionEnum(key: String) = (configuredOptions.getValue(key) as DeviceOptionValue.EnumValue).value
private fun CreativeDeviceInstance.optionDuration(key: String) = (configuredOptions.getValue(key) as DeviceOptionValue.DurationValue).milliseconds
private fun CreativeDeviceInstance.optionText(key: String) = (configuredOptions.getValue(key) as DeviceOptionValue.TextValue).value

data class SimulationLimits(
    val maxActionsPerTick: Int = 256,
    val maxPropagationDepth: Int = 64,
    val maxSessionActions: Int = 10_000,
    val maxPendingActions: Int = 4_096,
) {
    init { require(maxActionsPerTick > 0 && maxPropagationDepth > 0 && maxSessionActions > 0 && maxPendingActions > 0) }
}

enum class SimulationStatus { PAUSED, READY, POSSIBLE_EVENT_LOOP }
enum class TraceKind { INPUT, FUNCTION, EVENT, STATE_CHANGE, IGNORED, WARNING }

data class SimulationTraceEntry(
    val timeMillis: Long, val index: Long, val kind: TraceKind,
    val deviceId: String, val detail: String, val actorId: String,
)

data class LoopWarning(val reason: String, val deviceIds: List<String>, val atMillis: Long)

sealed interface DeviceRuntimeState
data class TriggerRuntime(val enabled: Boolean, val triggerCount: Int = 0, val nextAllowedAtMillis: Long = 0) : DeviceRuntimeState
data class TrackerRuntime(val assigned: Boolean, val progress: Int = 0, val completed: Boolean = false) : DeviceRuntimeState
data class BarrierRuntime(val enabled: Boolean) : DeviceRuntimeState

/** Scenario-only states; never stored in the mechanic document or Room. */
data class HudMessageRuntime(val visible: Boolean) : DeviceRuntimeState
data class ButtonRuntime(val enabled: Boolean, val interactions: Int = 0) : DeviceRuntimeState
data class RoundSettingsRuntime(val enabled: Boolean) : DeviceRuntimeState
data class CreatureManagerRuntime(val enabled: Boolean) : DeviceRuntimeState
data class EliminationManagerRuntime(val enabled: Boolean) : DeviceRuntimeState
