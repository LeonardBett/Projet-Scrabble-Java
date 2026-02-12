package fr.u_bordeaux.scrabble.view.cli;

import fr.u_bordeaux.scrabble.model.core.Board;
import fr.u_bordeaux.scrabble.model.core.Square;
import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.utils.Point;


public class BoardRenderer {
    
  
    private static final String RESET = "\u001B[0m";
    private static final String BG_CYAN = "\u001B[46m";           // Lettre compte double
    private static final String BG_BLUE = "\u001B[44m";           // Lettre compte triple
    private static final String BG_RED = "\u001B[41m";            // Mot compte double
    private static final String BG_MAGENTA = "\u001B[45m";        // Mot compte triple
    private static final String BG_DEFAULT = "\u001B[40m";        // Case normale (noir)
    
 
    private static final String WHITE_TEXT = "\u001B[37m";
    
    /**
     * Renders the game board with color highlights.
     */
    public void render(Board board, boolean showBonusSquares) {
        renderColumnHeaders();
        renderRows(board, showBonusSquares);
        System.out.println();
    }
    

    private void renderColumnHeaders() {
        System.out.print("  "); 
        for (int col = 1; col <= Board.SIZE; col++) {
            System.out.printf("%2d ", col);
        }
        System.out.println();
    }
    

    private void renderRows(Board board, boolean showBonusSquares) {
        for (int row = 0; row < Board.SIZE; row++) {
        
            char rowLabel = (char) ('a' + row);
            System.out.print(rowLabel + " ");
            
            for (int col = 0; col < Board.SIZE; col++) {
                Square square = board.getSquare(new Point(col, row));
                renderSquare(square, showBonusSquares);
            }
            
            System.out.println(RESET);  
        }
    }
    

    private void renderSquare(Square square, boolean showBonusSquares) {
        String bgColor = getBonusColor(square.getSquareType(), showBonusSquares);
        
        if (!square.isEmpty()) {
        
            char letter = Character.toUpperCase(square.getTile().getCharacter());
            System.out.print(bgColor + WHITE_TEXT + " " + letter + " " + RESET);
        } else {
           
            System.out.print(bgColor + WHITE_TEXT + " _ " + RESET);
        }
    }
    

    private String getBonusColor(SquareType type, boolean showBonusSquares) {
        if (!showBonusSquares) {
            return BG_DEFAULT;  
        }
        
        return switch (type) {
            case DOUBLE_LETTER -> BG_CYAN;      
            case TRIPLE_LETTER -> BG_BLUE;      
            case DOUBLE_WORD -> BG_RED;        
            case TRIPLE_WORD -> BG_MAGENTA;    
            default -> BG_DEFAULT;               
        };
    }
    

    
}