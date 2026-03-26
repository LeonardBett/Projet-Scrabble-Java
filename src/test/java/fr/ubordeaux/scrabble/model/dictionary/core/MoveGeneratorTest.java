package fr.ubordeaux.scrabble.model.dictionary.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveGeneratorTest {

  private MoveGenerator generator;
  private Gaddag gaddag;
  private Board board;

  @BeforeEach
  void setUp() {
    generator = new MoveGenerator();
    gaddag = new Gaddag();
    board = new Board(); // Initialize a standard 15x15 board
  }

  /**
   * Test that the generator finds playable words when an anchor tile exists on
   * the board and the
   * rack contains the required letters.
   */
  @Test
  void getPlayableWordsListShouldFindMovesWhenAnchorExists() {
    // We add "CAT" to the dictionary
    gaddag.add("CAT");

    // Place an anchor 'C' at the center (7, 7)
    board.getSquare(new Point(7, 7)).setTile(new Tile('C'));

    // Rack contains 'A' and 'T'
    Character[] rack = { 'A', 'T' };

    List<PlayableWord> results = generator.getPlayableWordsList(board, rack, gaddag);

    assertFalse(results.isEmpty(), "Should have found at least one move");
    assertTrue(results.stream().anyMatch(pw -> pw.getWord().equals("CAT")),
        "The generator should find the word 'CAT' anchored on 'C'");
  }

  /**
   * Test that the generator correctly uses Joker tiles (represented by a space '
   * ') from the rack
   * to form words.
   */
  @Test
  void getPlayableWordsListShouldHandleJokersInRack() {
    gaddag.add("DOG");

    // Anchor 'D' at (7, 7)
    board.getSquare(new Point(7, 7)).setTile(new Tile('D'));

    // Rack has 'O' and a Joker ' ' (used as 'G')
    Character[] rack = { 'O', 'G' };

    List<PlayableWord> results = generator.getPlayableWordsList(board, rack, gaddag);

    assertTrue(results.stream().anyMatch(pw -> pw.getWord().equals("DOG")),
        "The generator should find 'DOG' by substituting the Joker for 'G'");
  }

  /**
   * Test that moves creating invalid cross-words (perpendicular words) are
   * correctly rejected by
   * the generator.
   */
  @Test
  void getPlayableWordsListShouldRejectInvalidCrossWords() {
    // "OX" is NOT in our dictionary
    gaddag.add("HELLO");

    // Anchor 'H' at (7, 7)
    board.getSquare(new Point(7, 7)).setTile(new Tile('H'));

    // An existing tile 'X' at (8, 8)
    board.getSquare(new Point(8, 8)).setTile(new Tile('X'));

    // If we try to place "HELLO" horizontally starting at (7, 7):
    // 'H' is at (7, 7), 'E' would be at (8, 7).
    // Column 8 would then contain "EX" (vertically).
    // Since "EX" is invalid, the move should be rejected.

    Character[] rack = { 'E', 'L', 'L', 'O' };
    List<PlayableWord> results = generator.getPlayableWordsList(board, rack, gaddag);

    boolean foundHello = results.stream().anyMatch(pw -> pw.getWord().equals("HELLO"));
    assertFalse(foundHello, "Should reject 'HELLO' because it creates the invalid cross-word 'EX'");
  }

  /**
   * Test that words extending off the board boundaries are not generated.
   */
  @Test
  void getPlayableWordsListShouldRespectBoardBoundaries() {
    gaddag.add("SCRABBLE");

    // Place 'S' at the very edge of the board (14, 7)
    board.getSquare(new Point(14, 7)).setTile(new Tile('S'));

    Character[] rack = { 'C', 'R', 'A', 'B', 'B', 'L', 'E' };
    List<PlayableWord> results = generator.getPlayableWordsList(board, rack, gaddag);

    // Horizontal placement is impossible (14 + 8 > 15)
    boolean foundHorizontal = results.stream().anyMatch(
        pw -> pw.getWord().equals("SCRABBLE") && pw.getDirection() == Direction.HORIZONTAL);

    assertFalse(foundHorizontal, "Moves going out of bounds should not be generated");
  }

  /**
   * Test that the generator returns an empty list if the rack contains no letters
   * that can extend
   * existing board tiles.
   */
  @Test
  void getPlayableWordsListShouldReturnEmptyWhenNoMovesPossible() {
    board.getSquare(new Point(7, 7)).setTile(new Tile('Z'));
    gaddag.add("APPLE");

    Character[] rack = { 'A', 'B', 'C' }; // No way to use 'Z' or form 'APPLE'
    List<PlayableWord> results = generator.getPlayableWordsList(board, rack, gaddag);

    assertTrue(results.isEmpty(),
        "Should return an empty list when no moves are possible with the current rack");
  }
}
