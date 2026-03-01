package fr.u_bordeaux.scrabble.model.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TileTest {

    @Test
    void constructorShouldAssignCharacterAndStandardValue() {
        Tile tile = new Tile('a');

        assertEquals('a', tile.getCharacter());
        assertEquals(1, tile.getValue());
    }

    @Test
    void getStandardValueShouldReturnExpectedScores() {
        assertEquals(1, Tile.getStandardValue('E'));
        assertEquals(2, Tile.getStandardValue('D'));
        assertEquals(3, Tile.getStandardValue('C'));
        assertEquals(4, Tile.getStandardValue('F'));
        assertEquals(8, Tile.getStandardValue('J'));
        assertEquals(10, Tile.getStandardValue('Z'));
        assertEquals(0, Tile.getStandardValue(' '));
        assertEquals(0, Tile.getStandardValue('?'));
    }

    @Test
    void equalsAndHashCodeShouldUseCharacterAndValue() {
        Tile first = new Tile('K');
        Tile same = new Tile('K');
        Tile different = new Tile('L');

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
    }

    @Test
    void toStringShouldReturnCharacterAsString() {
        Tile tile = new Tile('Q');

        assertEquals("Q", tile.toString());
    }
}