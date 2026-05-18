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
