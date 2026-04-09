package fr.ubordeaux.scrabble.view.gui.main;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles refresh and UI state synchronization for {@link ScrabbleGui}.
 */
public final class ScrabbleGuiRefresh {

  /**
   * Creates a refresh helper.
   */
  public ScrabbleGuiRefresh() {
  }

  /**
   * Refreshes all UI sections.
   *
   * @param gui Scrabble GUI instance
   */
  public void refreshAll(ScrabbleGui gui) {
    gui.syncPauseAvailability();
    doRefreshBoard(gui.getBoardPanel());
    doRefreshRack(gui.getRackPanel(), gui.getGameInstance(), gui::onTileDragged);
    doRefreshScores(gui.getScorePanel(), gui.getGameInstance());
    gui.checkAiTurnIfNeeded();
  }

  /**
   * Refreshes board UI.
   *
   * @param gui Scrabble GUI instance
   */
  public void refreshBoard(ScrabbleGui gui) {
    doRefreshBoard(gui.getBoardPanel());
  }

  /**
   * Refreshes rack UI.
   *
   * @param gui Scrabble GUI instance
   */
  public void refreshRack(ScrabbleGui gui) {
    doRefreshRack(gui.getRackPanel(), gui.getGameInstance(), gui::onTileDragged);
  }

  /**
   * Refreshes score UI.
   *
   * @param gui Scrabble GUI instance
   */
  public void refreshScores(ScrabbleGui gui) {
    doRefreshScores(gui.getScorePanel(), gui.getGameInstance());
  }

  private void doRefreshBoard(BoardPanel boardPanel) {
    boardPanel.updateBoard();
  }

  private void doRefreshRack(RackPanel rackPanel,
      Game game,
      Consumer<fr.ubordeaux.scrabble.model.core.Tile> dragHandler) {
    Player currentPlayer = game.getCurrentPlayer();
    rackPanel.setRack(currentPlayer != null
        ? currentPlayer.getRack()
        : new fr.ubordeaux.scrabble.model.core.Rack());
    rackPanel.setOnTileDragged(dragHandler);
    if (currentPlayer != null) {
      int idx = game.getPlayers().indexOf(currentPlayer);
      if (idx >= 0) {
        rackPanel.setCurrentPlayerNumber(idx + 1);
      }
    }
  }

  private void doRefreshScores(ScorePanel scorePanel, Game game) {
    List<Player> players = game.getPlayers();
    if (players.isEmpty()) {
      return;
    }
    String[] names = players.stream().map(Player::getName).toArray(String[]::new);
    int[] scores = players.stream().mapToInt(Player::getScore).toArray();
    scorePanel.updateScores(names, scores);
    scorePanel.updateBagInfo(game.getBag().size());
    Player current = game.getCurrentPlayer();
    if (current != null) {
      int idx = players.indexOf(current);
      if (idx >= 0) {
        scorePanel.highlightCurrentPlayer(idx, current.getName());
      }
    }
  }
}
