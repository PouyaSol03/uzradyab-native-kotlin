# Project Instructions

## Figma Design Intake

- Use `figma-mcp-go` for Figma reads in this project.
- Do not use the official Figma MCP/API server unless the user explicitly asks for it.
- The expected setup is: Codex/VS Code is running, Figma Desktop is open, and the `figma-mcp-go` Figma plugin is running inside the target file.
- If a Figma tool returns `plugin not connected`, ask the user to restart the Figma plugin while the MCP server/session is active, then retry.
- For Figma URLs, convert `node-id=2498-23512` to colon format `2498:23512`.
- In `figma-mcp-go`, call `read_design_strategy` before reading unfamiliar frames.
- Prefer this read flow:
  1. `get_metadata`
  2. `get_pages`
  3. `get_design_context` with compact or minimal detail for the current selection/page
  4. `get_node`, `scan_text_nodes`, `scan_nodes_by_types`, `get_styles`, `get_variable_defs`, and `get_fonts` for the target frame
- `get_design_context` reads the current selection/page and does not take a `nodeId`; use `get_node` or scan tools when a specific frame node is provided.
- Use Figma frame data to build reusable Jetpack Compose components first, then screens.

## Android Compose Rules

- The app is Persian and RTL by default.
- Preserve RTL layout for each Figma frame; only force LTR for phone numbers, passwords, plate numbers, device IDs, coordinates, and technical codes.
- Build reusable UI components for repeated controls such as inputs, buttons, links, cards, and loading/error states.
- Keep API/backend boundaries separate from UI: screen state, events, ViewModel, repository, and network client should not be mixed into composables.
- Use the project typography and design tokens instead of hard-coded per-screen styling whenever possible.

## Build And Verification

- Do not run Gradle compile, assemble, install, or test tasks by default.
- The user will run builds/tests locally and send the result back when needed.
- Only run Gradle or other long verification commands when the user explicitly asks for it.
- Lightweight file reads, searches, diffs, and targeted static inspection are okay.

## Current Implementation Notes

- The React reference project is at `C:\Projects\uzradyab`; it is a heavily customized Traccar/Vite/MUI/Tailwind frontend.
- Use the React project as the behavior and styling reference, especially login, map, bottom navigation, device list, selected-device status card, reports, settings, OTP, payment, and notification flows.
- The main Traccar backend used by the React app is `https://app.uzradyab.ir`, with endpoints such as `/api/session`, `/api/devices`, `/api/positions`, `/api/reports/summary`, and WebSocket `/api/socket`.
- Secondary services from the React app are `https://pay.uzradyab.ir` for OTP/payment/custom Traccar helpers and `https://notification.uzradyab.ir` for notification preferences/latest events.
- Native sign-in now follows the React pattern: submit phone number as `email` plus `password` to `POST https://app.uzradyab.ir/api/session`.
- Successful native sign-in navigates from `/signin` to `/home`; sign-out calls `DELETE /api/session`, clears stored cookies, and navigates back to `/signin`.
- Native networking currently uses OkHttp with a persistent cookie jar, `SessionRepository`, `TraccarApiClient`, and ViewModels. Keep extending this separation instead of calling network APIs directly from composables.
- `/home` is currently a first data-backed shell: it loads current session, devices, and positions from `app.uzradyab.ir`, selects the first device, and shows a map-style summary card plus device list.
- Next likely steps are adding WebSocket `/api/socket`, a real map SDK implementation, selected-device bottom sheet parity with React `StatusCard`, and device/report/settings screens.
