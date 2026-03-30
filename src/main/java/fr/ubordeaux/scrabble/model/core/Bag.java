package fr.ubordeaux.scrabble.model.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the bag of tiles in the game. Manages the stock of letters and random distribution.
 */
public class Bag {
  private final List<Tile> tiles;
  private final Random random;
  private String language;

  // Size of the bag, only use in online client mode
  private int onlineSize = -1;

  /**
   * Constructor: initializes the bag with the standard distribution of tiles.
   */
  public Bag() {
    this(Tile.getActiveLanguage());
  }

  /**
   * Constructor: initializes the bag with the standard distribution of tiles for the given
   * language.
   *
   * @param language language code ("en" or "fr")
   */
  public Bag(String language) {
    this.tiles = new ArrayList<>();
    this.random = new Random();
    reset(language);
  }

  private void initializeBag(String language) {
    if ("fr".equals(language)) {
      // French Scrabble distribution (102 tiles).
      addTiles('A', 9, language);
      addTiles('B', 2, language);
      addTiles('C', 2, language);
      addTiles('D', 3, language);
      addTiles('E', 15, language);
      addTiles('F', 2, language);
      addTiles('G', 2, language);
      addTiles('H', 2, language);
      addTiles('I', 8, language);
      addTiles('J', 1, language);
      addTiles('K', 1, language);
      addTiles('L', 5, language);
      addTiles('M', 3, language);
      addTiles('N', 6, language);
      addTiles('O', 6, language);
      addTiles('P', 2, language);
      addTiles('Q', 1, language);
      addTiles('R', 6, language);
      addTiles('S', 6, language);
      addTiles('T', 6, language);
      addTiles('U', 6, language);
      addTiles('V', 2, language);
      addTiles('W', 1, language);
      addTiles('X', 1, language);
      addTiles('Y', 1, language);
      addTiles('Z', 1, language);
      addJokers(2, language);
      return;
    } else {     // English Scrabble distribution (100 tiles).
      addTiles('A', 9, language);
      addTiles('B', 2, language);
      addTiles('C', 2, language);
      addTiles('D', 4, language);
      addTiles('E', 12, language);
      addTiles('F', 2, language);
      addTiles('G', 3, language);
      addTiles('H', 2, language);
      addTiles('I', 9, language);
      addTiles('J', 1, language);
      addTiles('K', 1, language);
      addTiles('L', 4, language);
      addTiles('M', 2, language);
      addTiles('N', 6, language);
      addTiles('O', 8, language);
      addTiles('P', 2, language);
      addTiles('Q', 1, language);
      addTiles('R', 6, language);
      addTiles('S', 4, language);
      addTiles('T', 6, language);
      addTiles('U', 4, language);
      addTiles('V', 2, language);
      addTiles('W', 2, language);
      addTiles('X', 1, language);
      addTiles('Y', 2, language);
      addTiles('Z', 1, language);
      addJokers(2, language);
    }
  }

  private void addTiles(char letter, int count, String language) {
    for (int i = 0; i < count; i++) {
      tiles.add(new Tile(letter, language));
    }
  }

  private void addJokers(int count, String language) {
    for (int i = 0; i < count; i++) {
      tiles.add(new Tile(' ', true, language));
    }
  }

  /**
   * Resets the bag content using the distribution of the selected language.
   *
   * @param language language code ("en" or "fr")
   */
  public void reset(String language) {
    this.language = Tile.normalizeLanguage(language);
    this.tiles.clear();
    initializeBag(this.language);
    shuffle();
  }

  /**
   * Returns the language currently used by this bag distribution.
   *
   * @return language code.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Shuffles the content of the bag.
   */
  public void shuffle() {
    Collections.shuffle(tiles, random);
  }

  /**
   * Draws a single tile from the bag.
   *
   * @return The drawn Tile, or null if the bag is empty.
   */
  public Tile drawTile() {
    if (tiles.isEmpty()) {
      return null;
    }
    return tiles.removeLast();
  }

  /**
   * Puts a list of tiles back into the bag and shuffles it. Used for the "Exchange" move.
   *
   * @param tilesToReturn The tiles to put back.
   */
  public void putBack(List<Tile> tilesToReturn) {
    tiles.addAll(tilesToReturn);
    shuffle();
  }

  /**
   * Removes a specific tile from the bag. Used for undoing an exchange move.
   *
   * @param tile The tile to remove.
   * @return true if the tile was found and removed, false otherwise.
   */
  public boolean removeTile(Tile tile) {
    return tiles.remove(tile);
  }

  /**
   * Indicates whether the local bag has no remaining tiles.
   *
   * @return true when no tile can be drawn locally.
   */
  public boolean isEmpty() {
    return tiles.isEmpty();
  }

  /**
   * Returns the number of tiles currently available in the local bag.
   *
   * @return the local bag size.
   */
  public int size() {
    return tiles.size();
  }

  /**
   * Set the size of the bag used in online client mode.
   *
   * @param bagSize the new size of the bag
   */
  public void setOnlineSize(int bagSize) {
    this.onlineSize = bagSize;
  }

  /**
   * Returns the synchronized bag size used by online clients.
   *
   * @return the remote bag size, or -1 when not set.
   */
  public int getOnlineSize() {
    return onlineSize;
  }
}
