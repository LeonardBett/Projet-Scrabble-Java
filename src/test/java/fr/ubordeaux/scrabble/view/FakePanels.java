package fr.ubordeaux.scrabble.view;

import fr.ubordeaux.scrabble.model.dictionary.core.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fake implementations of GUI panels for testing without JavaFX dependency.
 * Uses composition pattern (no inheritance) to avoid JavaFX constructor
 * initialization.
 */
public class FakePanels {

  /** Fake BoardPanel - records all method calls instead of rendering. */
  public static class FakeBoardPanel {
    public List<String> calls = new ArrayList<>();

    public void placeTile(int row, int col, char character, int value) {
      calls.add("placeTile(" + row + "," + col + "," + character + "," + value + ")");
    }

    public void clearTile(int row, int col) {
      calls.add("clearTile(" + row + "," + col + ")");
    }

    public void updateBoard() {
      calls.add("updateBoard()");
    }

    public void clearAllPending() {
      calls.add("clearAllPending()");
    }

    public void setBoard(Object board) {
      calls.add("setBoard()");
    }

    public void setDisable(boolean disable) {
      calls.add("setDisable(" + disable + ")");
    }

    public boolean isDisable() {
      return false;
    }

    public boolean wasPlaceTileCalled(int row, int col, char character) {
      String call = "placeTile(" + row + "," + col + "," + character + ",";
      return calls.stream().anyMatch(c -> c.startsWith(call));
    }

    public boolean wasClearTileCalled(int row, int col) {
      String call = "clearTile(" + row + "," + col + ")";
      return calls.contains(call);
    }

    public boolean wasUpdateBoardCalled() {
      return calls.contains("updateBoard()");
    }

    public boolean wasClearAllPendingCalled() {
      return calls.contains("clearAllPending()");
    }

    public int callCount() {
      return calls.size();
    }

    public void reset() {
      calls.clear();
    }
  }

  /** Fake RackPanel - records method calls. */
  public static class FakeRackPanel {
    public List<String> calls = new ArrayList<>();
    private Consumer<Tile> onTileDraggedCallback;

    public void setRack(Object rack) {
      calls.add("setRack()");
    }

    public void hideTile(Tile tile) {
      calls.add("hideTile(" + (tile != null ? tile.getCharacter() : "null") + ")");
    }

    public void setOnTileDragged(Consumer<Tile> callback) {
      calls.add("setOnTileDragged()");
      this.onTileDraggedCallback = callback;
    }

    public void setDisable(boolean disable) {
      calls.add("setDisable(" + disable + ")");
    }

    public Consumer<Tile> getOnTileDraggedCallback() {
      return onTileDraggedCallback;
    }

    public boolean wasSetRackCalled() {
      return calls.contains("setRack()");
    }

    public boolean wasHideTileCalled() {
      return calls.stream().anyMatch(c -> c.startsWith("hideTile("));
    }

    public int callCount() {
      return calls.size();
    }

    public void reset() {
      calls.clear();
      onTileDraggedCallback = null;
    }
  }

  /** Fake MessagePanel - records dialog calls. */
  public static class FakeMessagePanel {
    public List<String> calls = new ArrayList<>();
    public boolean confirmResult = true;

    public void showInfo(String title, String message) {
      calls.add("showInfo(" + title + ")");
    }

    public void showError(String message) {
      calls.add("showError(" + message + ")");
    }

    public boolean showConfirmation(String message) {
      calls.add("showConfirmation(" + message + ")");
      return confirmResult;
    }

    public boolean wasErrorShown() {
      return calls.stream().anyMatch(c -> c.startsWith("showError("));
    }

    public boolean wasInfoShown() {
      return calls.stream().anyMatch(c -> c.startsWith("showInfo("));
    }

    public boolean wasConfirmationAsked() {
      return calls.stream().anyMatch(c -> c.startsWith("showConfirmation("));
    }

    public int callCount() {
      return calls.size();
    }

    public void reset() {
      calls.clear();
      confirmResult = true;
    }
  }

  /** Fake ScorePanel - records update calls. */
  public static class FakeScorePanel {
    public List<String> calls = new ArrayList<>();

    public void updateScores(String[] names, int[] scores) {
      calls.add("updateScores(" + (names != null ? names.length : 0) + ")");
    }

    public void updateBagInfo(int remaining) {
      calls.add("updateBagInfo(" + remaining + ")");
    }

    public void highlightCurrentPlayer(int index, String name) {
      calls.add("highlightCurrentPlayer(" + index + "," + name + ")");
    }

    public void startBlitzTimers(Object players, Runnable callback) {
      calls.add("startBlitzTimers()");
    }

    public void stopBlitzTimers() {
      calls.add("stopBlitzTimers()");
    }

    public boolean wasScoresUpdated() {
      return calls.stream().anyMatch(c -> c.startsWith("updateScores("));
    }

    public boolean wasBagInfoUpdated() {
      return calls.stream().anyMatch(c -> c.startsWith("updateBagInfo("));
    }

    public int callCount() {
      return calls.size();
    }

    public void reset() {
      calls.clear();
    }
  }

  /** Fake ControlPanel - records button setup/state. */
  public static class FakeControlPanel {
    public List<String> calls = new ArrayList<>();

    public void setGameplayButtonsDisabled(boolean disabled) {
      calls.add("setGameplayButtonsDisabled(" + disabled + ")");
    }

    public Consumer<Object> getPlayButton() {
      return obj -> calls.add("playButtonClicked()");
    }

    public Consumer<Object> getPassButton() {
      return obj -> calls.add("passButtonClicked()");
    }

    public Consumer<Object> getExchangeButton() {
      return obj -> calls.add("exchangeButtonClicked()");
    }

    public boolean wasPlayButtonsDisabled(boolean disabled) {
      String call = "setGameplayButtonsDisabled(" + disabled + ")";
      return calls.contains(call);
    }

    public int callCount() {
      return calls.size();
    }

    public void reset() {
      calls.clear();
    }
  }
}
