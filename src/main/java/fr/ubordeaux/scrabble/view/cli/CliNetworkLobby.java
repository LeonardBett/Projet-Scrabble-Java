package fr.ubordeaux.scrabble.view.cli;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

/** CLI-based network lobby for hosting and joining online games. */
public class CliNetworkLobby {

  private final NetworkManager networkManager;
  private final CliView cliView;
  private final CliInputHandler inputHandler;
  private final CliNetworkBridge bridge;

  /**
   * Creates a new CliNetworkLobby.
   *
   * @param networkManager the network manager facade
   * @param cliView the CLI view
   * @param inputHandler the input handler
   */
  public CliNetworkLobby(
      NetworkManager networkManager, CliView cliView, CliInputHandler inputHandler) {
    this.networkManager = networkManager;
    this.cliView = cliView;
    this.inputHandler = inputHandler;
    this.bridge = new CliNetworkBridge(networkManager, cliView);
  }

  /** Displays the network lobby menu. */
  public void showMenu() {
    networkManager.addObserver(bridge);
    networkManager.startOnlinePlay();

    try {
      while (true) {
        printMenu();

        System.out.print("\n>> ");
        String raw = inputHandler.askAction().trim();
        String normalized = raw.toLowerCase(Locale.ROOT);

        if (normalized.isEmpty()) {
          continue;
        }

        if ("1".equals(normalized) || "host".equals(normalized)
            || "server".equals(normalized)) {
          hostServer(null);
          continue;
        }

        if (normalized.startsWith("host ")) {
          Integer port = parseTrailingInt(raw);
          if (port == null) {
            cliView.displayError(I18n.translate("cli.network.portNotNumeric"));
            continue;
          }
          hostServer(port);
          continue;
        }

        if ("2".equals(normalized) || "join".equals(normalized)) {
          joinServer();
          continue;
        }

        if (normalized.startsWith("join ")) {
          if (tryDirectJoin(raw)) {
            continue;
          }
          joinServer();
          continue;
        }

        if ("players".equals(normalized)) {
          networkManager.players();
          continue;
        }

        if (normalized.startsWith("player ")) {
          handlePlayerInfo(raw);
          continue;
        }

        if ("scoreboard".equals(normalized)) {
          networkManager.scoreboard();
          continue;
        }

        if ("status".equals(normalized)) {
          networkManager.serverStatus();
          continue;
        }

        if ("ping".equals(normalized)) {
          networkManager.ping();
          continue;
        }

        if (normalized.startsWith("new ")) {
          handleNewInvitation(raw);
          continue;
        }

        if ("accept".equals(normalized)) {
          networkManager.accept();
          continue;
        }

        if ("decline".equals(normalized)) {
          networkManager.decline();
          continue;
        }

        if ("cancel".equals(normalized)) {
          networkManager.cancel();
          continue;
        }

        if ("away".equals(normalized)) {
          networkManager.away();
          continue;
        }

        if ("backstatus".equals(normalized) || "back-status".equals(normalized)) {
          networkManager.back();
          continue;
        }

        if ("pass".equals(normalized)) {
          networkManager.pass();
          continue;
        }

        if (normalized.startsWith("exchange ")) {
          String letters = raw.substring("exchange".length()).trim();
          if (letters.isEmpty()) {
            cliView.displayError(I18n.translate("cli.network.exchangeUsage"));
            continue;
          }
          networkManager.exchange(letters);
          continue;
        }

        if (normalized.startsWith("play ")) {
          handlePlay(raw);
          continue;
        }

        if ("disconnect".equals(normalized)) {
          networkManager.quit();
          cliView.displayMessage(I18n.translate("cli.network.disconnected"));
          continue;
        }

        if ("help".equals(normalized)) {
          cliView.displayMessage(I18n.translate("cli.network.help"));
          continue;
        }

        if (normalized.startsWith("show ")) {
          handleShowCommand(normalized);
          continue;
        }

        if ("hint".equals(normalized) || "undo".equals(normalized)
            || "redo".equals(normalized) || normalized.startsWith("set ")
            || normalized.startsWith("save ")
            || normalized.startsWith("load ") || "pause".equals(normalized)) {
          cliView.displayError(I18n.translate("cli.network.commandNotSupported", normalized));
          continue;
        }

        if (tryCliPlayNotation(raw)) {
          continue;
        }

        if ("3".equals(normalized) || "back".equals(normalized)
            || "quit".equals(normalized) || "exit".equals(normalized)) {
          networkManager.stopOnlinePlay();
          return;
        }

        cliView.displayError(I18n.translate("cli.action.invalidChoice"));
      }
    } finally {
      networkManager.removeObserver(bridge);
    }
  }

  private void printMenu() {
    cliView.displayMessage("\n=== " + I18n.translate("cli.network.title") + " ===");
    cliView.displayMessage("1. " + I18n.translate("cli.network.hostServer"));
    cliView.displayMessage("2. " + I18n.translate("cli.network.joinServer"));
    cliView.displayMessage("3. " + I18n.translate("cli.network.back"));
    cliView.displayMessage(I18n.translate("cli.network.helpHint"));
  }

