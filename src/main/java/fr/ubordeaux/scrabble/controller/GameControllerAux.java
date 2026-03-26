package fr.ubordeaux.scrabble.controller;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.ai.MlAgent;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.dictionary.core.Game;
import fr.ubordeaux.scrabble.model.dictionary.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.dictionary.core.Move;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.cli.CliInputHandler;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.util.List;

/**
 * CLI-specific orchestration extracted from GameController to keep the main
 * controller smaller.
 */
class GameControllerAux {
  private final GameController controller;

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

      runCliLoop(input, cliView, controller.getOrLoadGaddag());
      displayWinner(cliView);
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
    Runtime.getRuntime().addShutdownHook(new Thread(() -> finalAgent.close()));

    return sharedMlAgent;
  }

  private void configureExistingAiPlayers(MlAgent sharedMlAgent) {
    for (Player p : controller.internalGame().getPlayers()) {
      if (p instanceof AiPlayer bot) {
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
      cliView.displayMessage(
          "-> ML Agent activated for " + name + " (" + controller.configuredLanguage() + ")");
    }

    controller.addPlayer(bot);
  }

  private void startCliGame(CliView cliView) {
    controller.startGame();

    if (controller.internalGame().isBlitzModeEnabled()) {
      cliView.displayMessage("⏱  Mode blitz activated — time per player : "
          + controller.internalGame().getPlayers().get(0).getRemainingTimeDisplay());
      controller.startBlitzWatcher(cliView);
    }
  }

  private void runCliLoop(CliInputHandler input, CliView cliView, Gaddag currentGaddag) {
    boolean running = true;
    while (running && !controller.internalGame().isGameOver()) {
      controller.internalView().refresh();
      Player current = controller.internalGame().getCurrentPlayer();

      if (isBlitzTimeElapsed(current, cliView, true)) {
        break;
      }

      if (current.isAutonomous()) {
        playAutonomousTurn(current, currentGaddag, cliView);
        continue;
      }

      running = handleHumanTurn(current, input, cliView);
    }
  }

  private boolean isBlitzTimeElapsed(Player current, CliView cliView, boolean showEndGameError) {
    Game game = controller.internalGame();
    if (!game.isBlitzModeEnabled() || current == null || !current.isOutOfTime()) {
      return false;
    }

    controller.handleBlitzExpiry(current, cliView);
    if (showEndGameError) {
      game.setGameOver(true);
      controller.internalView().displayError(
          "Time's up for " + current.getName() + ". Game is over.");
    }
    return true;
  }

  private void playAutonomousTurn(Player player, Gaddag currentGaddag, CliView cliView) {
    cliView.displayMessage("\n--- It's AI (" + player.getName() + ") turn ---");
    try {
      player.playTurn(controller.internalGame(), currentGaddag);
      Thread.sleep(2000);
    } catch (RuntimeException | InterruptedException e) {
      cliView.displayError("Error during AI's turn: " + e.getMessage());
      e.printStackTrace();
      controller.handlePlayerMove(Move.createPass(player));
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
    switch (action) {
      case "1" -> {
        executeMoveAction(input.askPlayMove(current), cliView, "Move done.");
        return true;
      }
      case "2" -> {
        executeMoveAction(input.askExchangeMove(current), cliView, "Letters exchanged.");
        return true;
      }
      case "3" -> {
        try {
          controller.handlePlayerMove(Move.createPass(current));
          cliView.displayMessage(current.getName() + " skips his turn.");
        } catch (RuntimeException e) {
          cliView.displayError(e.getMessage());
        }
        return true;
      }
      case "4" -> {
        controller.undo();
        return true;
      }
      case "5" -> {
        controller.redo();
        return true;
      }
      case "6" -> {
        return !input.askConfirmation("Do you really want to quit ?");
      }
      case "7" -> {
        controller.provideHint();
        return true;
      }
      default -> {
        cliView.displayError("Invalid choice.");
        return true;
      }
    }
  }

  private void executeMoveAction(Move move, CliView cliView, String successMessage) {
    if (move == null) {
      return;
    }

    try {
      controller.handlePlayerMove(move);
      cliView.displaySuccess(successMessage);
    } catch (RuntimeException e) {
      cliView.displayError(e.getMessage());
    }
  }

  private void displayWinner(CliView cliView) {
    Player winner = controller.internalGame().determineWinner();
    if (winner != null) {
      cliView.displaySuccess("Game over. Winnenr: " + winner.getName()
          + " (" + winner.getScore() + " pts)");
    }
  }
}
