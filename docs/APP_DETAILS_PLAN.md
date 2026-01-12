# App Details Page - Master Design & Implementation Plan

## 1. Executive Summary
The **App Details** page is the core analytical interface of AppsAnalyzer. It acts as a deep inspection tool for developers and power users, revealing the internal structure, security posture, and technological composition of any installed Android application. The design philosophy prioritizes **information density** without clutter, using progressive disclosure (tabs, expandable lists) to manage complexity.

## 2. User Experience (UX) & Interface (UI) Design

### 2.1. Navigation & Transition
- **Entry**: Hero animation on the App Icon when transitioning from the main list. The background color should subtly shift to match the dominant color of the app icon (using `Palette` API).
- **Structure**: A `Scaffold` with a `LargeTopAppBar` that collapses into a standard `TopAppBar` on scroll.
- **Exit**: Swipe-back gesture and a prominent back button.

### 2.2. Visual Hierarchy & Style (Material 3)
- **Header (Hero Section)**:
  - **Icon**: 96dp x 96dp, elevated, with a subtle shadow.
  - **Title**: App Name (Headline Medium), max 2 lines, truncated with ellipsis.
  - **Subtitle**: Package Name (Body Medium, Monospace font for readability), clickable to copy.
  - **Chips Row**: Horizontal scrollable row of `AssistChip` or `FilterChip`:
    - Version: `v2.4.1 (Build 102)`
    - SDK: `Target: 34` | `Min: 24`
    - Tech: `Flutter` | `React Native` | `Native` (Color-coded: Flutter=Blue, RN=Cyan, Native=Green)
    - Arch: `64-bit` | `Split APK`

- **Content Area**:
  - **Tabs**: `ScrollableTabRow` with sticky headers.
    1.  **Overview**: General metadata, signatures, stores.
    2.  **Analysis**: Deep tech stack analysis, libraries.
    3.  **Components**: Manifest components (Activities, etc.).
    4.  **Permissions**: Requested permissions with safety levels.
    5.  **Resources**: Assets, strings, colors (Future).

### 2.3. Interaction Patterns
- **Copy-to-Clipboard**: **All** technical text fields (paths, hashes, package names) must be tap-to-copy. Provide immediate feedback via a small `Toast` or `Snackbar`.
- **Search**: A Floating Action Button (FAB) or Top Bar Action to search *within* the current tab (essential for finding specific permissions or classes).
- **External Actions**:
  - **Open App**: Primary action.
  - **App Info**: Deep link to Android Settings.
  - **Play Store**: Deep link to store listing.
  - **Export APK**: Save base.apk to user storage.

## 3. Detailed Information Architecture

### 3.1. Tab 1: Overview
*Focus: Identity & Metadata*
- **Installation Info**:
  - **Installer**: Google Play, Amazon App Store, Package Installer, or ADB.
  - **First Installed**: Date & Time (Locale formatted).
  - **Last Updated**: Date & Time.
  - **UID**: Linux User ID.
- **File System**:
  - **APK Size**: Formatted (e.g., "45.2 MB").
  - **APK Path**: `/data/app/.../base.apk` (Truncate middle, tap to expand).
  - **Data Path**: `/data/user/0/...`.
  - **Split APKs**: List of splits if present (e.g., `config.arm64_v8a`, `config.xxhdpi`).
- **Signatures (Security)**:
  - **Certificate Fingerprints**: MD5, SHA-1, SHA-256 (Monospace, copyable).
  - **Signing Algorithm**: e.g., `SHA256withRSA`.
  - **Issuer**: Common Name (CN), Organization (O).
  - **Validity**: Start and End dates (highlight if expired).

### 3.2. Tab 2: Analysis (Tech Stack)
*Focus: How it was built*
- **Framework Detection**:
  - **Primary Framework**: Flutter, React Native, Xamarin, Unity, Cordova, Ionic, Native.
  - **Evidence**: "Detected via `libflutter.so` in `lib/arm64`".
