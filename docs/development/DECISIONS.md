# NavTasks — Durable Decisions

- Preserve the existing Android Kotlin/Jetpack Compose application. Make minimal, reviewable changes; do not recreate the project.
- User data, profiles, schedules, recurring task semantics, task completion/history, settings and exchange formats must remain compatible.
- Use Apple Human Interface principles as design guidance: hierarchy, readable typography, consistent spacing, subtle surfaces, day/night contrast and restrained Liquid Glass. Android platform limitations still apply.
- Widgets use Android RemoteViews, not arbitrary Compose/Haze backdrop blur; do not claim native Liquid Glass for home-screen widgets.
- A widget task title opens the task; it must not silently complete it. A simple task's separate control can toggle completion; checklist tasks open in-app.
- AMOS/Services cards in Today appear on the actual scheduled due date only. Early reminders and Soon/Overdue screens are separate; never surface a future AMOS due card early because an early notification is configured.
- AMOS Today card stays compact, links directly to the work order; editing/completion happens in Works. Services with an open work-order reference must not be duplicated in Today (prior user decision; verify implementation when touching this logic).
- Notification small icon must be a high-contrast monochrome glyph recognizable as NavTasks, not a launcher glass image.
- For closing a work item on a different date, preserve the distinction between actual completion date and whether future occurrences retain or shift their schedule.
- No unverified APK may be described as device-tested. Build, unit tests and on-device checks are distinct milestones.
- Every completed stage must update docs/development files in GitHub alongside the implementation. Record exact commit IDs and CI links when available.

- 2026-11-07 approval: the notification should show the actual installed NavTasks color launcher icon (Android LargeIcon); small status-bar glyph is a monochrome translation of the existing layered-card brand, not a generic checkmark. Android OEM may suppress LargeIcon in some contexts.
- 2026-11-07 approval: Missed is neutral when zero, restrained red glass with red count in Today when positive; widgets show a red pill only with missed tasks and open the missed backlog on tap.
- 2026-11-07 approval: widget has localized date and progress; show pending task titles and adapt layout for width and height rather than stretching rows. Preserve task-click behavior.

- Widgets are Android RemoteViews. Use only its supported layout/view classes; do not put android.widget.Space or arbitrary Compose/custom views inside widget XML. Use layout padding or margins for gaps. Include a CI guard for known forbidden classes.

- 2026-11-07: version 0.4.48 is the first deliverable of this notification/widget update; v0.4.47 was automatically published then superseded due to unsupported widget XML. Do not recommend v0.4.47.

- 2026-11-07 user-approved clarification: Today Missed is completely absent (including its reserved horizontal gap) when backlog count is zero; the dates occupy that space. When count becomes positive, show restrained red glass with animated horizontal appearance; do not change data, overdue semantics, or the widget's existing zero-count behavior.

- 0.4.49 shipped on 2026-11-07: the Today missed chip has no zero state (hidden entirely), while the home widget missed badge retains its already-implemented zero-hide behavior. The date strip reclaims the width with an AnimatedVisibility horizontal transition.

- 2026-11-07 (0.4.50): equipment maintenance has two distinct record modes: planned (schedule, optional recurrence, reminders) and unplanned (AMOS requisition, no scheduled next due). This is different from a planned one-off with period=0. Existing JSON with no unplanned flag means Planned.
- Unplanned requests must not contribute to date-based Today/Calendar/Soon/Overdue or scheduled service notifications. Actual completion appears in history; request number is archived, and no next cycle is generated.
- Reuse current local record/profile storage. For an unplanned record nextDate is nullable; the existing lastDate is interpreted as the request creation date in that mode. Do not fabricate a next due date.
- New unplanned records require a manually entered AMOS requisition number; NavTasks does not create the request in external AMOS. The selective-template exchange removes original request numbers, leaving an unplanned draft that must have a new number before completion.
- Prevent changing service type when service completion history exists to avoid reinterpretation of archived records.

