package fr.ubordeaux.scrabble.model.dictionary.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.ubordeaux.scrabble.model.enums.SquareType;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoringTest {

  /**
   * Test that word score calculation correctly applies letter and word
   * multipliers only to newly
   * placed tiles, not to tiles already on the board.
   */
  @Test
  void calculateWordScoreShouldApplyLetterAndWordMultipliersOnlyOnNewTiles() {
    Square s1 = squareWithTile(0, 0, SquareType.DOUBLE_LETTER, 'A');
    Square s2 = squareWithTile(1, 0, SquareType.TRIPLE_WORD, 'C');
    Square s3 = squareWithTile(2, 0, SquareType.DOUBLE_WORD, 'D');

    int score = Scoring.calculateWordScore(List.of(s1, s2, s3), List.of(s1, s2));

    assertEquals(21, score);
  }

  /**
   * Test that placing all 7 tiles from the rack (a "bingo") adds a 50-point bonus
   * to the word
   * score.
   */
  @Test
  void calculateWordScoreShouldAddBingoBonusWhenSevenTilesPlaced() {
    List<Square> wordSquares = new ArrayList<>();
    for (int i = 0; i < Rack.MAX_SIZE; i++) {
      wordSquares.add(squareWithTile(i, 0, SquareType.NORMAL, 'A'));
    }

    int score = Scoring.calculateWordScore(wordSquares, wordSquares);

    assertEquals(57, score);
  }

  /**
   * Test that calculateWordScore validates its inputs and throws appropriate
   * exceptions for null
   * parameters, empty squares, or squares without tiles.
   */
  @Test
  void calculateWordScoreShouldValidateInputs() {
    Square filled = squareWithTile(0, 0, SquareType.NORMAL, 'A');
    final Square empty = new Square(new Point(1, 0), SquareType.NORMAL);

    assertThrows(NullPointerException.class, () -> Scoring.calculateWordScore(null, List.of()));
    assertThrows(NullPointerException.class,
        () -> Scoring.calculateWordScore(List.of(filled), null));
    assertThrows(IllegalArgumentException.class,
        () -> Scoring.calculateWordScore(List.of(), List.of()));
    assertThrows(IllegalArgumentException.class,
        () -> Scoring.calculateWordScore(List.of(empty), List.of(empty)));
  }

  /**
   * Test that the bingo bonus returns 50 points only when exactly 7 tiles (rack
   * maximum) are
   * placed, and 0 otherwise.
   */
  @Test
  void calculateBingoBonusShouldReturnFiftyOnlyForRackMaxSize() {
    assertEquals(50, Scoring.calculateBingoBonus(Rack.MAX_SIZE));
    assertEquals(0, Scoring.calculateBingoBonus(Rack.MAX_SIZE - 1));
  }

  private Square squareWithTile(int x, int y, SquareType type, char letter) {
    Square square = new Square(new Point(x, y), type);
    square.setTile(new Tile(letter));
    return square;
  }
}