- **Native Libraries (JNI)**:
  - List of all `.so` files found in the APK.
  - Grouped by Architecture (`arm64-v8a`, `armeabi-v7a`, `x86_64`).
  - Size of each library.
- **Language Features**:
  - **Kotlin**: Detect `kotlin_module` files or specific metadata.
  - **Compose**: Detect Compose-specific classes or libraries.

### 3.3. Tab 3: Components
*Focus: Android Manifest Entries*
- **Grouped Lists** (Collapsible):
  - **Activities**: Entry points, UI screens.
    - *Flags*: `LAUNCHER`, `EXPORTED`, `ALIAS`.
  - **Services**: Background jobs.
    - *Flags*: `FOREGROUND`, `ISOLATED`.
  - **Receivers**: Event listeners.
  - **Providers**: Data sharing.
- **Detail View**: Tapping a component shows its Intent Filters (Actions, Categories, Data Schemes).

### 3.4. Tab 4: Permissions
*Focus: Privacy & Security*
- **Categorization**:
  - **Dangerous**: Runtime permissions (Location, Camera, Mic) - **Red Icon**.
  - **Normal**: Install-time permissions (Internet, Vibrate) - **Grey Icon**.
  - **Signature/System**: System-level permissions - **Yellow Icon**.
- **Status**:
  - Show "Granted" or "Denied" (if accessible via `checkPermission` APIs, otherwise just "Requested").
  - Description: Short localized description of what the permission allows.

## 4. Technical Implementation Strategy

### 4.1. Domain Layer
- **Models**:
  - `AppDetail`: Root object.
  - `AppSignature`: Wrapper for certificate info.
  - `AppComponent`: Polymorphic class for Activity/Service/etc. with flags.
  - `AppPermission`: Name, protection level, description.
- **UseCase**: `GetAppDetailsUseCase`
  - Input: `packageName`
  - Output: `Flow<Resource<AppDetail>>` (Loading, Success, Error).
  - Logic: Orchestrates data fetching from `PackageManager`, `AppAnalyzer`, and `SignatureUtils`.

### 4.2. Data Layer
- **Sources**:
  - `PackageManager`: Primary source. Use `GET_META_DATA`, `GET_SIGNING_CERTIFICATES`, `GET_PERMISSIONS`, `GET_PROVIDERS`, etc.
  - `ZipFile`: To inspect APK contents for libraries (`lib/`) and framework files (e.g., `libflutter.so`).
  - `PackageInfo`: Access `splitNames`, `applicationInfo.sourceDir`.
- **Optimization**:
  - **Lazy Loading**: Do not calculate SHA-256 or unzip APKs on the main thread.
  - **Caching**: Cache heavy calculations (hashes) in a memory cache or Room database if necessary.

### 4.3. UI Layer (Compose)
- **ViewModel**: `AppDetailsViewModel`
  - State: `AppDetailsState` (isLoading, data: AppDetail?, error: String?, currentTab: Int).
  - Effects: `ShowToast`, `OpenUrl`.
- **Composables**:
  - `AppDetailsScreen`: Main scaffold.
  - `DetailHeader`: Collapsing toolbar.
  - `InfoCard`: Card container for sections.
  - `PermissionItem`: Row with icon and status.
  - `ComponentTree`: Recursive or indented list for components.

## 5. Development Roadmap
1.  **Phase 1: Skeleton & Navigation** - Setup route, basic UI structure, passing package name.
2.  **Phase 2: Basic Metadata** - Fetch and display name, version, icons, and dates.
3.  **Phase 3: Tech Stack Analysis** - Implement `ZipFile` inspection for `.so` files and framework detection.
4.  **Phase 4: Components & Permissions** - Parse `PackageInfo` arrays and display lists.
5.  **Phase 5: Polish** - Add Hero transitions, copy interactions, and color theming.
