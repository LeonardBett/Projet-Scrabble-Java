# scrabble-java

Scrabble U-Bordeaux is a Java 21 project that implements the Scrabble game in CLI and GUI, with local multiplayer, network multiplayer, AI opponents, save/load, contest mode, and language selection.

## Prerequisites

- JDK 21
- Maven
- Bash for the helper scripts and completion setup
- Python if you want to train the machine learning model used by `--ai-ml`

## Build And Run

The repository ships with a wrapper script that builds and launches the application:

```bash
./scrabble
```

Examples:

```bash
./scrabble --gui
./scrabble -l fr
./scrabble --super --blitz -t 20
```

Notes:

- `./scrabble` builds the project with Maven (without tests) before launching it.
- Command-line options are forwarded directly to the application, including `-v` and `--verbose`.
- A single positional argument is treated as a save file to load at startup.

## Maven Test Modes

The project defines two ways to run tests with Maven:

1. Quick tests (default profile):

```bash
mvn clean package
```

This runs the default `quick-tests` profile (faster feedback, excludes heavy GUI/network suites).

2. Full test suite (all tests):

```bash
mvn clean -Pfull-tests package
```

Use this command when you want exhaustive validation before merging/releasing.

Note: Maven profiles use `-P` (uppercase), not `-p`.



## Main Features

- Standard Scrabble board in 15x15 mode.
- Super Scrabble mode in 21x21 with `-s` or `--super`.
- CLI and GUI launch modes.
- Two built-in languages: `en` and `fr`.
- Save and load from the CLI, GUI, or a startup save file.
- Blitz mode with a per-player time limit.
- AI players with configurable thinking time.
- Expectiminimax and machine learning options for the AI.
- Local network multiplayer with host/join discovery.
- Contest mode that solves a loaded position and prints the best move.

## Super Scrabble Mode

Use `-s` or `--super` to start a game on a 21x21 board. The standard mode remains 15x15.

```bash
./scrabble --super
./scrabble -s
```

## Command-Line Options

The application entry point is `fr.ubordeaux.scrabble.App`.

| Option | Effect |
| --- | --- |
| `-h`, `--help` | Display the help text. |
| `-V`, `--version` | Display the program version. |
| `--list-languages` | Print the supported language codes. |
| `-g`, `--gui` | Launch the JavaFX GUI. |
| `-s`, `--super` | Enable Super Scrabble mode (`21x21`). |
| `-p N`, `--players N` | Set the number of players to 2, 3, or 4. |
| `-b`, `--blitz` | Enable blitz mode. |
| `-t TIME`, `--time TIME` | Set the blitz time limit in minutes. Ignored if blitz is not enabled. |
| `-l LANG`, `--lang LANG`, `--language LANG` | Set the dictionary language. |
| `-D FILE`, `--dictionary FILE` | Use a custom dictionary file. |
| `-a COLOR`, `--ai COLOR` | Add an AI player for the given color. The option can be repeated. |
| `-ai-time TIME`, `--ai-time TIME` | Set the AI thinking time in seconds. |
| `-ai-exptiminimax`, `--ai-exptiminimax` | Enable the expectiminimax search mode. |
| `--ai-ml` | Enable machine learning for word search. |
| `-c FILE`, `--contest FILE` | Load a saved game and print the best contest move. |
| `-S PORT`, `--server PORT` | Start the network server on the given port. |
| `--daemon` | Start the server headlessly. |
| `-v`, `--verbose` | Enable verbose application logging. |
| `-d`, `--debug` | Enable debug logging. |
| `SAVE_FILE` | Load a saved game at startup. |

Behavior details that matter in practice:

- `--time` is only meaningful with `--blitz`.
- If no language is passed, the app uses `LC_ALL` or `LANG`, then falls back to `en`.
- `-a` defaults to `RED` when no color is provided.
- `-p` accepts only 2, 3, or 4 players.

### Playing a Joker in CLI

When entering a move in the CLI, you must differentiate between regular tiles and Joker (blank) tiles:
- Type regular tiles in **uppercase**.
- Type the letter representing the Joker in **lowercase**.

For example, to play the word "DOG" using a Joker for the letter 'G', you would type:
`DOg`

## CLI Commands

Once a game is running in the terminal, the shell accepts these commands:

