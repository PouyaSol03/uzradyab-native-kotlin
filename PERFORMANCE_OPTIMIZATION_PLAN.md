# UI & Performance Optimization Plan (Future Task)

## Objective
Optimize Compose UI performance, reduce lag during navigation (e.g., when launching the app or opening screens like `DeviceManagementPanel` and `EventsReportScreen`), and eliminate unnecessary recompositions across the project.

## Current Bottlenecks Identified
1. **Heavy Lists/Maps Recomposition:** Frequent changes in underlying view models trigger full list recompositions. (Partially mitigated by `ImmutableListWrapper` but requires a broader pass).
2. **Heavy Screen Transitions:** Loading APIs, heavy calculations, and map markers immediately on navigation blocks the UI thread and drops frames during the screen transition animation.
3. **Map Marker Overload:** Loading too many positions/markers synchronously on the map can stutter the UI.

## Implementation Steps

### 1. Enforce Compose Stability
- Audit all data classes used in UI states (`UiState` models) and ensure they are either strictly `@Immutable` or wrapped in stable structures.
- Move away from passing raw `List` or `Map` collections into Composable functions, using `kotlinx.collections.immutable.PersistentList` or our `ImmutableListWrapper` everywhere.

### 2. Defer Heavy Work on Navigation
- Use `androidx.lifecycle.compose.LifecycleResumeEffect` or `LaunchedEffect(Unit)` with `delay` to defer heavy API calls until *after* the screen transition animation completes.
- Alternatively, load a placeholder/shimmer UI instantly and fetch data in the background without blocking the UI thread.

### 3. Map Rendering Optimizations
- Introduce marker clustering if not already present.
- Cache map markers in a stable state structure to prevent the map library from redrawing unaffected markers when only a single device's state changes.
- Move position processing (such as location decoding and filtering) entirely to `Dispatchers.Default` before updating the UI state.

### 4. Optimize Specific Screens
- **DeviceManagementPanel:** Defer heavy tab initializations, load tabs lazily using `HorizontalPager` or simple conditional renders rather than initializing all tab content at once.
- **EventsReportScreen:** Introduce pagination or a virtualized list for events instead of loading thousands of events synchronously. Add a shimmer loading state.
- **HomeMapScreen:** Ensure bottom sheets and device selection dialogs do not recompose the underlying map when opened/closed by extracting them out of the main map's composition scope (e.g., use `derivedStateOf`).

## Verification
- Measure screen transition times using Android Studio Profiler (JankStats).
- Ensure no skipped frames during `HomeMapScreen` -> `EventsReportScreen` navigation.
- Verify Layout Inspector shows 'Skipped' for unaffected UI components during state changes.
