package chess.engine;

import java.security.SecureRandom;

public class Zobrist {
    // 1. The Piece Keys: [Color (0=White, 1=Black)][Piece Type (1-6)][Square (0-63)]
    public static final long[][][] pieceKeys = new long[2][7][64];
    
    // 2. The Game State Keys
    public static final long blackMoveKey;
    public static final long[] castlingKeys = new long[16]; // 4 booleans = 16 possible states
    public static final long[] enPassantKeys = new long[8]; // 8 possible files for an EP capture

    // Static initializer block runs exactly once when the engine starts
    static {
        SecureRandom random = new SecureRandom();

        // 1. Fill the piece table with random 64-bit longs
        for (int color = 0; color < 2; color++) {
            for (int pieceType = 1; pieceType <= 6; pieceType++) {
                for (int square = 0; square < 64; square++) {
                    pieceKeys[color][pieceType][square] = random.nextLong();
                }
            }
        }

        // 2. Assign the side-to-move key
        blackMoveKey = random.nextLong();

        // 3. Assign the castling rights keys
        for (int i = 0; i < 16; i++) {
            castlingKeys[i] = random.nextLong();
        }

        // 4. Assign the En Passant file keys
        for (int i = 0; i < 8; i++) {
            enPassantKeys[i] = random.nextLong();
        }
    }

    /**
     * Calculates the full 64-bit fingerprint of a completely fresh board.
     * We will only run this expensive loop ONCE per game.
     */
    public static long generateInitialKey(Boardd board, boolean isBlackToMove) {
        long finalKey = 0L;
        int[][] matrix = board.getMatrix();

        // 1. XOR all the pieces currently on the board
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int piece = matrix[r][c];
                if (piece != 0) {
                    int colorIndex = (piece < 0) ? 0 : 1; // Negative is White
                    int pieceType = Math.abs(piece);
                    int squareIndex = (r * 8) + c; // Flatten 2D coords to 0-63
                    
                    finalKey ^= pieceKeys[colorIndex][pieceType][squareIndex];
                }
            }
        }

        // 2. XOR the side to move
        if (isBlackToMove) {
            finalKey ^= blackMoveKey;
        }

        // 3. XOR the En Passant file (if it exists)
        if (board.enPassantCol != -1) {
            finalKey ^= enPassantKeys[board.enPassantCol];
        }

     // 4. XOR the Castling Rights
        // Translating your specific King/Rook movement trackers into the 16-state castling integer
        int castleState = 0;
        
        if (!board.whiteKingMoved && !board.whiteRookR) castleState |= 1;
        if (!board.whiteKingMoved && !board.whiteRookL) castleState |= 2;
        if (!board.blackKingMoved && !board.blackRookR) castleState |= 4;
        if (!board.blackKingMoved && !board.blackRookL) castleState |= 8;
        
        finalKey ^= castlingKeys[castleState];
        
        finalKey ^= castlingKeys[castleState];

        return finalKey;
    }
}