  private void hostServer(Integer presetPort) {
    int port = presetPort == null ? NetworkManager.DEFAULT_TCP_PORT : presetPort;

    if (presetPort == null) {
      cliView.displayMessage("\n" + I18n.translate("cli.network.hostPrompt"));
      System.out.print(I18n.translate("cli.network.portPrompt"));

      String portInput = inputHandler.askAction().trim();
      if (!portInput.isEmpty()) {
        Integer parsedPort = parseTrailingInt(portInput);
        if (parsedPort == null) {
          cliView.displayError(I18n.translate("cli.network.portNotNumeric"));
          return;
        }
        port = parsedPort;
      }
    }

    if (port < 1 || port > 65535) {
      cliView.displayError(I18n.translate("cli.network.invalidPort"));
      return;
    }

    if (networkManager.serverStart(port)) {
      cliView.displayMessage(I18n.translate("cli.network.serverStartedSuccess", port));
      networkManager.join(NetworkManager.DEFAULT_ADDRESS, port);
      cliView.displayMessage(I18n.translate("cli.network.hostAutoJoined", port));
      cliView.displayMessage(I18n.translate("cli.network.serverHostTip", port));
    } else {
      cliView.displayError(I18n.translate("cli.network.serverStartFailed"));
    }
  }

  private void joinServer() {
    cliView.displayMessage("\n" + I18n.translate("cli.network.discoveringServers"));

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    List<ServerInfo> servers = networkManager.serverList();
    if (servers.isEmpty()) {
      cliView.displayMessage(I18n.translate("cli.network.noServersFound"));
      return;
    }

    cliView.displayMessage(I18n.translate("cli.network.availableServers"));
    for (int i = 0; i < servers.size(); i++) {
      ServerInfo server = servers.get(i);
      cliView.displayMessage(
          String.format("%d. %s (%s:%d)", i + 1, server.getName(),
              server.getIp(), server.getPort()));
    }

    System.out.print("\n" + I18n.translate("cli.network.selectServer"));
    String rawChoice = inputHandler.askAction().trim();
    Integer selectedIndex = parseTrailingInt(rawChoice);

    if (selectedIndex == null) {
      cliView.displayError(I18n.translate("cli.network.invalidInput"));
      return;
    }

    int index = selectedIndex - 1;
    if (index < 0 || index >= servers.size()) {
      cliView.displayError(I18n.translate("cli.network.invalidSelection"));
      return;
    }

    ServerInfo selected = servers.get(index);
    networkManager.join(selected.getIp(), selected.getPort());
    cliView.displayMessage(I18n.translate("cli.network.joinedServer"));
  }

  private boolean tryDirectJoin(String raw) {
    String[] tokens = raw.trim().split("\\s+");
    if (tokens.length < 3) {
      return false;
    }

    String address = tokens[1];
    try {
      int port = Integer.parseInt(tokens[2]);
      networkManager.join(address, port);
      cliView.displayMessage(I18n.translate("cli.network.joinedServer"));
      return true;
    } catch (NumberFormatException e) {
      cliView.displayError(I18n.translate("cli.network.portNotNumeric"));
      return true;
    }
  }

  private void handlePlayerInfo(String raw) {
    Integer id = parseTrailingInt(raw);
    if (id == null || id <= 0) {
      cliView.displayError(I18n.translate("cli.network.playerUsage"));
      return;
    }
    networkManager.playersPlayerId(id);
  }

  private void handleNewInvitation(String raw) {
    String[] tokens = raw.trim().split("\\s+");
    if (tokens.length < 2 || tokens.length > 4) {
      cliView.displayError(I18n.translate("cli.network.newUsage"));
      return;
    }

    try {
      int id1 = Integer.parseInt(tokens[1]);
      if (tokens.length == 2) {
        networkManager.newPlayerId(id1);
        return;
      }

      int id2 = Integer.parseInt(tokens[2]);
      if (tokens.length == 3) {
        networkManager.newPlayerId(id1, id2);
        return;
      }

      int id3 = Integer.parseInt(tokens[3]);
      networkManager.newPlayerId(id1, id2, id3);
    } catch (NumberFormatException e) {
      cliView.displayError(I18n.translate("cli.network.newUsage"));
    }
  }

  private void handlePlay(String raw) {
    String payload = raw.substring("play".length()).trim();
    String[] parts = payload.split("\\s+");
    if (parts.length < 2) {
      cliView.displayError(I18n.translate("cli.network.playUsage"));
      return;
    }

    String posDir = parts[0];
    if (posDir.length() < 3) {
      cliView.displayError(I18n.translate("cli.network.playUsage"));
      return;
    }

    char dirChar = Character.toUpperCase(posDir.charAt(posDir.length() - 1));
    String direction = dirChar == 'V' ? "V" : "H";
    String pos = posDir.substring(0, posDir.length() - 1).toLowerCase(Locale.ROOT);

    int x;
    int y;
    if (pos.matches("[a-o]\\d+")) {
      y = pos.charAt(0) - 'a' + 1;
      x = Integer.parseInt(pos.substring(1));
    } else if (pos.matches("\\d+[a-o]")) {
      y = pos.charAt(pos.length() - 1) - 'a' + 1;
      x = Integer.parseInt(pos.substring(0, pos.length() - 1));
    } else {
      cliView.displayError(I18n.translate("cli.network.playUsage"));
      return;
    }

    String word = String.join("", java.util.Arrays.copyOfRange(parts, 1, parts.length)).toUpperCase(
        Locale.ROOT);
    networkManager.play(x, y, direction, word);
  }

