package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.interfaces.Player;

/**
 * Représente l'état global d'une partie de Scrabble.
 * Gère le tour par tour, les scores et la logique principale du jeu.
 */
public class Game {
    private final Board board;
    private final Player[] players;

    public Game(Board board, Player[] players) {
        this.board = board;
        this.players = players;
    }

}
