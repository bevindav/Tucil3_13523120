public class Heuristic {
    public static final int MANHATTAN_DISTANCE = 0;
    public static final int BLOCKING_VEHICLES = 1;
    public static final int ADVANCED_BLOCKING = 2;
    public static final int COMBINED = 3;
    
    public static int calculate(State state, int heuristicType) {
        switch (heuristicType) {
            case MANHATTAN_DISTANCE:
                return calculateManhattanDistance(state);
            case BLOCKING_VEHICLES:
                return calculateBlockingVehicles(state);
            case ADVANCED_BLOCKING:
                return calculateAdvancedBlocking(state);
            case COMBINED:
                return calculateCombined(state);
            default:
                return calculateManhattanDistance(state); 
        }
    }
    
    private static int calculateManhattanDistance(State state) {
        Piece primaryPiece = state.getPieces('P');
        Board board = state.getBoard();
        int exitX = board.getExitX();
        int exitY = board.getExitY();
        
        if (primaryPiece.getOrientation() == Piece.Orientation.HORIZONTAL) {
            int rightEnd = primaryPiece.getX() + primaryPiece.getSize() - 1;
            int distance = 0;
            
            if (exitX > rightEnd) {
                distance = exitX - rightEnd;
            } else if (exitX == -1) { // Exit di kiri
                distance = primaryPiece.getX();
            }
            
            return distance;
        } else if (primaryPiece.getOrientation() == Piece.Orientation.VERTICAL) {
            int bottomEnd = primaryPiece.getY() + primaryPiece.getSize() - 1;
            int distance = 0;
            
            if (exitY > bottomEnd) {
                distance = exitY - bottomEnd;
            } else if (exitY == -1) { // Exit di atas
                distance = primaryPiece.getY();
            }
            
            return distance;
        }
        return 0;
    }
    
    private static int calculateBlockingVehicles(State state) {
        Piece primaryPiece = state.getPieces('P');
        Board board = state.getBoard();
        int count = 0;
        char[][] boardArray = board.getBoard();
        
        if (primaryPiece.getOrientation() == Piece.Orientation.HORIZONTAL) {
            int row = primaryPiece.getY();
            int rightEnd = primaryPiece.getX() + primaryPiece.getSize() - 1;
            int exitX = board.getExitX();
            
            // Check if exit is on the right
            if (exitX > rightEnd) {
                // Count blocking vehicles between car and right exit
                for (int col = rightEnd + 1; col < exitX; col++) {
                    if (boardArray[row][col] != '.') {
                        count++;
                    }
                }
            } 
            // Check if exit is on the left
            else if (exitX == -1) {
                // Count blocking vehicles between car and left exit
                for (int col = primaryPiece.getX() - 1; col >= 0; col--) {
                    if (boardArray[row][col] != '.') {
                        count++;
                    }
                }
            }
        } else if (primaryPiece.getOrientation() == Piece.Orientation.VERTICAL) {
            int col = primaryPiece.getX();
            int bottomEnd = primaryPiece.getY() + primaryPiece.getSize() - 1;
            int exitY = board.getExitY();
            
            // Check if exit is below
            if (exitY > bottomEnd) {
                // Count blocking vehicles between car and bottom exit
                for (int row = bottomEnd + 1; row < exitY; row++) {
                    if (boardArray[row][col] != '.') {
                        count++;
                    }
                }
            } 
            // Check if exit is above
            else if (exitY == -1) {
                // Count blocking vehicles between car and top exit
                for (int row = primaryPiece.getY() - 1; row >= 0; row--) {
                    if (boardArray[row][col] != '.') {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    private static int calculateAdvancedBlocking(State state) {
        // Implementasi lebih kompleks yang mempertimbangkan kesulitan memindahkan kendaraan
        int basicBlocking = calculateBlockingVehicles(state);
        int movabilityPenalty = 0;
        Board board = state.getBoard();
        char[][] boardArray = board.getBoard();
        Piece primaryPiece = state.getPieces('P');
        
        // Periksa "movability" kendaraan yang menghalangi
        if (primaryPiece.getOrientation() == Piece.Orientation.HORIZONTAL) {
            int row = primaryPiece.getY();
            int rightEnd = primaryPiece.getX() + primaryPiece.getSize() - 1;
            int exitX = board.getExitX();
            
            // Jika exit di kanan
            if (exitX > rightEnd) {
                for (int col = rightEnd + 1; col < exitX; col++) {
                    if (boardArray[row][col] != '.') {
                        char blockingPieceId = boardArray[row][col];
                        Piece blockingPiece = state.getPieces(blockingPieceId);
                        
                        // Jika kendaraan penghalang vertikal, periksa apakah sulit dipindahkan
                        if (blockingPiece != null && blockingPiece.getOrientation() == Piece.Orientation.VERTICAL) {
                            // Periksa apakah ada penghalang di atas atau bawah
                            int blockingRow = blockingPiece.getY();
                            int blockingHeight = blockingPiece.getSize();
                            
                            // Periksa penghalang di atas
                            boolean blockedAbove = blockingRow > 0 && boardArray[blockingRow - 1][col] != '.';
                            
                            // Periksa penghalang di bawah
                            boolean blockedBelow = blockingRow + blockingHeight < boardArray.length && 
                                                 boardArray[blockingRow + blockingHeight][col] != '.';
                                                 
                            if (blockedAbove && blockedBelow) {
                                movabilityPenalty += 3; // Sangat sulit dipindahkan
                            } else if (blockedAbove || blockedBelow) {
                                movabilityPenalty += 1; // Cukup sulit dipindahkan
                            }
                        }
                    }
                }
            }
        }
        
        return basicBlocking * 2 + movabilityPenalty;
    }
    
    private static int calculateCombined(State state) {
        // Kombinasi dari beberapa heuristic dengan bobot
        return calculateManhattanDistance(state) + 
               calculateBlockingVehicles(state) * 2 +
               (int)(Math.sqrt(calculateAdvancedBlocking(state)) * 1.5);
    }
}