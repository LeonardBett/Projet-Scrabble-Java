# scrabble-java

## Bash autocompletion for launch options

You can enable tab-completion for CLI options such as `--help`, `--super`, `--players`, `--lang`, `--ai-time`, and `--ai-ml`.

### Recommended workflow

After each new clone (on any machine), run this once from the repository root:

```bash
source scripts/setup-completion.sh
```

This command:
- activates completion immediately in the current terminal;
- installs persistence in `~/.bashrc` for future terminals.

After `git pull`, run the same command only if completion behavior changed or if your current shell was not reloaded.

At first launch (`./scrabble` or `./run.sh`), the launcher can still auto-install persistence and points to the same setup command for immediate activation.

Note: a script started with `./scrabble` cannot directly modify the parent shell state. For immediate activation in the current terminal, use `source scripts/setup-completion.sh`.

### Quick check

1. Ensure completion is installed and loaded:

```bash
source scripts/setup-completion.sh
```

2. Then type and complete options with `TAB`:

```bash
./run.sh --<TAB>
./scrabble --<TAB>
./run.sh -p <TAB>     # suggests: 2 3 4
./scrabble -p <TAB>   # suggests: 2 3 4
./run.sh -l <TAB>     # suggests: en fr
./run.sh -ai-time <TAB>
```

### Super Scrabble mode

Use `-s` or `--super` to start a game on a `21x21` board. The standard mode remains `15x15`.

```bash
./scrabble --super
./run.sh -s
```

### GUI Keyboard Shortcuts

If you are using the Graphical User Interface (GUI), you can easily customize your keyboard shortcuts at any time during the game.

Simply press **`CTRL + ,`** (Control + Comma) to open the shortcut configuration menu.

### Machine Learning AI Setup

To use the Machine Learning option for the artificial intelligence player (e.g., via the `--ai-ml` flag), you must first train the neural network model.

**Prerequisite:** You must have **Python** installed on your system.

Run the following script from the root of the repository to start the training process:

```bash
./train_ml.sh
````
## Network Multiplayer

The game features a Client-Server architecture for online multiplayer.

### Hosting a Server
You can start the server directly from the command line:

```bash
./scrabble --server [PORT]
````

Headless Mode (Daemon): Add the --daemon option to run the server invisibly, without a graphical interface or interactive terminal. This is ideal for deploying the game on a remote dedicated server.

Graphical Mode (GUI): You can also start the network server directly from the graphical interface (launched via -g or --gui), which includes a comprehensive menu to manage lobbies and connected players.

### Joining a Server
To join an existing server, use the Network menu in the GUI, or type the join IP[:PORT] command in the CLI.

The game includes an automatic discovery system (UDP broadcast) to instantly detect and list available servers on your local network.

### Playing over the Internet (WAN)
The current network architecture is optimized for Local Area Networks (LAN), using UDP broadcast for automatic server discovery. To play over the Internet, manual configuration is required:
1. **No Automatic Discovery:** Internet routers block UDP broadcast packets by default. 
2. **Port Forwarding:** The player hosting the server must configure their home router to forward the TCP port (ex: `12345`) to their local machine.
3. **Manual Connection:** Clients must manually join the game by entering the host's public IP address instead of relying on the server list.