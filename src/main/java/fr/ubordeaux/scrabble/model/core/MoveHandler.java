package fr.ubordeaux.scrabble.model.core;

import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the execution of moves in the game. This class encapsulates the logic for processing
 * PLAY, EXCHANGE, and PASS moves.
 */
public class MoveHandler {
  private final Game game;
  private Gaddag dictionary;
  private String dictionaryLanguage;

  /**
   * Creates a move handler bound to a game instance.
   *
   * @param game owning game.
   */
  public MoveHandler(Game game) {
    this.game = game;
  }

  /**
   * Extracts the complete word string formed by placing tiles at the given position. This includes
   * existing tiles on the board (prefix and suffix) combined with new tiles.
   *
   * @param startPosition Starting position of the word.
   * @param direction     Direction of the word (HORIZONTAL or VERTICAL).
   * @param tiles         Tiles to be placed.
   * @return The complete word as a String (including existing tiles from the board).
   */
  public String getCompleteWord(Point startPosition, Direction direction, List<Tile> tiles) {
    List<String> formedWords = getFormedWords(startPosition, direction, tiles);
    return formedWords.isEmpty() ? "" : formedWords.getFirst();
  }

  /**
   * Extracts all words formed by placing the tiles at the given position. The main word is returned
   * first, followed by any perpendicular words created.
   *
   * @param startPosition starting position of the move.
   * @param direction     direction of the move.
   * @param tiles         tiles to be placed.
   * @return ordered list of formed words.
   */
  public List<String> getFormedWords(Point startPosition, Direction direction, List<Tile> tiles) {
    List<Square> wordSquares = new ArrayList<>();
    List<Point> wordPositions = new ArrayList<>();
    List<Square> newlyPlacedSquares = new ArrayList<>();
    List<Point> newlyPlacedPositions = new ArrayList<>();
    List<Tile> newlyPlacedTiles = new ArrayList<>();

    buildWordForMove(startPosition, direction, tiles, wordSquares, wordPositions,
        newlyPlacedSquares, newlyPlacedPositions, newlyPlacedTiles);

    List<String> formedWords = new ArrayList<>();
    formedWords.add(buildWord(wordSquares, newlyPlacedSquares, newlyPlacedTiles));

    for (int i = 0; i < newlyPlacedPositions.size(); i++) {
      String crossWord =
          buildPerpendicularWord(newlyPlacedPositions.get(i), direction, newlyPlacedTiles.get(i));
      if (crossWord.length() > 1) {
        formedWords.add(crossWord);
      }
    }

    return formedWords;
  }

  private String buildWord(List<Square> wordSquares, List<Square> newlyPlacedSquares,
                           List<Tile> newlyPlacedTiles) {
    StringBuilder word = new StringBuilder();
    for (Square square : wordSquares) {
      if (!square.isEmpty()) {
        word.append(square.getTile().getCharacter());
        continue;
      }

      int placedIndex = newlyPlacedSquares.indexOf(square);
      if (placedIndex >= 0) {
        word.append(newlyPlacedTiles.get(placedIndex).getCharacter());
      }
    }
    return word.toString();
  }

  private String buildPerpendicularWord(Point pos, Direction mainDirection, Tile placedTile) {
    int pdx = mainDirection == Direction.HORIZONTAL ? 0 : 1;
    int pdy = mainDirection == Direction.HORIZONTAL ? 1 : 0;

    StringBuilder word = new StringBuilder();

    int bx = pos.getX() - pdx;
    int by = pos.getY() - pdy;
    List<Character> prefix = new ArrayList<>();
    while (true) {
      Point bp = new Point(bx, by);
      Square bs = game.getBoard().getSquare(bp);
      if (bs == null || bs.isEmpty()) {
        break;
      }
      prefix.add(0, bs.getTile().getCharacter());
      bx -= pdx;
      by -= pdy;
    }

    for (char prefixLetter : prefix) {
      word.append(prefixLetter);
    }
    word.append(placedTile.getCharacter());

    int fx = pos.getX() + pdx;
    int fy = pos.getY() + pdy;
    while (true) {
      Point fp = new Point(fx, fy);
      Square fs = game.getBoard().getSquare(fp);
      if (fs == null || fs.isEmpty()) {
        break;
      }
      word.append(fs.getTile().getCharacter());
      fx += pdx;
      fy += pdy;
    }

    return word.toString();
  }

