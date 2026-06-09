# Move All Windows To Display

An IntelliJ Platform plugin that relocates **every open project window** (and
detached tool windows) to a display you pick from a visual map of your monitor
layout — in a single keystroke.

It scratches a specific macOS itch: when you dock or undock a laptop, the OS
collapses all your IDE windows onto the built-in screen, and IntelliJ has no
built-in "move everything to that monitor" command. This adds one.

## Features

- Visual picker that mirrors your System Settings display arrangement — just
  click the screen you want.
- Moves all open project windows at once, plus floating/detached tool windows.
- Preserves each window's relative position on its new screen.
- Bound to a keyboard shortcut (default <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>Shift</kbd>+<kbd>M</kbd>).

## Requirements

- An IntelliJ-based IDE, version 2025.1 or newer (build 251+).
- Built against JDK 21.

## Installation

### From a release build

1. Download the plugin `.zip` from the [Releases](../../releases) page.
2. In your IDE: **Settings → Plugins → ⚙ → Install Plugin from Disk…**, choose
   the zip, and restart.

### From source

```bash
git clone https://github.com/<username>/move-windows-plugin.git
cd move-windows-plugin
./gradlew buildPlugin
```

The installable zip is written to `build/distributions/`.

## Usage

1. Connect your second display.
2. Trigger **Window → Move All Windows To Display…**, or press
   <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>Shift</kbd>+<kbd>M</kbd>.
3. Click the target screen in the picker. Every window moves there.

Rebind the shortcut under **Settings → Keymap** (search for
"Move All Windows To Display").

## Development

Launch a sandbox IDE with the plugin loaded:

```bash
./gradlew runIde
```

Build the distributable:

```bash
./gradlew buildPlugin
```

## How it works

IntelliJ is a Swing application, so the plugin asks the platform for each
project's `JFrame` via `WindowManager`, enumerates displays through
`GraphicsEnvironment`, and repositions each window with AWT. Because macOS lays
all displays out in one virtual coordinate space — the same arrangement you set
in System Settings — moving a window across screens is just a coordinate
translation.

## License

[MIT](LICENSE) <!-- or your license of choice -->