package fr.u_bordeaux.scrabble.model.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoRedoTest {

    /**
     * Test that the undo/redo mechanism correctly manages history stacks,
     * allowing moves to be undone and redone in proper order.
     */
    @Test
    void undoAndRedoShouldFollowHistoryStacks() {
        UndoRedo undoRedo = new UndoRedo();
        Move move = Move.createPass(new HumanPlayer("Alice"));

        assertFalse(undoRedo.canUndo());
        assertFalse(undoRedo.canRedo());

        undoRedo.addMove(move);
        assertTrue(undoRedo.canUndo());

        Move undone = undoRedo.undo();
        assertEquals(move, undone);
        assertFalse(undoRedo.canUndo());
        assertTrue(undoRedo.canRedo());

        Move redone = undoRedo.redo();
        assertEquals(move, redone);
        assertTrue(undoRedo.canUndo());
    }

    /**
     * Test that adding a new move after undoing clears the redo stack,
     * preventing invalid redo operations on an alternate history branch.
     */
    @Test
    void addMoveShouldClearRedoStack() {
        UndoRedo undoRedo = new UndoRedo();
        Move first = Move.createPass(new HumanPlayer("Alice"));
        Move second = Move.createPass(new HumanPlayer("Bob"));

        undoRedo.addMove(first);
        undoRedo.undo();
        assertTrue(undoRedo.canRedo());

        undoRedo.addMove(second);
        assertFalse(undoRedo.canRedo());
    }

    /**
     * Test that undo and redo operations return null when there are
     * no moves available to undo or redo.
     */
    @Test
    void undoAndRedoShouldReturnNullWhenUnavailable() {
        UndoRedo undoRedo = new UndoRedo();

        assertNull(undoRedo.undo());
        assertNull(undoRedo.redo());
    }
}