- v0.4.50 was signed and released 2026-11-07, source 0a3467be; previous release 0.4.49 remains available. GitHub CI is not physical-device acceptance.

- 2026-11-07 (0.4.51): “Дата заявки AMOS” for an unplanned service is manually selectable, defaulting to the current day for new entries. It is historical/reference metadata, not a due/completion date; must not create reminders, recurrence, scheduled Today/Calendar entries, Soon/Overdue status, or affect actual completion date. Reuse the existing Unplanned lastDate field; old profiles must remain compatible.

- 0.4.51 signed release 2026-11-07, source 651948cb0c280cd63a418d0054515f4d83102ea9: unplanned AMOS request date remains informational; it is not a deadline or the service-completion date. No change to profile JSON schema.

- 2026-11-07 (0.4.52 design): for standalone Services and AMOS pages, the scroll container must be edge-to-edge like Today. No navigationBarsPadding() on the full scrolling Column; the status-bar inset stays. Keep safe navigationBarsPadding() on the bottom-left floating Add control and 106dp internal LazyColumn trailing content padding for accessible card buttons. Do not introduce opaque reserved footer/bottomBar.

- 0.4.52 (2026-11-07): signed release ships edge-to-edge standalone Services and AMOS scroll containers. Maintain the decision not to re-add root navigationBarsPadding() when editing these pages; keep safe inset on floating Add buttons. Source d38aa26ddeb8ac3fe5f568dddb1c551463fc5a4d; physical HONOR verification pending.

- 2026-11-07 design approval from HONOR screenshot review: Plan month grid must not outline every date in bright blue. Use gentle opaque/translucent cells; emphasize Today by a restrained blue fill. Weekend labels/numerals get muted coral (informational weekend only, never an overdue state). Day numbers stay centered independent of any bottom-positioned semantic status dots; 16sp numerals. Keep calendar selection, statuses and month swipe unchanged.

- 0.4.53 release (2026-11-07): month cells rely on subtle fill instead of blue outline. Weekend is an informational warm-coral text accent; status-dot color retains independent meaning. Numeric position is fixed at geometric center and dot independently anchored to bottom. Keep these semantics in future calendar UI edits. Source 81cce3e29b74454f6329a124f9589570a9cb2722; no data or task logic changes.

- 2026-11-07 (0.4.54): Weekend contrast must apply consistently in both Plan month calendar and Today's 7-day strip. Reuse calendarIsWeekend(date) and one shared calendarWeekendInk(p) palette (dark #E5A4A0, day #B36563), including selected weekend days; selected Today day retains its existing blue glass backdrop. Do not treat a weekend as a missed task. Avoid altering the 7-day interaction/selection or any schedule semantics.

- 0.4.54 signed release (2026-11-07): Today weekend styling matches Plan's shared coral palette while the blue selected glass remains intact. No data/recurrence/calendar task logic changed. Source 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f; device validation still pending.

- 2026-11-07 (0.4.55 UI): Floating primary creation Add (+) is anchored at BottomEnd on Tasks, Services and AMOS, aligned conceptually with the Today bottom-right calendar shortcut. Horizontal inset 19dp, original vertical insets and navigationBarsPadding() preserved. Do not move dock or alter click semantics.

- 0.4.55 published on 2026-11-07 from 401564269c574271b1e3c22c356989fe2d94eb58; all floating Add (+) controls in Tasks, Services and AMOS consistently use right-side placement and retain safe insets. CI success does not replace real-device acceptance.

- 2026-11-07 (0.4.56): Handover floating Add (+) follows the same BottomEnd placement and 19dp right inset as Tasks, Services and AMOS. Retain 83dp bottom gap when Handover embedded in main tab dock, 18dp when standalone, platform navigation safe inset, and original Add action. This completes the intended consistent primary-creation placement.

