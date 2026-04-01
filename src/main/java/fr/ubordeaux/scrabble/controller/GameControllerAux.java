package fr.ubordeaux.scrabble.controller;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.ai.MlAgent;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.savefiles.GameLoader;
import fr.ubordeaux.scrabble.model.savefiles.SaveManager;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.cli.CliInputHandler;
import fr.ubordeaux.scrabble.view.cli.CliNetworkLobby;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

/**
 * CLI-specific orchestration extracted from GameController to keep the main controller smaller.
 */
class GameControllerAux {
  private final GameController controller;
  private Game pendingLoadedGame;
  private boolean restartRequested;
  private boolean hasUnsavedChanges;

  GameControllerAux(GameController controller) {
    this.controller = controller;
  }

  void runCli() {
    CliView cliView = controller.requireCliView();
    CliInputHandler input = new CliInputHandler();

    try {
      MlAgent sharedMlAgent = initSharedMlAgentIfEnabled();
      configureExistingAiPlayers(sharedMlAgent);

      cliView.displayWelcome();
      ensureMinimumPlayers(input, cliView, sharedMlAgent);
      startCliGame(cliView);

      boolean running = true;
      while (running) {
        runCliLoop(input, controller.requireCliView(), controller.getOrLoadGaddag());
        if (restartRequested && pendingLoadedGame != null) {
          applyLoadedGame(sharedMlAgent);
          continue;
        }
        running = false;
      }

      displayWinner(controller.requireCliView());
    } finally {
      controller.stopBlitzWatcher();
      input.close();
    }
  }

