package fr.u_bordeaux.scrabble.model.core;

import java.util.Stack;

/**
 * Manages the history of moves for undo/redo functionality.
 * Only HumanPlayers can initiate undo/redo.
 */
public class UndoRedo {
    private final Stack<Move> history;
    private final Stack<Move> redoStack;

    public UndoRedo() {
        this.history = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Adds a move to the history.
     * Clears the redo stack because a new branch of history is created.
     *
     * @param move The move to add.
     */
    public void addMove(Move move) {
        history.push(move);
        redoStack.clear();
    }

    /**
     * Checks if undo is possible.
     *
     * @return true if there is at least one move in history.
     */
    public boolean canUndo() {
        return !history.isEmpty();
    }

    /**
     * Checks if redo is possible.
     *
     * @return true if there is at least one move in the redo stack.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Pops the last move from history and pushes it to the redo stack.
     *
     * @return The move to undo, or null if history is empty.
     */
    public Move undo() {
        if (!canUndo()) {
            return null;
        }
        Move move = history.pop();
        redoStack.push(move);
        return move;
    }

    /**
     * Pops the last move from the redo stack and pushes it back to history.
     *
     * @return The move to redo, or null if redo stack is empty.
     */
    public Move redo() {
        if (!canRedo()) {
            return null;
        }
        Move move = redoStack.pop();
        history.push(move);
        return move;
    }

    public Stack<Move> getHistory() {
        return history;
    }
}