| Command | Effect |
| --- | --- |
| `help` | Show the full CLI help. |
| `help CMD` | Show help for a specific command. |
| `quit` | Quit the game, optionally saving first if there are unsaved changes. |
| `load FILE` | Load a saved game and restart from it. |
| `save FILE` | Save the current game. |
| `pause` | Pause or resume the blitz clock. |
| `hint` | Ask the AI for a hint. |
| `undo [N]` | Undo one move or `N` moves. |
| `redo [N]` | Redo one move or `N` moves. |
| `show board` | Refresh the board display. |
| `show history` | Show move history. |
| `show time` | Show remaining time for each player. |
| `show configuration` | Print the current configuration. |
| `set PARAM=VALUE` | Update runtime configuration. Multiple assignments can be separated with `;`. |
| `network` | Open the CLI network lobby. |
| `exchange LETTERS` | Exchange tiles from the rack. |
| `pass` | Pass the turn. |
| move notation | Any unrecognized input is parsed as a play move if possible. |

Implementation note: the in-session `new` command is exposed by help text, but the current shell returns an error for it while a session is already running.

`set` supports runtime updates for:

- language
- players
- super scrabble
- blitz
- blitz timeout
- AI time
- expectiminimax
- machine learning
- dictionary path
- debug
- verbose

## GUI

The GUI is implemented with JavaFX.

- Launch it with `-g` or `--gui`.
- A save file passed as the single positional argument is loaded at startup.
- The main window includes game actions, save/load, multiplayer access, and configuration display.
- The configuration dialog exists, but the editor itself is not fully implemented yet.
- You can open the shortcut configuration menu with Ctrl + ,.

### GUI Keyboard Shortcuts

If you are using the Graphical User Interface, you can customize your keyboard shortcuts at any time during the game by pressing Ctrl + ,.

## AI And ML

AI play is configured from the command line or from the runtime configuration.

- Use `-a COLOR` to assign AI players by color.
- Use `-ai-time` to control the AI search budget.
- Use `-ai-exptiminimax` to enable expectiminimax.
- Use `--ai-ml` to enable the TensorFlow-based model.

Before using the machine learning mode, train the models:

```bash
./train_ml.sh
```

This script expects Python to be available and generates the model files used by the ML agent.

## Network Multiplayer

The network stack uses a client/server architecture with local discovery.

### Hosting a Server

Start a server from the command line:

```bash
./scrabble -S 12345
```

Add `--daemon` to run the server in headless mode:

```bash
./scrabble -S 12345 --daemon
```

You can also reach the network lobby from the GUI, where the multiplayer menu lets you manage the same host flow.

### Joining a Server

In the CLI, `network` opens the lobby. From there, you can:

- join one of the automatically discovered servers on the local network
- connect manually by IP and port
- accept or decline invitations
- disconnect from the server

The GUI exposes the same lobby behavior, with additional screens for server status, player details, and invitations.

### Playing Over The Internet

The current implementation is oriented toward LAN play because automatic discovery relies on local network broadcast.

For a WAN game, the host must expose the server port and clients must join manually with the public IP address.

### Practical Notes

- The lobby can show discovered servers and host-side status information.
- The server can be started without launching the game itself when `--daemon` is used.
- The client-side CLI and GUI both rely on the same network manager underneath.

## Save And Load

The game supports three save/load paths:

- startup loading via a positional save file argument
- `load FILE` and `save FILE` in the CLI shell
- the GUI save/load actions

Saved games can be reopened later from the same application version and implementation format.

## Bash Completion

Tab completion is available for the launch options such as `--help`, `--super`, `--players`, `--lang`, `--ai-time`, and `--ai-ml`.

Recommended workflow after a fresh clone:

1. Activate completion in the current shell and persist it in `~/.bashrc`:

```bash
source scripts/setup-completion.sh
```

2. Then use TAB on the launcher options:

```bash
./scrabble --<TAB>
./scrabble -p <TAB>
./scrabble -l <TAB>
./scrabble -ai-time <TAB>
```

At first launch, the launcher can also propose installing completion persistence automatically. For immediate activation in the current terminal, use `source scripts/setup-completion.sh`.

## Implementation Notes

- The default language is derived from the environment when possible.
- The dictionaries used by the standard game are loaded from the bundled resources.
- Contest mode loads a saved game, computes playable words, and prints the best scoring move or `pass` if no move exists.
- `./scrabble` is the recommended launcher script and forwards options to `fr.ubordeaux.scrabble.App`.
- The GUI configuration editor is a placeholder in the current implementation.