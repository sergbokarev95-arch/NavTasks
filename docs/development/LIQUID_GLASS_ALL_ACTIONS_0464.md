# NavTasks 0.4.64 — ONE in-place signed update: all primary round Liquid Glass controls

**User directive (2026-11-08):** avoid multiple downloads on weak internet; apply established Liquid Glass to all remaining relevant round/floating controls in one batch; change ONLY DAY white outline a little, NEVER shader/lens/animations.

## Frozen exact optical implementation
- Same approved bar optics as Glass Lab: vibrancy(); blur 2dp; lens rim 12dp/displacement 36dp; Highlight.Default alpha .85; shadow .22; identical 56dp dock and its moving capsule with selection blur1.5dp/10dp/18dp/1.30x scale.
- `NavLiquidDockOptics.kt`, `NavLiquidDockTabs.kt`, `NavGlass.kt`, static opaque gradient card tokens and business data stores are byte-identical to approved sources (CI SHA guards).
- Only day-mode outer border brush shared by bar and round controls changed from white dominant to slightly greyer blue (#9AAEC0 etc.) with same .75dp border. Night outer rim unchanged. Not a blur/opacity/refraction change.
- Reusable `NavLiquidGlassRoundButton` is the exact existing '+' material with glyph/color/size parameters (54dp floating, 44dp header). Same haptic/glassClickable and original action callback.
- Real original-content LayerBackdrop sources; all optical controls rendered as siblings OUTSIDE their capture layer to avoid feedback/self-refraction.
- Main root: Today Calendar, Tasks '+', top Settings; Plan Back; separate Profiles Back/Settings; Settings Back; Task Editor Back; Services/Handover/AMOS Back/Search/Add; standalone Works Back. Android 12/12L Haze fallback retained; inline text/form/checkbox controls remain normal for usability.
- Accepted Quick Profile pinch/background blur/Settings safe footer and all recurrence/Services/AMOS/Handover/profile logic unchanged.
- Signed same-ID Beta `com.navtasks.app.glass`, versionName 0.4.64-beta versionCode89, original betaStable key; ZIP direct GitHub Release. Stays on isolated feature branch; `main` code unchanged. Never uninstall installed app; export full profile before upgrade.
- **Recovery before installation:** signed same-ID, same-key Haze stable fallback with versionCode90 must be built so Android can install it over candidate89 without uninstall.
- HONOR acceptance required: light borders still visible but less white/merged on light cards, no changed optical refraction/selected lens/animation, round Back/Search/Create/Calendar/Settings render and work in both themes; normal tasks/AMOS/services/profile edits, scrolling, nav bar insets, rapid tab switches, profile pinch.
