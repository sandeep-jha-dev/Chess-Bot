package chess.engine;

public class Boardd {
	private int[][] matrix;
	public boolean whiteToMove = true;
	public boolean whiteKingMoved = false;
	public boolean blackKingMoved = false;
	public boolean whiteRookL = false;
	public boolean whiteRookR = false;
	public boolean blackRookL = false;
	public boolean blackRookR = false;
	public long zobristKey;
	
	public int enPassantCol = -1;
		public Boardd() {
		this.matrix = new int[8][8];
		initializeBoard();
	    this.zobristKey = Zobrist.generateInitialKey(this, false);
	    }
		
	private void initializeBoard() {
		for(int i = 0; i<8; i++) {
			matrix[1][i] = 1;
			matrix[6][i] = -1;
		}
		
		int [] pieceOrder = {4,2,3,5,6,3,2,4};
		for(int i = 0; i<8;i++) {
			matrix[0][i] = pieceOrder[i];
			matrix[7][i] = -pieceOrder[i];
		}
	}
	public void makeMove(Move move) {
		// --- ZOBRIST INCREMENTAL UPDATE ---
		int startSquare = (move.startRow * 8) + move.startCol;
		int endSquare = (move.endRow * 8) + move.endCol;

		int movingPiece = matrix[move.startRow][move.startCol];
		int capturedPiece = matrix[move.endRow][move.endCol];

		int movingColor = (movingPiece < 0) ? 0 : 1; // 0 for White, 1 for Black
		int movingType = Math.abs(movingPiece);

		// 1. LIFT THE PIECE: XOR out the moving piece from its starting square
		this.zobristKey ^= Zobrist.pieceKeys[movingColor][movingType][startSquare];

		// 2. DELETE THE CAPTURE: If a piece is being eaten, XOR it out of existence
		if (capturedPiece != 0) {
		    int capturedColor = (capturedPiece < 0) ? 0 : 1;
		    int capturedType = Math.abs(capturedPiece);
		    this.zobristKey ^= Zobrist.pieceKeys[capturedColor][capturedType][endSquare];
		}

		// 3. PLACE THE PIECE: XOR the moving piece into its new destination square
		this.zobristKey ^= Zobrist.pieceKeys[movingColor][movingType][endSquare];

		// 4. FLIP THE CLOCK: Toggle the side to move
		this.zobristKey ^= Zobrist.blackMoveKey;

		int piece = matrix[move.startRow][move.startCol];
		if (Math.abs(piece) == 1 && (move.endRow == 0 || move.endRow == 7)) {
			piece = (piece > 0) ? 5 : -5;
		}
		
		if(move.isEnPassant) {
			int dir = (piece > 0) ? 1 : -1;
			matrix[move.endRow - dir][move.endCol]= 0; 
		}
		if(move.isCastling) {
			if(move.endCol == 6) {
				matrix[move.startRow][5] = matrix[move.startRow][7];
				matrix[move.startRow][7] = 0;
			}
			else if(move.endCol == 2) {
				matrix[move.startRow][3] = matrix[move.startRow][0];
				matrix[move.startRow][0] = 0;
			}
		}
		updateHistory(piece, move.startRow, move.startCol);
		if(Math.abs(piece) == 1 && Math.abs(move.startRow - move.endRow) == 2) {
			enPassantCol = move.startCol;
		}
		else {
			enPassantCol = -1;
		}
		matrix[move.endRow][move.endCol] = piece;
		matrix[move.startRow][move.startCol] = 0;
	}
	
	public void undoMove(Move move, int capturedPiece, int originalMovedPiece, int prevEnPassant, long prevZobristKey) {
	    int piece = originalMovedPiece;

	    // 1. Physically move back
	    matrix[move.startRow][move.startCol] = piece;
	    matrix[move.endRow][move.endCol] = capturedPiece;
	    
	    this.enPassantCol = prevEnPassant;
	    this.zobristKey = prevZobristKey;

	    // 2. IMPORTANT: Reset Movement Flags
	    // We only reset them if the piece is back at its home square
	    if (piece == 6 && move.startRow == 0 && move.startCol == 4) whiteKingMoved = false;
	    if (piece == -6 && move.startRow == 7 && move.startCol == 4) blackKingMoved = false;
	    
	    if (piece == 4) { // White Rook
	        if (move.startCol == 0) whiteRookL = false;
	        if (move.startCol == 7) whiteRookR = false;
	    }
	    if (piece == -4) { // Black Rook
	        if (move.startCol == 0) blackRookL = false;
	        if (move.startCol == 7) blackRookR = false;
	    }

	    // 3. Undo special move side-effects
	    if (move.isCastling) {
	        if (move.endCol == 6) { // Kingside
	            matrix[move.startRow][7] = matrix[move.startRow][5];
	            matrix[move.startRow][5] = 0;
	        } else if (move.endCol == 2) { // Queenside
	            matrix[move.startRow][0] = matrix[move.startRow][3];
	            matrix[move.startRow][3] = 0;
	        }
	    }
	    
	    if (move.isEnPassant) {
	        int dir = (piece > 0) ? 1 : -1;
	        matrix[move.endRow - dir][move.endCol] = (piece > 0) ? -1 : 1;
	    }
	}
	
	private void updateHistory(int piece, int r, int c) {
		if (piece == 6) whiteKingMoved = true;
        if (piece == -6) blackKingMoved = true;
        if (r == 0 && c == 0) whiteRookL = true;
        if (r == 0 && c == 7) whiteRookR = true;
        if (r == 7 && c == 0) blackRookL = true;
        if (r == 7 && c == 7) blackRookR = true;
	}
	
	public boolean isWhiteToMove() {
		return whiteToMove;
	}
	public void switchTurn() {
		whiteToMove = !whiteToMove;
	}
	public int[][] getMatrix(){
		return matrix;
	}
	public void applyUCIMove(int startRow, int startCol, int endRow, int endCol, char promotion) {
	    Move incomingMove = new Move(startRow, startCol, endRow, endCol);
	    int piece = matrix[startRow][startCol];

	    // 1. Detect Castling from CuteChess
	    if (Math.abs(piece) == 6 && Math.abs(startCol - endCol) == 2) {
	        incomingMove.isCastling = true;
	    }
	    
	    // 2. Detect En Passant from CuteChess
	    if (Math.abs(piece) == 1 && startCol != endCol && matrix[endRow][endCol] == 0) {
	        incomingMove.isEnPassant = true;
	    }

	    // Forcefully apply the move
	    this.makeMove(incomingMove);

	    // 3. Handle Underpromotion from CuteChess
	    if (promotion == 'q' || promotion == 'r' || promotion == 'b' || promotion == 'n') {
	        int promoPiece = 5; // Default Queen
	        if (promotion == 'r') promoPiece = 4;
	        else if (promotion == 'b') promoPiece = 3;
	        else if (promotion == 'n') promoPiece = 2;
	        matrix[endRow][endCol] = (piece > 0) ? promoPiece : -promoPiece;
	    }

	    this.whiteToMove = !this.whiteToMove; 
	}
}