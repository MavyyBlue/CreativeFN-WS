# Passive overlay boundary

Not implemented or declared in the Phase 0 manifest. No overlay permission is
requested on launch. The current application has no network or game access permissions.

Phase 8 uses a manually started TYPE_APPLICATION_OVERLAY window and foreground
service where required. Request Draw Over Other Apps only after explicit selection
of overlay mode, explain its purpose, and recheck permission before adding a window.
Provide a persistent notification with an immediately effective Stop Overlay action.
Review current API 37 foreground-service policy when implementing the service.

Bubble touches stay within the window bounds. Mini graph and guide panels support
drag, resize, collapse, expand, opacity, lock and orientation changes. Persist
position, size, current step and completion. Clamp restored windows into current
screen bounds. The overlay displays only a manually selected saved mechanic.
Load it into an immutable compact model; no simulation, continuous graph animation,
network requests, database polling or idle recomposition loops while backgrounded.

Never inject, hook, inspect process memory/files/network traffic, automate controls
or taps, use AccessibilityService, capture screens, OCR gameplay, infer live game
state, modify Fortnite, act as a macro or provide competitive gameplay assistance.
The companion never communicates with the Fortnite client. Real-phone landscape
interaction and stop behavior are required acceptance evidence for Phase 8.