  private MlAgent initSharedMlAgentIfEnabled() {
    if (!controller.isMlEnabled()) {
      return null;
    }

    List<String> dictList = controller.getOrLoadDictionaryList();
    String modelPath = "src/main/resources/ai/model_" + controller.configuredLanguage();
    MlAgent sharedMlAgent = new MlAgent(modelPath, dictList);

    final MlAgent finalAgent = sharedMlAgent;
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (finalAgent != null) {
        finalAgent.close();
      }
    }));

    return sharedMlAgent;
  }

  private void configureExistingAiPlayers(MlAgent sharedMlAgent) {
    for (Player p : controller.internalGame().getPlayers()) {
      if (p instanceof AiPlayer) {
        AiPlayer bot = (AiPlayer) p;
        bot.setExpectiminimaxMode(controller.isExpectiminimaxEnabled());
        if (sharedMlAgent != null) {
          bot.setMlAgent(sharedMlAgent);
        }
      }
    }
  }

  private void ensureMinimumPlayers(CliInputHandler input, CliView cliView, MlAgent sharedMlAgent) {
    if (controller.internalGame().getPlayers().size() >= 2) {
      return;
    }

    int num = controller.configuredPlayerCount() > 0
        ? controller.configuredPlayerCount()
        : input.askNumberOfPlayers();

    for (int i = 1; i <= num; i++) {
      String name = input.askPlayerName(i);
      PlayerColor assignedColor = PlayerColor.fromIndex(i - 1);

      if (name.toUpperCase().startsWith("IA") || name.toUpperCase().startsWith("AI")) {
        addConfiguredAiPlayer(name, assignedColor, sharedMlAgent, cliView);
      } else {
        controller.addPlayer(new HumanPlayer(name, assignedColor));
      }
    }
  }

  private void addConfiguredAiPlayer(
      String name,
      PlayerColor assignedColor,
      MlAgent sharedMlAgent,
      CliView cliView) {
    AiPlayer bot = new AiPlayer(name, 3, controller.configuredAiTime(), assignedColor);
    bot.setExpectiminimaxMode(controller.isExpectiminimaxEnabled());

    if (sharedMlAgent != null) {
      bot.setMlAgent(sharedMlAgent);
      cliView.displayMessage(I18n.translate(
          "cli.game.mlActivated",
          name,
          controller.configuredLanguage()));
    }

    controller.addPlayer(bot);
  }

  private void startCliGame(CliView cliView) {
    controller.startGame();
    hasUnsavedChanges = false;

    if (controller.internalGame().isBlitzModeEnabled()) {
      cliView.displayMessage(I18n.translate(
          "cli.game.blitzActivated",
          controller.internalGame().getPlayers().get(0).getRemainingTimeDisplay()));
      controller.startBlitzWatcher(cliView);
    }
  }

  private void runCliLoop(CliInputHandler input, CliView cliView, Gaddag currentGaddag) {
    boolean running = true;
    restartRequested = false;
    while (running && !controller.internalGame().isGameOver()) {
      Player current = controller.internalGame().getCurrentPlayer();

      if (isBlitzTimeElapsed(current, cliView, true)) {
        break;
      }

      if (current.isAutonomous()) {
        playAutonomousTurn(current, currentGaddag, cliView);
        continue;
      }

      running = handleHumanTurn(current, input, cliView);
      if (restartRequested) {
        break;
      }
    }
  }

  private void applyLoadedGame(MlAgent sharedMlAgent) {
    controller.stopBlitzWatcher();

    Game loaded = pendingLoadedGame;
    pendingLoadedGame = null;
    restartRequested = false;

    CliView loadedView = new CliView(loaded);
    loadedView.setBlitzMode(loaded.isBlitzModeEnabled());
    controller.adoptLoadedCliState(loaded, loadedView);
    configureExistingAiPlayers(sharedMlAgent);

    if (loaded.isBlitzModeEnabled()) {
      Player current = loaded.getCurrentPlayer();
      if (current != null && !current.isOutOfTime()) {
        current.startTurnTimer();
      }
      controller.startBlitzWatcher(loadedView);
    }

    loadedView.refresh();
    hasUnsavedChanges = false;
  }

  private boolean isBlitzTimeElapsed(Player current, CliView cliView, boolean showEndGameError) {
    Game game = controller.internalGame();
    if (!game.isBlitzModeEnabled() || current == null || !current.isOutOfTime()) {
      return false;
    }

    controller.handleBlitzExpiry(current, cliView);
    if (showEndGameError) {
      game.setGameOver(true);
      controller.internalView().displayError(I18n.translate("cli.game.timeUp", current.getName()));
    }
    return true;
  }

  private void playAutonomousTurn(Player player, Gaddag currentGaddag, CliView cliView) {
    cliView.displayMessage(I18n.translate("cli.game.aiTurn", player.getName()));
    try {
      player.playTurn(controller.internalGame(), currentGaddag);
      // AI moves bypass controller.handlePlayerMove(), so refresh explicitly on successful move.
      cliView.refresh();
      hasUnsavedChanges = true;
      Thread.sleep(2000);
    } catch (Exception e) {
      cliView.displayError(I18n.translate("cli.game.aiTurnError", e.getMessage()));
      controller.handlePlayerMove(Move.createPass(player));
      hasUnsavedChanges = true;
    }
  }

  private boolean handleHumanTurn(Player current, CliInputHandler input, CliView cliView) {
    String action = input.askAction();

    if (isBlitzTimeElapsed(current, cliView, false)) {
      return false;
    }

    return executeHumanAction(action, current, input, cliView);
  }

  private boolean executeHumanAction(
      String action,
      Player current,
      CliInputHandler input,
      CliView cliView) {
    String normalizedAction = action == null ? "" : action.trim();
    return executeShellCommand(normalizedAction, current, input, cliView);
  }

  private boolean executeShellCommand(
      String rawCommand,
      Player current,
      CliInputHandler input,
      CliView cliView) {
    if (rawCommand.isBlank()) {
      cliView.displayError(I18n.translate("cli.action.invalidChoice"));
      return true;
    }

    String[] tokens = rawCommand.split("\\s+");
    String command = tokens[0].toLowerCase(Locale.ROOT);

    switch (command) {
      case "help":
        displayHelp(cliView, tokens.length > 1 ? tokens[1].toLowerCase(Locale.ROOT) : null);
        return true;
      case "quit":
        return handleQuitCommand(input, cliView);
      case "hint":
        controller.provideHint();
        return true;
      case "pass":
        try {
          controller.handlePlayerMove(Move.createPass(current));
          hasUnsavedChanges = true;
          cliView.displayMessage(I18n.translate("cli.game.playerSkips", current.getName()));
        } catch (RuntimeException e) {
          cliView.displayError(e.getMessage());
        }
        return true;
      case "undo":
        applyUndoRedo(tokens, true, cliView);
        return true;
      case "redo":
        applyUndoRedo(tokens, false, cliView);
        return true;
      case "pause":
        pauseBlitzClock(current, cliView);
        return true;
      case "show":
        handleShow(tokens, cliView);
        return true;
      case "set":
        handleSet(tokens, cliView);
        return true;
      case "save":
        handleSave(tokens, cliView);
        return true;
      case "load":
        handleLoad(tokens, cliView);
        return true;
      case "network":
        handleNetwork(input, cliView);
        return true;
      case "new":
        cliView.displayError(I18n.translate("cli.shell.commandNotSupportedInSession"));
        return true;
      case "exchange": {
        String payload = rawCommand.length() > 8 ? rawCommand.substring(8).trim() : "";
        executeMoveAction(
            input.parseExchangeLetters(current, payload),
            cliView,
            I18n.translate("cli.game.lettersExchanged"));
        return true;
      }
      default: {
        Move inferredPlay = input.parsePlayMoveNotation(current, rawCommand);
        if (inferredPlay != null) {
          executeMoveAction(inferredPlay, cliView, I18n.translate("cli.game.moveDone"));
          return true;
        }
        cliView.displayError(I18n.translate("cli.action.invalidChoice"));
        return true;
      }
    }
  }

  private void applyUndoRedo(String[] tokens, boolean undo, CliView cliView) {
    int count = 1;
    if (tokens.length > 1) {
      try {
        count = Integer.parseInt(tokens[1]);
      } catch (NumberFormatException e) {
        cliView.displayError(I18n.translate("cli.shell.invalidCountArg"));
        return;
      }
    }

    if (count <= 0) {
      cliView.displayError(I18n.translate("cli.shell.countMustBePositive"));
      return;
    }

    for (int i = 0; i < count; i++) {
      if (undo) {
        controller.undo();
      } else {
        controller.redo();
      }
      hasUnsavedChanges = true;
    }
  }

  private void pauseBlitzClock(Player current, CliView cliView) {
    if (!controller.internalGame().isBlitzModeEnabled()) {
      cliView.displayError(I18n.translate("cli.shell.blitzNotEnabled"));
      return;
    }
    controller.togglePause();
  }

  private void handleShow(String[] tokens, CliView cliView) {
    if (tokens.length < 2) {
      cliView.displayError(I18n.translate("cli.shell.show.usage"));
      return;
    }

    String target = tokens[1].toLowerCase(Locale.ROOT);
    switch (target) {
      case "board":
        cliView.refresh();
        return;
      case "history":
        showHistory(cliView);
        return;
      case "time":
        showTime(cliView);
        return;
      case "configuration":
        showConfiguration(cliView);
        return;
      default:
        cliView.displayError(I18n.translate("cli.shell.show.unknownTarget", target));
    }
  }

  private void showHistory(CliView cliView) {
    Stack<Move> history = controller.internalGame().getUndoRedo().getHistory();
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

  private void showTime(CliView cliView) {
    List<Player> players = controller.internalGame().getPlayers();
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

  private void showConfiguration(CliView cliView) {
    Game game = controller.internalGame();
    String config = I18n.translate(
        "cli.shell.config",
        controller.configuredLanguage(),
        game.getPlayers().size(),
        game.isBlitzModeEnabled(),
        controller.configuredAiTime(),
        controller.isExpectiminimaxEnabled(),
        controller.isMlEnabled(),
        GameLogger.isDebug(),
        GameLogger.isVerbose());
    cliView.displayMessage(config);
  }

  private void handleSet(String[] tokens, CliView cliView) {
    String[] kv = parseSetArguments(tokens);
    if (kv == null) {
      cliView.displayError(I18n.translate("cli.shell.set.usage"));
      return;
    }

    String key = kv[0].trim().toLowerCase(Locale.ROOT);
    String value = kv.length > 1 ? kv[1].trim().toLowerCase(Locale.ROOT) : "";

    switch (key) {
      case "debug": {
        Boolean enabled = parseBoolean(value, cliView);
        if (enabled == null) {
          return;
        }
        GameLogger.setDebug(enabled);
        cliView.displayMessage(I18n.translate("cli.shell.set.updated", "debug", enabled));
        return;
      }
      case "verbose": {
        Boolean enabled = parseBoolean(value, cliView);
        if (enabled == null) {
          return;
        }
        GameLogger.setVerbose(enabled);
        cliView.displayMessage(I18n.translate("cli.shell.set.updated", "verbose", enabled));
        return;
      }
      default:
        cliView.displayError(I18n.translate("cli.shell.set.unsupportedParam", key));
    }
  }

  private String[] parseSetArguments(String[] tokens) {
    if (tokens.length < 2) {
      return null;
    }

    if (tokens.length >= 4 && "=".equals(tokens[2])) {
      return new String[] {tokens[1], tokens[3]};
    }

    if (tokens.length >= 3 && !tokens[1].contains("=")) {
      return new String[] {tokens[1], tokens[2]};
    }

    if (tokens[1].contains("=")) {
      return tokens[1].split("=", 2);
    }

    return null;
  }

  private Boolean parseBoolean(String value, CliView cliView) {
    if ("true".equals(value) || "1".equals(value) || "yes".equals(value)
        || "y".equals(value) || "oui".equals(value) || "o".equals(value)) {
      return Boolean.TRUE;
    }
    if ("false".equals(value) || "0".equals(value) || "no".equals(value)
        || "n".equals(value) || "non".equals(value)) {
      return Boolean.FALSE;
    }
    cliView.displayError(I18n.translate("cli.shell.set.invalidBoolean"));
    return null;
  }

  private void handleSave(String[] tokens, CliView cliView) {
    if (tokens.length < 2) {
      cliView.displayError(I18n.translate("cli.shell.save.usage"));
      return;
    }

    try {
      new SaveManager().saveGame(controller.internalGame(), tokens[1]);
      hasUnsavedChanges = false;
      cliView.displaySuccess(I18n.translate("cli.shell.save.success", tokens[1]));
    } catch (IOException e) {
      cliView.displayError(I18n.translate("cli.shell.save.failure", e.getMessage()));
    }
  }

  private void handleLoad(String[] tokens, CliView cliView) {
    if (tokens.length < 2) {
      cliView.displayError(I18n.translate("cli.shell.load.usage"));
      return;
    }

    try {
      Game loaded = new GameLoader().loadGame(tokens[1]);
      pendingLoadedGame = loaded;
      restartRequested = true;
      cliView.displaySuccess(I18n.translate("cli.shell.load.success", tokens[1]));
    } catch (Exception e) {
      cliView.displayError(I18n.translate("cli.shell.load.failure", e.getMessage()));
    }
  }

  private void handleNetwork(CliInputHandler input, CliView cliView) {
    NetworkManager networkManager = new NetworkManager();
    CliNetworkLobby lobby = new CliNetworkLobby(networkManager, cliView, input);
    lobby.showMenu();
  }

  private void displayHelp(CliView cliView, String cmd) {
    if (cmd == null || cmd.isBlank()) {
      cliView.displayMessage(I18n.translate("cli.shell.help.full"));
      return;
    }

    switch (cmd) {
      case "new":
        cliView.displayMessage(I18n.translate("cli.shell.help.new"));
        return;
      case "help":
        cliView.displayMessage(I18n.translate("cli.shell.help.help"));
        return;
      case "quit":
        cliView.displayMessage(I18n.translate("cli.shell.help.quit"));
        return;
      case "load":
        cliView.displayMessage(I18n.translate("cli.shell.help.load"));
        return;
      case "save":
        cliView.displayMessage(I18n.translate("cli.shell.help.save"));
        return;
      case "pause":
        cliView.displayMessage(I18n.translate("cli.shell.help.pause"));
        return;
      case "hint":
        cliView.displayMessage(I18n.translate("cli.shell.help.hint"));
        return;
      case "undo":
        cliView.displayMessage(I18n.translate("cli.shell.help.undo"));
        return;
      case "redo":
        cliView.displayMessage(I18n.translate("cli.shell.help.redo"));
        return;
      case "show":
        cliView.displayMessage(I18n.translate("cli.shell.help.show"));
        return;
      case "set":
        cliView.displayMessage(I18n.translate("cli.shell.help.set"));
        return;
      case "network":
        cliView.displayMessage(I18n.translate("cli.shell.help.network"));
        return;
      default:
        cliView.displayError(I18n.translate("cli.shell.help.unknown", cmd));
    }
  }

  private void executeMoveAction(Move move, CliView cliView, String successMessage) {
    if (move == null) {
      return;
    }

    try {
      controller.handlePlayerMove(move);
      hasUnsavedChanges = true;
      cliView.displaySuccess(successMessage);
    } catch (RuntimeException e) {
      cliView.displayError(e.getMessage());
    }
  }

  private boolean handleQuitCommand(CliInputHandler input, CliView cliView) {
    if (!hasUnsavedChanges) {
      return !input.askConfirmation(I18n.translate("scrabble.quitConfirmation"));
    }

    boolean saveBeforeQuit = input.askConfirmation(I18n.translate("cli.quit.savePrompt"));
    if (!saveBeforeQuit) {
      return false;
    }

    while (true) {
      String path = input.askText(I18n.translate("cli.quit.savePathPrompt"));
      if (path.isBlank()) {
        cliView.displayError(I18n.translate("cli.quit.save.failure", "empty path"));
      } else {
        try {
          new SaveManager().saveGame(controller.internalGame(), path);
          hasUnsavedChanges = false;
          cliView.displaySuccess(I18n.translate("cli.quit.save.success", path));
          return false;
        } catch (IOException e) {
          cliView.displayError(I18n.translate("cli.quit.save.failure", e.getMessage()));
        }
      }

      boolean retry = input.askConfirmation(I18n.translate("cli.quit.save.retryPrompt"));
      if (!retry) {
        return false;
      }
    }
  }

  private void displayWinner(CliView cliView) {
    Player winner = controller.internalGame().determineWinner();
    if (winner != null) {
      cliView.displaySuccess(
          I18n.translate("cli.game.winner", winner.getName(), winner.getScore()));
    }
  }
}