- 0.4.56 signed release 2026-11-07: Handover Add is now right-aligned and has the same safe area and original section-creation action. Source 612ba1ba215e8c1871a654acda2304f5c44c678c. No schema or business changes; do not treat CI as visual-device acceptance.

- 2026-11-07 architecture audit (no implementation approval): Telegram's Liquid Glass uses API33+ RuntimeShader/RenderEffect for optical refraction while NavTasks uses genuine Haze 1.7.1 backdrop blur and a gradient-based active lens. Preserve the existing NavGlass pipeline by default; prefer low-risk stacked-opacity/material tuning before a new GPU shader. No copying Telegram GPL-2.0 implementation into NavTasks without license review. See GLASS_TECH_AUDIT.md.

- 2026-11-07 (0.4.57, experimental optics): User authorized progress toward genuinely refractive glass. Implement own small AGSL capsule-edge pixel displacement for the Haze backdrop on API33+, not Telegram GPL source. Keep Haze 1.7.1, the original four-tab click/swipe/navigation system, active selection spring, exact dock geometry, icon layers and data model unchanged. Retain API31-32 fallback; no shader on tab text/icons. This is an unverified visual experiment until HONOR acceptance.

- 2026-11-07 (0.4.57 released experimental): original, independently authored dock-only AGSL refraction on Android 13+, Haze fallback on API31–32, with foreground glyphs kept outside optical effect and existing Haze 1.7.1 retained. Main CI 42376895012 and publisher 42377117640 SUCCESS, signed source 3d2ffdeead8c607d2e6f7b43d126d18ae6593581. On-device optical quality still unverified; do not broaden to other surfaces before confirming performance.

- 2026-11-08 (0.4.58 urgent visual-regression correction): the **full-size glass material** must cover 56dp dock with no layout padding shrinking it. Apply 1dp horizontal/2dp vertical only to selection/interactive foreground. Keep effects of moving content restrained: 30dp navigation blur and a maximum 1.2dp optical bend across a 5dp edge; preserve API33 gate/Haze fallback. User-observed scrolling-text shimmer and apparent size shrink in 0.4.57 justify this scoped hotfix; physical acceptance still required.

- 0.4.58 signed hotfix published 2026-11-08 from 4118106265361b3e28b2f4636f5c6c17def6801c. Outer dock bounds 56dp and unchanged foreground inset are mandatory; narrow AGSL refraction/stronger Haze blur is experimental until device validation. Main CI and publisher passed; don't treat CI as proof of no flicker.

- 2026-11-08 (0.4.59): v0.4.57/0.4.58 shader+material experiments **failed HONOR visual acceptance** (moving text shimmer/jitter, no convincing glass). Restore the exact v0.4.56 NavGlass.kt rendering in a *higher-version* signed hotfix to keep app data and modern fixes. Leave experimental optics sources dormant for forensic analysis; do NOT call them in the dock. Stability takes priority over unmeasured refraction. Future visual experiments must separate stable blur capture from shader and have explicit revert/QA gates.

- 2026-11-08 v0.4.59 signed: rejected v0.4.57/0.4.58 AGSL rendering is disconnected from runtime. NavGlass.kt restored byte-for-byte from v0.4.56 for stability. CI 42383033196 and publisher 42383289163 SUCCESS; user HONOR acceptance pending. New physically refractive glass has NOT been achieved. Future work must use an isolated visual test before merging into product.

- 2026-11-08: pursue actual Liquid Glass through an **isolated side-by-side APK** first. Lab branch `experiments/liquid-glass-backdrop` uses Apache-2.0 Backdrop 1.0.6 LayerBackdrop to record original content, then samples/refracts it, with Glass ON/OFF toggle and Day/Night testing. Separate applicationId `com.navtasks.glasslab`; stable NavTasks v0.4.59 untouched. Never copy Telegram GPL-2.0 code. Do not merge effects into production until HONOR scrolling/text/frame-time checks pass. CI 42385989762 SUCCESS; physical testing pending.


