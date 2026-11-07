# Full-app Glass Dock Preview R2 — softer backdrop (2026-11-08)

User HONOR screenshot of R1: "Check ECDIS License" was still clearly legible through the glass behind Today/Work/Handover/Tasks, competing with navigation typography. Keep the refraction geometry and beautiful 1.30x outward selected capsule; soften captured source only.

## Change
- Original preview branch `feature/liquid-glass-dock-preview` based on stable `0.4.59`. R1 preserved as `backup/dockpreview-r1-before-soft-blur` and release tag `dockpreview-v0.4.59-r1`.
- `NavLiquidDockOptics.bar.blurDp` 2.0 → **5.0dp**, `selection.blurDp` 1.5 → **4.0dp**. `lens()` rim height/displacement unchanged (bar 12/36dp; selection 10/18dp).
- Tests assert optics, still require 1.30x selection and unchanged four-tab order. No change to UI/source capture/dock geometry/Business logic.
- Signed preview build **same isolated package** `com.navtasks.app.glasspreview`, versionCode 82, versionName 0.4.59-dock-preview2; not an update of stable NavTasks.
- Confirm on HONOR: text under dock now soft enough not to compete with legible tab captions; borders still bend and scroll does not shimmer. Also test pale cards on light theme and proportions at both dock ends.
- Update production only after user accepts. This R2 is still a test preview.