  private boolean tryCliPlayNotation(String raw) {
    String[] parts = raw.trim().split("\\s+");
    if (parts.length < 2) {
      return false;
    }

    String posDir = parts[0];
    if (posDir.length() < 3) {
      return false;
    }

    char dirChar = Character.toUpperCase(posDir.charAt(posDir.length() - 1));
    if (dirChar != 'H' && dirChar != 'G' && dirChar != 'V') {
      return false;
    }

    String pos = posDir.substring(0, posDir.length() - 1).toLowerCase(Locale.ROOT);
    int x;
    int y;

    try {
      if (pos.matches("[a-o]\\d+")) {
        y = pos.charAt(0) - 'a' + 1;
        x = Integer.parseInt(pos.substring(1));
      } else if (pos.matches("\\d+[a-o]")) {
        y = pos.charAt(pos.length() - 1) - 'a' + 1;
        x = Integer.parseInt(pos.substring(0, pos.length() - 1));
      } else {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }

    String direction = dirChar == 'V' ? "V" : "H";
    String word = String.join("", java.util.Arrays.copyOfRange(parts, 1, parts.length)).toUpperCase(
        Locale.ROOT);
    networkManager.play(x, y, direction, word);
    return true;
  }

  private Integer parseTrailingInt(String value) {
    String[] tokens = value.trim().split("\\s+");
    if (tokens.length == 0) {
      return null;
    }

    String maybeNumber = tokens[tokens.length - 1];
    try {
      return Integer.parseInt(maybeNumber);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private void handleShowCommand(String normalizedCommand) {
    String[] tokens = normalizedCommand.split("\\s+");
    if (tokens.length < 2) {
      cliView.displayError(I18n.translate("cli.shell.show.usage"));
      return;
    }

    Game game = networkManager.getLocalGame();
    if (game == null) {
      cliView.displayError(I18n.translate("cli.network.noLocalGame"));
      return;
    }

    String target = tokens[1];
    switch (target) {
      case "board":
        new CliView(game).refresh();
        return;
      case "history":
        showHistory(game);
        return;
      case "time":
        showTime(game);
        return;
      case "configuration":
        showConfiguration(game);
        return;
      default:
        cliView.displayError(I18n.translate("cli.shell.show.unknownTarget", target));
    }
  }

  private void showHistory(Game game) {
    Stack<Move> history = game.getUndoRedo().getHistory();
    if (history.isEmpty()) {
      cliView.displayMessage(I18n.translate("cli.shell.history.empty"));
      return;
    }

    List<String> lines = new ArrayList<>();
    int index = 1;
    for (Move move : history) {
      lines.add(index + ". " + formatMove(move));
      index++;
    }
    cliView.displayMessage(String.join(System.lineSeparator(), lines));
  }

  private void showTime(Game game) {
    List<Player> players = game.getPlayers();
    if (players.isEmpty()) {
      cliView.displayMessage(I18n.translate("cli.shell.players.empty"));
      return;
    }

    List<String> lines = new ArrayList<>();
    for (Player p : players) {
      lines.add(p.getName() + ": " + p.getRemainingTimeDisplay());
    }
    cliView.displayMessage(String.join(System.lineSeparator(), lines));
  }

  private void showConfiguration(Game game) {
    String config = I18n.translate(
        "cli.shell.config",
        game.getLanguage(),
        game.getPlayers().size(),
        game.isBlitzModeEnabled(),
        0,
        false,
        false,
        GameLogger.isDebug(),
        GameLogger.isVerbose());
    cliView.displayMessage(config);
  }

  private String formatMove(Move move) {
    String playerName = move.getPlayer() == null ? "?" : move.getPlayer().getName();
    switch (move.getType()) {
      case PASS:
        return playerName + " " + I18n.translate("cli.shell.history.pass");
      case EXCHANGE:
        return playerName + " " + I18n.translate("cli.shell.history.exchange") + " "
            + tilesToText(move);
      case PLAY:
        Point start = move.getStartPosition();
        String coord = start == null ? "?" : toCoord(start);
        String direction = move.getDirection() == null
            ? "?"
            : (move.getDirection().name().startsWith("H") ? "h" : "v");
        return playerName + " " + coord + direction + " " + tilesToText(move);
      default:
        return playerName + " <unknown move>";
    }
  }

  private String tilesToText(Move move) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < move.getTiles().size(); i++) {
      sb.append(move.getTiles().get(i).getCharacter());
    }
    return sb.toString();
  }

  private String toCoord(Point p) {
    char row = (char) ('a' + p.getY());
    int col = p.getX() + 1;
    return "" + row + col;
  }
}
