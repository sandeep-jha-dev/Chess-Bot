package chess.engine;

import java.util.ArrayList;
import java.util.List;

public class MoveGen {

    private static final int[][] KNIGHT_OFFSETS = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    private static final int[][] ROOK_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] BISHOP_DIRS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] QUEEN_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public static List<Move> getLegalMoves(Boardd b, int r, int c, boolean isWhite) {
        List<Move> moves = new ArrayList<>();
        int[][] matrix = b.getMatrix();
        int piece = Math.abs(matrix[r][c]);

        switch (piece) {
            case 1: addPawnMoves(b, r, c, isWhite, moves); break;
            case 2: addLeaperMoves(matrix, r, c, isWhite, moves, KNIGHT_OFFSETS); break;
            case 3: addSlidingMoves(matrix, r, c, isWhite, moves, BISHOP_DIRS); break;
            case 4: addSlidingMoves(matrix, r, c, isWhite, moves, ROOK_DIRS); break;
            case 5: addSlidingMoves(matrix, r, c, isWhite, moves, QUEEN_DIRS); break;
            case 6: addLeaperMoves(matrix, r, c, isWhite, moves, QUEEN_DIRS); 
            addCastlingMoves(b, r, c, isWhite, moves);
            break;
        }
        return moves;
    }

    private static void addPawnMoves(Boardd b, int r, int c, boolean isWhite, List<Move> moves) {
    	int[][] matrix = b.getMatrix();
        int dir = isWhite ? 1 : -1;
        int startRank = isWhite ? 1 : 6;
        int forwardOneRow = r + dir;
        if(isSafe(forwardOneRow, c) && matrix[forwardOneRow][c] == 0) {
        	moves.add(new Move(r, c, forwardOneRow, c));
        	if(r == startRank && matrix[r+2*dir][c] == 0) {
        		moves.add(new Move(r, c, r+2*dir, c));
        	}
        }
        int[] cols = {c-1,c+1};
        for(int nc : cols) {
        if (isSafe(r + dir, nc)) {
        	int target = matrix[r+dir][nc];
        	if(target != 0 && (isWhite ? target < 0 : target > 0)) {
        		moves.add(new Move(r, c, r+dir, nc));
        	}
        	if(target == 0 && nc == b.enPassantCol && r == (isWhite ? 4 : 3)) {
        		Move ep = new Move(r, c, r+dir, nc);
        		ep.isEnPassant = true;
        		moves.add(ep);
        	}
        }
      }
    }
    private static void addCastlingMoves(Boardd b, int r, int c, boolean isWhite, List<Move> moves) {
    	int[][] matrix = b.getMatrix();
    	boolean moved = isWhite ? b.whiteKingMoved : b.blackKingMoved;
    	if(moved || isSquareAttacked(matrix, r, c, !isWhite)) {
    		return;
    	}
    	boolean rMoved = isWhite ? b.whiteRookR : b.blackRookR;
    	if(!rMoved && matrix[r][5] == 0 && matrix[r][6] == 0) {
    		if(!isSquareAttacked(matrix, r, 5, !isWhite) && !isSquareAttacked(matrix, r, 6, !isWhite)) {
    		Move m = new Move(r, c, r, 6);
    		m.isCastling = true;
    		moves.add(m);
    	}
    }
    	boolean lMoved = isWhite ? b.whiteRookL : b.blackRookL;
    	if(!lMoved && matrix[r][1] == 0 && matrix[r][2] == 0 && matrix[r][3] == 0) {
    		if(!isSquareAttacked(matrix, r, 3, !isWhite) && !isSquareAttacked(matrix, r, 2, !isWhite)) {
    			Move m = new Move(r, c, r, 2);
    			m.isCastling = true;
    			moves.add(m);
    		}
    	}
    	// Inside addCastlingMoves
    	if (moved) {
    	    System.out.println("Castling failed: King has already moved!");
    	    return;
    	}
    	if (isSquareAttacked(matrix, r, c, !isWhite)) {
    	    System.out.println("Castling failed: King is in check!");
    	    return;
    	}
    }
    
    private static void addSlidingMoves(int[][] board, int r, int c, boolean isWhite, List<Move> moves, int[][] dirs) {
        for (int[] d : dirs) {
            int tr = r + d[0], tc = c + d[1];
            while (isSafe(tr, tc)) {
            	if(board[tr][tc] == 0) {
            		moves.add(new Move(r, c, tr, tc));
            	}
            	else {
            		if(isWhite ? board[tr][tc] < 0 : board[tr][tc] > 0) {
            			moves.add(new Move(r, c, tr, tc));
            		}
            		break;
            	}
            	tr += d[0];
            	tc += d[1];
            }
        }
    }

    private static void addLeaperMoves(int[][] board, int r, int c, boolean isWhite, List<Move> moves, int[][] offsets) {
        for (int[] o : offsets) {
            int tr = r + o[0], tc = c + o[1];
            if (isSafe(tr, tc)) {
                int target = board[tr][tc];
                if (target == 0 || (isWhite ? target < 0 : target > 0)) moves.add(new Move(r, c, tr, tc));
            }
        }
    }

    private static boolean isSafe(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }
    
    public static boolean isSquareAttacked(int[][] board, int r, int c, boolean whiteAttacker) {
    	int pDir = whiteAttacker ? -1 : 1;
    	int attackerPawn = whiteAttacker ? 1 : -1;
    	if (isSafe(r+pDir, c-1) && board[r+pDir][c-1] == attackerPawn) return true;
    	if (isSafe(r+pDir, c+1) && board[r+pDir][c+1] == attackerPawn) return true;
    	int attackerKnight = whiteAttacker ? 2 : -2;
    	
    	for (int[] o : KNIGHT_OFFSETS) {
    		if(isSafe(r+o[0], c+o[1]) && board[r+o[0]][c+o[1]] == attackerKnight) return true;
    	}
    	if(checkSlidingAttacker(board, r, c, whiteAttacker, ROOK_DIRS, 4, 5)) return true;
    	if(checkSlidingAttacker(board, r, c, whiteAttacker, BISHOP_DIRS, 3, 5)) return true;
    	int attackerKing = whiteAttacker ? 6 : -6;
    	
    	for(int[] d : QUEEN_DIRS) {
    		if(isSafe(r+d[0], c+d[1]) && board[r+d[0]][c+d[1]] == attackerKing) return true;
    	}
    	return false;
    }
    
    private static boolean checkSlidingAttacker(int[][] board, int r, int c, boolean whiteAttacking, int[][] dirs, int pType, int qType) {
    	for(int[] d : dirs) {
    		int tr = r+d[0], tc = c+d[1];
    		while(isSafe(tr, tc)) {
    			int piece = board[tr][tc];
    			if(piece != 0) {
    				if(whiteAttacking ? (piece == pType || piece == qType) : (piece == -pType || piece == -qType)) return true;
    				break;
    			}
    			tr += d[0];
    			tc += d[1];
    		}
    	}
    	return false;
    }
    
    public static List<Move> getAllLegalMoves(Boardd b, boolean isWhite) {
        List<Move> allMoves = new ArrayList<>();
        int[][] matrix = b.getMatrix();
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int piece = matrix[r][c];
                // If the piece belongs to the player whose turn it is
                if (piece != 0 && (isWhite ? piece > 0 : piece < 0)) {
                    allMoves.addAll(getStrictlyLegalMoves(b, r, c, isWhite));
                }
            }
        }
        return allMoves;
    }
    
    public static List<Move> getStrictlyLegalMoves(Boardd b, int r, int c, boolean isWhite){
        List<Move> pseudoMoves = getLegalMoves(b, r, c, isWhite);
        List<Move> strictlyLegal = new ArrayList<>();

        for(Move m : pseudoMoves) {
            int originalPiece = b.getMatrix()[m.startRow][m.startCol];
            int capturedPiece = b.getMatrix()[m.endRow][m.endCol];
            int prevEP = b.enPassantCol;
            long prevKey = b.zobristKey;
            
            // Use the REAL move mechanics so Castling and En Passant calculate perfectly
            b.makeMove(m); 

            int[][] matrix = b.getMatrix();
            int kr = -1, kc = -1;
            for (int i = 0; i<8; i++) {
                for(int j = 0; j<8; j++) {
                    if(matrix[i][j] == (isWhite ? 6 : -6)) {
                        kr = i;
                        kc = j;
                        break;
                    }
                }
            }

            // Check if the King is safe
            if(!isSquareAttacked(matrix, kr, kc, !isWhite)) {
                strictlyLegal.add(m);
            }

            // Undo the REAL move
            b.undoMove(m, capturedPiece, originalPiece, prevEP, prevKey); 
        }
        return strictlyLegal;
    }
    
}