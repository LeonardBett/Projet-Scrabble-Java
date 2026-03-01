package fr.u_bordeaux.scrabble.model.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoRedoTest {

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

    @Test
    void undoAndRedoShouldReturnNullWhenUnavailable() {
        UndoRedo undoRedo = new UndoRedo();

        assertNull(undoRedo.undo());
        assertNull(undoRedo.redo());
    }
}
