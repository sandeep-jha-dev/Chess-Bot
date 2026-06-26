package chess.engine;

public class PST {
    // 1. PAWN: Heavily rewards advancing towards the promotion rank (Row 0).
    // Penalizes leaving the center pawns unmoved, and highly rewards controlling the center.
    private static final int[][] PAWN = {
        {  0,  0,  0,  0,  0,  0,  0,  0}, // Row 0: Promotion rank
        { 50, 50, 50, 50, 50, 50, 50, 50}, // Row 1: One step from Queen
        { 10, 10, 20, 30, 30, 20, 10, 10}, // Row 2
        {  5,  5, 10, 25, 25, 10,  5,  5}, // Row 3
        {  0,  0,  0, 20, 20,  0,  0,  0}, // Row 4
        {  5, -5,-10,  0,  0,-10, -5,  5}, // Row 5: Slight penalty for moving edge pawns early
        {  5, 10, 10,-20,-20, 10, 10,  5}, // Row 6: Start rank (Center pawns should move!)
        {  0,  0,  0,  0,  0,  0,  0,  0}  // Row 7: Back rank
    };

    // 2. KNIGHT: The classic "Knights on the rim are dim."
    // Massive penalties for corners and edges, huge bonuses for the deep center.
    private static final int[][] KNIGHT = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    // 3. BISHOP: Rewards long diagonals and center control. Penalizes being trapped on the edges.
    private static final int[][] BISHOP = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    // 4. ROOK: Wants to be on the 7th rank (Row 1 for White) and centralized on the back rank.
    private static final int[][] ROOK = {
        {  0,  0,  0,  0,  0,  0,  0,  0},
        {  5, 10, 10, 10, 10, 10, 10,  5}, // The absolute ideal rank for a Rook
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        {  0,  0,  0,  5,  5,  0,  0,  0}  // Back rank, preferring the center files
    };

    // 5. QUEEN: Heavily penalized for early aggressive center placement.
    // She wants to be developed slightly, but kept safely behind the pawn structure.
    private static final int[][] QUEEN = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        { -5,  0,  5,  5,  5,  5,  0, -5},
        {  0,  0,  5,  5,  5,  5,  0, -5},
        {-10,  5,  5,  5,  5,  5,  0,-10},
        {-10,  0,  5,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20}
    };

    // 6. KING (Middle Game): Needs to hide. Extreme penalty for leaving the back rank.
    // Massive bonus for castling behind the G or B pawns.
    private static final int[][] KING_MID = {
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-20,-30,-30,-40,-40,-30,-30,-20},
        {-10,-20,-20,-20,-20,-20,-20,-10},
        { 20, 20,  0,  0,  0,  0, 20, 20},
        { 20, 30, 10,  0,  0, 10, 30, 20}  // Heavy reward for King safety in corners
    };

 // 7. KING (Endgame): Queens are gone, danger is low.
    // The King becomes a highly active attacking piece. Massive rewards for centralization.
    private static final int[][] KING_END = {
        {-50,-30,-30,-30,-30,-30,-30,-50},
        {-30,-30,  0,  0,  0,  0,-30,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-20,-10,  0,  0,-10,-20,-30},
        {-50,-40,-30,-20,-20,-30,-40,-50}
    };
    /**
     * The Universal Lookup Function.
     * It takes the raw piece type, its coordinates, and its color.
     * If the piece is Black, it mathematically flips the board to read the White table upside down.
     */
 // Update the method signature to include 'boolean isEndgame'
    public static int getScore(int pieceType, int r, int c, boolean isWhite, boolean isEndgame) {
        int tableRow = isWhite ? r : (7 - r);
        
        switch (pieceType) {
            case 1: return PAWN[tableRow][c];
            case 2: return KNIGHT[tableRow][c];
            case 3: return BISHOP[tableRow][c];
            case 4: return ROOK[tableRow][c];
            case 5: return QUEEN[tableRow][c];
            // The dynamic swap!
            case 6: return isEndgame ? KING_END[tableRow][c] : KING_MID[tableRow][c];
            default: return 0;
        }
    }
}