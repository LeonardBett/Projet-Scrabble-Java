package fr.ubordeaux.scrabble.view.cli;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.UserInterface;
import fr.ubordeaux.scrabble.view.cli.renderer.BoardRenderer;
import fr.ubordeaux.scrabble.view.cli.renderer.MessageRenderer;
import fr.ubordeaux.scrabble.view.cli.renderer.PlayerRenderer;
import fr.ubordeaux.scrabble.view.cli.renderer.RackRenderer;

/**
 * Full CLI view implementing UserInterface.
 */
public class CliView implements UserInterface {

  private final Game game;
  private final BoardRenderer boardRenderer;
  private final PlayerRenderer playerRenderer;
  private final RackRenderer rackRenderer;
  private final MessageRenderer messageRenderer;

  private boolean isBlitzMode;
  private boolean showBonusSquares;

  /**
   * Creates a CliView with bonus squares shown by default.
   *
   * @param game the game model to display
   */
  public CliView(Game game) {
    this(game, false);
  }

  /**
   * Creates a CliView with optional blitz mode.
   *
   * @param game the game model to display
   * @param isBlitzMode true to enable blitz mode (timed turns)
   */
  public CliView(Game game, boolean isBlitzMode) {
    this.game = game;
    this.isBlitzMode = isBlitzMode;
    this.showBonusSquares = true;
    this.boardRenderer = new BoardRenderer();
    this.playerRenderer = new PlayerRenderer();
    this.rackRenderer = new RackRenderer();
    this.messageRenderer = new MessageRenderer();
  }

  /**
   * Refreshes the entire display.
   */
  @Override
  public void refresh() {
    displayGameState(showBonusSquares);
  }

  /**
   * Displays the complete state of the game.
   *
   * @param showBonusSquares true to highlight bonus squares on the board
   */
  public void displayGameState(boolean showBonusSquares) {
    messageRenderer.separator();
    boardRenderer.render(game.getBoard(), showBonusSquares);
    playerRenderer.renderPlayerList(game.getPlayers());
    displayCurrentPlayer();
  }

  /**
   * Displays the current player and their rack.
   */
  public void displayCurrentPlayer() {
    Player current = game.getCurrentPlayer();
    if (current != null) {
      playerRenderer.renderCurrentPlayer(current);
      rackRenderer.render(current);
    }
  }

  /**
   * Displays an informational message.
   *
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    messageRenderer.info(message);
  }

  /**
   * Displays an error message.
   *
   * @param error the error message to display
   */
  @Override
  public void displayError(String error) {
    messageRenderer.error(error);
  }

  /**
   * Displays a success message.
   *
   * @param message the success message to display
   */
  @Override
  public void displaySuccess(String message) {
    messageRenderer.success(message);
  }

  /**
   * Displays the welcome message.
   */
  public void displayWelcome() {
    messageRenderer.welcome();
  }

  /**
   * Enables or disables blitz mode.
   *
   * @param isBlitzMode true to enable blitz mode
   */
  public void setBlitzMode(boolean isBlitzMode) {
    this.isBlitzMode = isBlitzMode;
  }

  /**
   * Shows or hides bonus squares on the board.
   *
   * @param show true to show bonus squares
   */
  public void setShowBonusSquares(boolean show) {
    this.showBonusSquares = show;
  }

  /**
   * Returns the board renderer.
   *
   * @return the BoardRenderer instance
   */
  public BoardRenderer getBoardRenderer() {
    return boardRenderer;
  }

  /**
   * Returns the player renderer.
   *
   * @return the PlayerRenderer instance
   */
  public PlayerRenderer getPlayerRenderer() {
    return playerRenderer;
  }

  /**
   * Returns the rack renderer.
   *
   * @return the RackRenderer instance
   */
  public RackRenderer getRackRenderer() {
    return rackRenderer;
  }

  /**
   * Returns the message renderer.
   *
   * @return the MessageRenderer instance
   */
  public MessageRenderer getMessageRenderer() {
    return messageRenderer;
  }
}
