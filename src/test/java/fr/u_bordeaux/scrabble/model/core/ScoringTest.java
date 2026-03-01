package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.utils.Point;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoringTest {

    @Test
    void calculateWordScoreShouldApplyLetterAndWordMultipliersOnlyOnNewTiles() {
        Square s1 = squareWithTile(0, 0, SquareType.DOUBLE_LETTER, 'A');
        Square s2 = squareWithTile(1, 0, SquareType.TRIPLE_WORD, 'C');
        Square s3 = squareWithTile(2, 0, SquareType.DOUBLE_WORD, 'D');

        int score = Scoring.calculateWordScore(List.of(s1, s2, s3), List.of(s1, s2));

        assertEquals(21, score);
    }

    @Test
    void calculateWordScoreShouldAddBingoBonusWhenSevenTilesPlaced() {
        List<Square> wordSquares = new ArrayList<>();
        for (int i = 0; i < Rack.MAX_SIZE; i++) {
            wordSquares.add(squareWithTile(i, 0, SquareType.NORMAL, 'A'));
        }

        int score = Scoring.calculateWordScore(wordSquares, wordSquares);

        assertEquals(57, score);
    }

    @Test
    void calculateWordScoreShouldValidateInputs() {
        Square filled = squareWithTile(0, 0, SquareType.NORMAL, 'A');
        Square empty = new Square(new Point(1, 0), SquareType.NORMAL);

        assertThrows(NullPointerException.class, () -> Scoring.calculateWordScore(null, List.of()));
        assertThrows(NullPointerException.class, () -> Scoring.calculateWordScore(List.of(filled), null));
        assertThrows(IllegalArgumentException.class, () -> Scoring.calculateWordScore(List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> Scoring.calculateWordScore(List.of(empty), List.of(empty)));
    }

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