  /**
   * Validates and applies a PLAY move.
   *
   * @param move move to apply.
   */
  public void handlePlayMove(Move move) {
    // 1. Extract move details
    Player player = move.getPlayer();
    List<Tile> tiles = move.getTiles();
    Point startPosition = move.getStartPosition();
    Direction direction = move.getDirection();

    // Build and validate the word (including prefix/suffix).
    List<Square> wordSquares = new ArrayList<>();
    List<Point> wordPositions = new ArrayList<>();
    List<Square> newlyPlacedSquares = new ArrayList<>();
    List<Point> newlyPlacedPositions = new ArrayList<>();
    List<Tile> newlyPlacedTiles = new ArrayList<>();
    buildWordForMove(startPosition, direction, tiles, wordSquares, wordPositions,
        newlyPlacedSquares, newlyPlacedPositions, newlyPlacedTiles);
    validateWordsAgainstDictionary(getFormedWords(startPosition, direction, tiles));
    // Resolve concrete rack tiles to consume (supports jokers replacing letters)
    List<Tile> resolvedTiles = resolveTilesFromRack(player.getRack().getTiles(), newlyPlacedTiles);

    // Place the tiles from the move into the recorded newly placed positions
    for (int i = 0; i < newlyPlacedPositions.size(); i++) {
      Point p = newlyPlacedPositions.get(i);
      Square sq = game.getBoard().getSquare(p);
      Tile tileToPlace = resolvedTiles.get(i);
      if (!sq.isEmpty()) {
        throw new IllegalArgumentException("Attempting to place a tile on a non-empty square.");
      }
      sq.setTile(tileToPlace);
    }

    // Save placed positions in the move for undo
    move.setPlacedPositions(newlyPlacedPositions);
    move.setPlacedTiles(resolvedTiles);

    // 5. Calculate the score using the Scoring utility (main word + crosses)
    int totalScore = 0;
    totalScore += Scoring.calculateWordScore(wordSquares, newlyPlacedSquares);

    for (Point p : newlyPlacedPositions) {
      List<Square> cross = getPerpendicularWordSquares(p, direction);
      if (cross.size() > 1) {
        totalScore += Scoring.calculateWordScore(cross, newlyPlacedSquares);
      }
    }

    player.addScore(totalScore);

    // Save score in move for undo
    move.setScoreGained(totalScore);

    // 6. Remove the used tiles from the player's rack (resolved from rack content)
    for (Tile tile : resolvedTiles) {
      player.getRack().removeTile(tile);
    }

    // 7. Refill the player's rack from the bag
    List<Tile> drawn = game.refillRack(player);
    move.setDrawnTiles(drawn);

    GameLogger.logVerbose(
        "Player " + player.getName() + " played a word for " + totalScore + " points.");
  }

  private void validateWordsAgainstDictionary(List<String> formedWords) {
    Gaddag gaddagDictionary = getOrLoadDictionary();

    for (String formedWord : formedWords) {
      if (formedWord == null || formedWord.isBlank()) {
        continue;
      }
      ensureWordInDictionary(formedWord, gaddagDictionary);
    }
  }

  private void ensureWordInDictionary(String word, Gaddag gaddagDictionary) {
    if (word == null || word.isBlank() || word.length() < 2) {
      return;
    }

    if (!gaddagDictionary.containsWord(word.toUpperCase())) {
      throw new IllegalArgumentException("Word not found in dictionary: " + word);
    }
  }

