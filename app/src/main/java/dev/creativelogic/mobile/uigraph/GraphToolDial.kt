package dev.creativelogic.mobile.uigraph

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** View-only canvas controls. Does not mutate graph content or simulation state directly. */
@Composable
internal fun GraphToolDial(expanded: Boolean, inSimulation: Boolean, canFit: Boolean,
    onToggle: () -> Unit, onDevices: () -> Unit, onBindings: () -> Unit, onFit: () -> Unit,
    onSimulation: () -> Unit, modifier: Modifier = Modifier) {
    // Collapsed state occupies just its anchor: no invisible panel blocking graph gestures.
    BoxWithConstraints(modifier) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val compact = availableWidth < 420.dp || availableHeight < 190.dp
        // This is deliberately a fan, not a pop-up drawer. Small landscape windows
        // arrange the same actions in a short stacked fan to prevent cut-off.
        Box(Modifier.padding(8.dp).size(
            width = if (expanded && !inSimulation) (if (compact) 200.dp else 292.dp) else 82.dp,
            height = if (expanded && !inSimulation) (if (compact) 174.dp else 175.dp) else 48.dp,
        )) {
            if (expanded && !inSimulation) {
                if (compact) {
                    DialOption("Devices", onDevices, Modifier.offset(0.dp, 53.dp), "graph-devices")
                    DialOption("Bindings", onBindings, Modifier.offset(70.dp, 64.dp), "graph-bindings")
                    DialOption("Fit", onFit, Modifier.offset(27.dp, 112.dp), "graph-fit", enabled = canFit)
                    DialOption("Simulate", onSimulation, Modifier.offset(87.dp, 122.dp), "graph-simulation-toggle")
                } else {
                    DialOption("Devices", onDevices, Modifier.offset(4.dp, 59.dp), "graph-devices")
                    DialOption("Bindings", onBindings, Modifier.offset(43.dp, 112.dp), "graph-bindings")
                    DialOption("Fit", onFit, Modifier.offset(139.dp, 97.dp), "graph-fit", enabled = canFit)
                    DialOption("Simulate", onSimulation, Modifier.offset(193.dp, 35.dp), "graph-simulation-toggle")
                }
            }
            FilledTonalButton(onClick = if (inSimulation) onSimulation else onToggle,
                modifier = Modifier.align(Alignment.TopStart)
                    .testTag(if (inSimulation) "graph-simulation-toggle" else "graph-tools-toggle")
                    .heightIn(min = 44.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Text(if (inSimulation) "Design" else if (expanded) "Close" else "Tools",
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun DialOption(label: String, action: () -> Unit, modifier: Modifier, tag: String, enabled: Boolean = true) {
    FilledTonalButton(onClick = action, enabled = enabled, modifier = modifier.testTag(tag).heightIn(min = 42.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