- 2026-11-08 (Glass Lab 0.1.2): HONOR visual feedback accepts the new border-refraction quality of Lab 0.1.1. Replicate production NavGlassGlyph vector paths for four tabs exactly in the standalone app; use active blue #329CFF night/#087DFA day and production inactive glyph/caption colors. Animate one lens with stable spring, max 10dp horizontal stretch, then neutral matte-grey settled selection; keep 56dp backdrop geometry, real scene capture and Glass OFF fallback. Main application is unchanged until HONOR test of 0.1.2 confirms motion, no shimmer, smooth switching and geometry. CI 42389729911 SUCCESS; standalone https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.2.


- 2026-11-08 — user chose first increased selection lens width test of +28dp vs previous +10dp, exclusively in isolated Glass Lab 0.1.3. Keep height 52dp, outer dock 56dp, existing direct backdrop refraction and spring motion unchanged. Lab CI 42390942917 successful; physical HONOR review remains required before production integration. Source 9f3ac3c8574d539141dd828d76c9c08da28fbe6f; release glasslab-v0.1.3.


- 2026-11-08: The selected lens must **scale uniformly in X and Y**, not stretch only in width. On transition, a 1.30x experiment raises the 52dp selection to 67.6dp, visibly outside the 56dp outer bar, while the dock itself stays the same. Resting lens returns to 52dp and neutral grey. Keep the 0.1.1 direct Backdrop refraction, original icons/blue palette, springs, horizontal clamps, and unmoving icon sizes. Lab 0.1.4 CI 42392699540 SUCCESS; HONOR approval still pending. Stable main 0.4.59 is untouched.


- 2026-11-08: A uniformly scaled selection must not be nested under a backdrop parent that clips its children. Lab 0.1.5 renders backing, 1.30× lens and four-tab foreground as siblings, preserves actual refraction, and protects layout architecture in CI. First/last clipping and smoothness still require HONOR test. Do not change stable 0.4.59 app before acceptance.


- 2026-11-08 — Port HONOR-approved standalone Glass Lab 0.1.5 optics into a **separate full NavTasks preview**, NOT into stable production. Experimental branch `feature/liquid-glass-dock-preview`, original main source snapshot `backup/navtasks-0.4.59-before-liquid-glass`. Keep 56dp dock + 1.30x proportional outward-growing selection, LayerBackdrop capturing unblurred page only, four original glyphs and matte-grey settle; retain legacy Haze dock for older API and rollback. Full app test uses debug `applicationIdSuffix=".glasspreview"`, isolating all user data; CI 42397184202 passes. Only after HONOR approval progress to '+' / calendar / settings, never blanket-apply glass to all controls at once.


- 2026-11-08 — User requires NavTasks Liquid Glass dock to blur underlying **text slightly more** so names such as "Check ECDIS License" do not compete with navigation labels. Tune only pre-lens blur (outer bar 2→5dp, selected lens 1.5→4dp) in isolated full-app Preview R2. Preserve existing refraction displacement/height, 1.30x proportional grow, grey rest, original glyphs and fallback Haze. CI 42398375900 SUCCESS; physical HONOR acceptance outstanding. No main app changes.

- 2026-11-12: User approved pre-1.0.0 eight-item polish and full functional/Apple HIG audit. Keep accepted RC20/code188 installed, perform source changes only on isolated feature branch and archive exact baseline. Handover vessel entry headers may toggle collapsed content, text opens existing editor, Personal notes stay expanded; search retains occurrence counts and may reveal results automatically. Disabled task templates are only visually struck/sorted; enabled semantics and history remain unchanged. Color and highlighter selection are distinct. No stable production release until user separately approves.
- 2026-11-12: First presentation stage signed CI43211482395 passed in non-publishing mode. This is a build/test pass NOT physical HONOR acceptance. The compose-rich-editor RC14 selection handles issue remains open; do not silently upgrade a Kotlin/Compose-incompatible editor or replace persistent rich-text formatting. Future installable code strictly >189.