  private Gaddag getOrLoadDictionary() {
    String dictionarySource = resolveDictionarySource();
    if (dictionary != null && dictionarySource.equals(dictionaryLanguage)) {
      return dictionary;
    }

    Gaddag loadedDictionary = new Gaddag();
    try (BufferedReader br = openDictionaryReader(dictionarySource)) {
      String line;
      while ((line = br.readLine()) != null) {
        String cleanWord = line.trim().toUpperCase();
        if (!cleanWord.isEmpty()) {
          loadedDictionary.add(cleanWord);
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error while loading the dictionary: " + e.getMessage(), e);
    }

    dictionary = loadedDictionary;
    dictionaryLanguage = dictionarySource;
    return dictionary;
  }

  private String resolveDictionarySource() {
    String customDictionaryPath = game.getDictionaryPathOverride();
    if (customDictionaryPath != null && !customDictionaryPath.isBlank()) {
      return customDictionaryPath;
    }
    return "dictionaries/lexicon_" + Tile.normalizeLanguage(game.getLanguage()) + ".txt";
  }

  private BufferedReader openDictionaryReader(String dictionarySource) throws Exception {
    if (dictionarySource.startsWith("dictionaries/")) {
      InputStream is = getClass().getClassLoader().getResourceAsStream(dictionarySource);
      if (is == null) {
        throw new IllegalStateException(
            "Dictionary file " + dictionarySource + " not found in resources.");
      }
      return new BufferedReader(new InputStreamReader(is));
    }

    return java.nio.file.Files.newBufferedReader(java.nio.file.Path.of(dictionarySource));
  }

  private List<Tile> resolveTilesFromRack(List<Tile> rackTiles, List<Tile> tilesToPlace) {
    List<Tile> remaining = new ArrayList<>(rackTiles);
    List<Tile> resolved = new ArrayList<>();

    for (Tile requested : tilesToPlace) {
      Tile fromRack = findAndConsumeMatchingTile(remaining, requested);
      if (fromRack == null) {
        throw new IllegalArgumentException("Player does not have the tile "
            + requested.getCharacter());
      }

      if (fromRack.isJoker() && requested.getCharacter() != ' ') {
        resolved.add(new Tile(requested.getCharacter(), true));
      } else {
        resolved.add(fromRack);
      }
    }

    return resolved;
  }

  private Tile findAndConsumeMatchingTile(List<Tile> remainingRackTiles, Tile requested) {
    for (int i = 0; i < remainingRackTiles.size(); i++) {
      Tile candidate = remainingRackTiles.get(i);
      if (candidate.equals(requested)) {
        remainingRackTiles.remove(i);
        return candidate;
      }
    }

    if (requested.isJoker()) {
      for (int i = 0; i < remainingRackTiles.size(); i++) {
        Tile candidate = remainingRackTiles.get(i);
        if (candidate.isJoker()) {
          remainingRackTiles.remove(i);
          return candidate;
        }
      }
      return null;
    }

    for (int i = 0; i < remainingRackTiles.size(); i++) {
      Tile candidate = remainingRackTiles.get(i);
      if (candidate.isJoker()) {
        remainingRackTiles.remove(i);
        return candidate;
      }
    }

    return null;
  }

  /**
   * Applies an EXCHANGE move by returning selected tiles to the bag and drawing new ones.
   *
   * @param move move to apply.
   */
  public void handleExchangeMove(Move move) {
    Player player = move.getPlayer();
    List<Tile> tilesToExchange = move.getTiles();

    // Rule: Cannot exchange if bag has fewer than 7 tiles
    if (game.getBag().size() < 7) {
      throw new IllegalStateException("Cannot exchange tiles: bag has fewer than 7 tiles left.");
    }

    // 1. Verify player has these tiles (and remove them)
    for (Tile tile : tilesToExchange) {
      if (!player.getRack().removeTile(tile)) {
        // In a real scenario, we should probably rollback or check beforehand
        throw new IllegalArgumentException("Player does not have the tile " + tile.getCharacter());
      }
    }

    // 2. Put them back in the bag
    game.getBag().putBack(tilesToExchange);

    // 3. Draw new tiles
    List<Tile> drawn = game.refillRack(player);
    move.setDrawnTiles(drawn);

    GameLogger.logVerbose(
        "Player " + player.getName() + " exchanged " + tilesToExchange.size() + " tiles.");
  }

  /**
   * Applies a PASS move.
   *
   * @param move move to apply.
   */
  public void handlePassMove(Move move) {
    GameLogger.logVerbose("Player " + move.getPlayer() + " passed.");
  }

  /**
   * Reverts a previously applied move.
   *
   * @param move move to revert.
   */
  public void revertMove(Move move) {
    if (move.getType() != MoveType.PLAY) {
      return; // Only PLAY moves are handled for now
    }

    Player player = move.getPlayer();

    // 1. Revert Score
    player.addScore(-move.getScoreGained());

    // 2. Revert Rack & Bag (Remove drawn tiles from rack and put them back in bag)
    List<Tile> drawnTiles = move.getDrawnTiles();
    if (drawnTiles != null && !drawnTiles.isEmpty()) {
      for (Tile tile : drawnTiles) {
        player.getRack().removeTile(tile);
      }
      game.getBag().putBack(drawnTiles);
    }

    // 3. Revert Board (Remove placed tiles from board and put them back in rack)
    List<Point> placed = move.getPlacedPositions();
    List<Tile> placedTiles = move.getPlacedTiles();
    if (placed != null && !placed.isEmpty() && placedTiles != null && !placedTiles.isEmpty()) {
      for (int i = 0; i < placed.size() && i < placedTiles.size(); i++) {
        Point currentPos = placed.get(i);
        Square square = game.getBoard().getSquare(currentPos);
        Tile tile = placedTiles.get(i);

        if (square != null && tile != null && square.getTile() == tile) {
          square.setTile(null);
          player.getRack().addTile(tile);
        }
      }
    }
  }

  // Helper: compute the list of board positions covered by the move and validate
  // bounds
  private List<Point> computePositions(Point startPosition, Direction direction, int length) {
    int startX = startPosition.getX();
    int startY = startPosition.getY();
    int dx = direction == Direction.HORIZONTAL ? 1 : 0;
    int dy = direction == Direction.VERTICAL ? 1 : 0;

    List<Point> positions = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      Point p = new Point(startX + i * dx, startY + i * dy);
      Square sq = game.getBoard().getSquare(p);
      if (sq == null) {
        throw new IllegalArgumentException("Word extends beyond board boundaries.");
      }
      positions.add(p);
    }
    return positions;
  }

  /**
   * Build the full word on the board (including existing prefix/suffix), record newly placed
   * squares/positions and validate first-move/adjacency rules. Throws IllegalArgumentException on
   * invalid placements.
   */
  private void buildWordForMove(Point startPosition, Direction direction, List<Tile> tiles,
                                List<Square> wordSquares, List<Point> wordPositions,
                                List<Square> newlyPlacedSquares,
                                List<Point> newlyPlacedPositions, List<Tile> newlyPlacedTiles) {

    int x = startPosition.getX();
    int y = startPosition.getY();
    int dx = direction == Direction.HORIZONTAL ? 1 : 0;
    int dy = direction == Direction.VERTICAL ? 1 : 0;

    // prefix (existing contiguous tiles before start)
    int bx = x - dx;
    int by = y - dy;
    List<Square> prefix = new ArrayList<>();
    List<Point> prefixPos = new ArrayList<>();
    while (true) {
      Point bp = new Point(bx, by);
      Square bs = game.getBoard().getSquare(bp);
      if (bs == null || bs.isEmpty()) {
        break;
      }
      prefix.add(0, bs);
      prefixPos.add(0, bp);
      bx -= dx;
      by -= dy;
    }
    wordSquares.addAll(prefix);
    wordPositions.addAll(prefixPos);

    // forward scan, placing provided tiles on empty squares and including existing
    // tiles
    int tileIndex = 0;
    int curX = x;
    int curY = y;
    while (true) {
      Point cp = new Point(curX, curY);
      Square cs = game.getBoard().getSquare(cp);
      if (cs == null) {
        if (tileIndex == tiles.size()) {
          break;
        }
        throw new IllegalArgumentException("Word extends beyond board boundaries.");
      }

      if (!cs.isEmpty()) {
        // existing tile contributes to the word; do not consume a provided tile
        wordSquares.add(cs);
        wordPositions.add(cp);
      } else if (tileIndex < tiles.size()) {
        wordSquares.add(cs);
        wordPositions.add(cp);
        newlyPlacedSquares.add(cs);
        newlyPlacedPositions.add(new Point(curX, curY));
        // record which provided tile will be placed here
        newlyPlacedTiles.add(tiles.get(tileIndex));
        tileIndex++;
      } else {
        break;
      }

      curX += dx;
      curY += dy;
    }

    if (tileIndex != tiles.size()) {
      throw new IllegalArgumentException(
          "Not enough space on the board to place all tiles of the move.");
    }

    // Validation: center for first move
    if (!game.isFirstMoveDone()) {
      int boardSize = game.getBoard().getSize();
      Point center = new Point(boardSize / 2, boardSize / 2);
      boolean coversCenter = wordPositions.stream().anyMatch(center::equals);
      if (!coversCenter) {
        throw new IllegalArgumentException("First word must cover the center square.");
      }
    } else {
      // Non-first move: must touch existing tiles (overlap or adjacency)
      boolean touchesExisting = false;
      for (Square sq : wordSquares) {
        if (!sq.isEmpty() && !newlyPlacedSquares.contains(sq)) {
          touchesExisting = true;
          break;
        }
      }

      if (!touchesExisting) {
        for (Point npPos : newlyPlacedPositions) {
          Point up = new Point(npPos.getX(), npPos.getY() - 1);
          Point down = new Point(npPos.getX(), npPos.getY() + 1);
          Point left = new Point(npPos.getX() - 1, npPos.getY());
          Point right = new Point(npPos.getX() + 1, npPos.getY());
          Square su = game.getBoard().getSquare(up);
          Square sd = game.getBoard().getSquare(down);
          Square sl = game.getBoard().getSquare(left);
          Square sr = game.getBoard().getSquare(right);
          if ((su != null && !su.isEmpty()) || (sd != null && !sd.isEmpty())
              || (sl != null && !sl.isEmpty()) || (sr != null && !sr.isEmpty())) {
            touchesExisting = true;
            break;
          }
        }
      }

      if (!touchesExisting) {
        throw new IllegalArgumentException("Placed word must touch existing tiles on the board.");
      }
    }
  }

  /**
   * Returns the list of squares forming the perpendicular word that includes the square at `pos`.
   * The order is from the start of that perpendicular word to its end.
   */
  private List<Square> getPerpendicularWordSquares(Point pos, Direction mainDirection) {
    List<Square> result = new ArrayList<>();
    int pdx = mainDirection == Direction.HORIZONTAL ? 0 : 1;
    int pdy = mainDirection == Direction.HORIZONTAL ? 1 : 0;

    // scan backward
    int bx = pos.getX() - pdx;
    int by = pos.getY() - pdy;
    List<Square> prefix = new ArrayList<>();
    while (true) {
      Point bp = new Point(bx, by);
      Square bs = game.getBoard().getSquare(bp);
      if (bs == null || bs.isEmpty()) {
        break;
      }
      prefix.add(0, bs);
      bx -= pdx;
      by -= pdy;
    }

    // center
    Square center = game.getBoard().getSquare(pos);
    if (center == null) {
      return result;
    }

    // scan forward
    int fx = pos.getX() + pdx;
    int fy = pos.getY() + pdy;
    List<Square> suffix = new ArrayList<>();
    while (true) {
      Point fp = new Point(fx, fy);
      Square fs = game.getBoard().getSquare(fp);
      if (fs == null || fs.isEmpty()) {
        break;
      }
      suffix.add(fs);
      fx += pdx;
      fy += pdy;
    }

    result.addAll(prefix);
    result.add(center);
    result.addAll(suffix);
    return result;
  }

  // Helper: validate placement rules (center coverage for first move, touching
  // existing tiles, conflicts)
  private void validatePlacement(List<Point> positions, List<Tile> tiles) {
    // Determine if this is the first play using Game flag (avoid scanning the whole
    // board)
    boolean boardEmpty = !game.isFirstMoveDone();
    if (boardEmpty) {
      int boardSize = game.getBoard().getSize();
      Point center = new Point(boardSize / 2, boardSize / 2);
      boolean coversCenter = positions.stream().anyMatch(center::equals);
      if (!coversCenter) {
        throw new IllegalArgumentException("First word must cover the center square.");
      }
      return;
    }

    // Non-first move: must touch existing tiles (overlap allowed if letters match)
    boolean touchesExisting = false;
    for (int i = 0; i < positions.size(); i++) {
      Point p = positions.get(i);
      Square sq = game.getBoard().getSquare(p);
      if (!sq.isEmpty()) {
        Tile existing = sq.getTile();
        Tile provided = tiles.get(i);
        if (existing.getCharacter() != provided.getCharacter()) {
          throw new IllegalArgumentException("Word conflicts with existing tiles on the board.");
        }
        touchesExisting = true;
        break;
      }

      Point up = new Point(p.getX(), p.getY() - 1);
      Point down = new Point(p.getX(), p.getY() + 1);
      Point left = new Point(p.getX() - 1, p.getY());
      Point right = new Point(p.getX() + 1, p.getY());
      Square su = game.getBoard().getSquare(up);
      Square sd = game.getBoard().getSquare(down);
      Square sl = game.getBoard().getSquare(left);
      Square sr = game.getBoard().getSquare(right);
      if ((su != null && !su.isEmpty()) || (sd != null && !sd.isEmpty())
          || (sl != null && !sl.isEmpty()) || (sr != null && !sr.isEmpty())) {
        touchesExisting = true;
        break;
      }
    }

    if (!touchesExisting) {
      throw new IllegalArgumentException("Placed word must touch existing tiles on the board.");
    }
  }
}
