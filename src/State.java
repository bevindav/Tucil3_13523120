import java.io.*;
import java.util.*;

public class State implements Comparable<State> {
    private Map<Character, Piece> pieces; // Map of vehicle IDs to Piece objects
    private Board board;
    private int cost;
    private State parent; // To track the solution path
    private String move;
    private int heuristic; // For A* search

    public State(Map<Character, Piece> pieces, Board board, int cost, State parent, String move) {
        this.pieces = pieces;
        this.board = board;
        this.cost = cost;
        this.parent = parent;
        this.move = move;
        this.heuristic = computeHeuristic();
    }

    public int getCost() {
        return cost;
    }

    public Board getBoard() {
        return board;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public String getMove() {
        return move;
    }

    public State getParent() {
        return parent;
    }

    public void setParent(State parent) {
        this.parent = parent;
    }

    // Compute heuristic for A* search
    private int computeHeuristic() {
        Piece xCar = pieces.get('P');
        if (xCar == null) return 0;

        int count = 0;
        if (xCar.getOrientation() == Piece.Orientation.HORIZONTAL) {
            int row = xCar.getY();
            if (board.getExitX() == -1) {
                // Exit on the left side
                int leftCol = xCar.getX();
                for (int c = leftCol - 1; c >= 0; c--) {
                    if (board.getBoard()[row][c] != '.') count++;
                }
            } else {
                // Exit on the right side
                int rightEnd = xCar.getX() + xCar.getSize() - 1;
                for (int c = rightEnd + 1; c < board.getBoard()[0].length; c++) {
                    if (board.getBoard()[row][c] != '.') count++;
                }
            }
        } else {
            int col = xCar.getX();
            if (board.getExitY() == -1) {
                // Exit on the top
                int topRow = xCar.getY();
                for (int r = topRow - 1; r >= 0; r--) {
                    if (board.getBoard()[r][col] != '.') count++;
                }
            } else {
                // Exit on the bottom
                int bottomEnd = xCar.getY() + xCar.getSize() - 1;
                for (int r = bottomEnd + 1; r < board.getBoard().length; r++) {
                    if (board.getBoard()[r][col] != '.') count++;
                }
            }
        }
        return count;
    }

    // Compare states based on cost (UCS)
    @Override
    public int compareTo(State other) {
        return Integer.compare(this.cost, other.cost);
    }

    // Check if the red car has reached the exit
    public boolean isGoal() {
        Piece redCar = pieces.get('P');
        if (redCar == null) return false;

        if (redCar.getOrientation() == Piece.Orientation.HORIZONTAL) {
            int leftCol = redCar.getX();
            int rightEndCol = redCar.getX() + redCar.getSize() - 1;
            int row = redCar.getY();

            if (board.getExitX() == -1) {
                // Exit on the left, check if the leftmost part of the car is at the exit
                return board.getExitY() == row && leftCol == board.getExitX();
            } else {
                // Exit on the right
                return board.getExitY() == row && rightEndCol == board.getExitX();
            }
        } else {
            int topRow = redCar.getY();
            int bottomEndRow = redCar.getY() + redCar.getSize() - 1;
            int col = redCar.getX();

            if (board.getExitY() == -1) {
                // Exit on the top
                return board.getExitX() == col && topRow == board.getExitY();
            } else {
                // Exit on the bottom
                return board.getExitX() == col && bottomEndRow == board.getExitY();
            }
        }
    }

    public List<State> getNextStates() {
        List<State> nextStates = new ArrayList<>();
        for (Piece p : pieces.values()) {
            // Try both directions: forward (+1) and backward (-1)
            for (int direction : new int[]{1, -1}) {
                int steps = 1;
                while (true) {
                    if (!canMove(p, direction, steps)) break;
                    State newState = moveVehicle(p, direction * steps);
                    if (newState != null) nextStates.add(newState);
                    steps++;
                }
            }
        }
        return nextStates;
    }

    private boolean canMove(Piece p, int direction, int steps) {
        if (p.getOrientation() == Piece.Orientation.HORIZONTAL) {
            int row = p.getY();
            if (direction == -1) {
                for (int i = 1; i <= steps; i++) {
                    int col = p.getX() - i;
                    if (col < 0) {
                        if (col == -1) {
                            if (i == steps) {
                                return board.getExitY() == row && board.getExitX() == -1;
                            } else return false;
                        }
                        return false;
                    }
                    if (board.getBoard()[row][col] != '.') return false;
                }
            } else {
                int rightEnd = p.getX() + p.getSize() - 1;
                for (int i = 1; i <= steps; i++) {
                    int col = rightEnd + i;
                    if (col >= board.getBoard()[0].length) {
                        if (col == board.getBoard()[0].length && i == steps) {
                            return board.getExitY() == row && board.getExitX() == col;
                        } else return false;
                    }
                    if (col < board.getBoard()[0].length && board.getBoard()[row][col] != '.') return false;
                }
            }
        } else {
            int col = p.getX();
            if (direction == -1) {
                for (int i = 1; i <= steps; i++) {
                    int row = p.getY() - i;
                    if (row < 0) {
                        if (row == -1 && i == steps) {
                            return board.getExitX() == col && board.getExitY() == -1;
                        } else return false;
                    }
                    if (board.getBoard()[row][col] != '.') return false;
                }
            } else {
                int bottomEnd = p.getY() + p.getSize() - 1;
                for (int i = 1; i <= steps; i++) {
                    int row = bottomEnd + i;
                    if (row >= board.getBoard().length) {
                        if (row == board.getBoard().length && i == steps) {
                            return board.getExitX() == col && board.getExitY() == row;
                        } else return false;
                    }
                    if (row < board.getBoard().length && board.getBoard()[row][col] != '.') return false;
                }
            }
        }
        return true;
    }

    private State moveVehicle(Piece p, int move) {
        // Copy the vehicle and its map
        Map<Character, Piece> newPieces = new HashMap<>();
        for (Map.Entry<Character, Piece> entry : pieces.entrySet()) {
            newPieces.put(entry.getKey(), entry.getValue().copy());
        }

        Piece movedPiece = newPieces.get(p.getPieceChar());

        // Update the position of the moved vehicle
        if (movedPiece.getOrientation() == Piece.Orientation.HORIZONTAL) {
            movedPiece.setX(movedPiece.getX() + move); // Horizontal movement (left/right)
        } else {
            movedPiece.setY(movedPiece.getY() + move); // Vertical movement (up/down)
        }

        // Create a new empty board
        char[][] newBoard = new char[board.getBoard().length][board.getBoard()[0].length];
        for (int i = 0; i < newBoard.length; i++) {
            Arrays.fill(newBoard[i], '.');
        }

        // Update the positions of all the pieces on the new board
        for (Piece piece : newPieces.values()) {
            int r = piece.getY();
            int c = piece.getX();
            for (int i = 0; i < piece.getSize(); i++) {
                if (piece.getOrientation() == Piece.Orientation.HORIZONTAL) {
                    int cc = c + i;
                    if (r >= 0 && r < newBoard.length && cc >= 0 && cc < newBoard[0].length) {
                        newBoard[r][cc] = piece.getPieceChar();
                    }
                } else {
                    int rr = r + i;
                    if (rr >= 0 && rr < newBoard.length && c >= 0 && c < newBoard[0].length) {
                        newBoard[rr][c] = piece.getPieceChar();
                    }
                }
            }
        }

        // Calculate the new cost (1 per move)
        int newCost = this.cost + 1;
        Board newBoardCopy = board.copy();
        newBoardCopy.setBoard(newBoard);

        // Determine the move direction and return the new state
        String direction = (movedPiece.getOrientation() == Piece.Orientation.HORIZONTAL)
                ? (move > 0 ? "right" : "left")
                : (move > 0 ? "down" : "up");

        return new State(newPieces, newBoardCopy, newCost, this, "Move " + p.getPieceChar() + " to " + direction);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State other = (State) o;

        if (pieces.size() != other.pieces.size()) return false;

        for (char id : pieces.keySet()) {
            Piece v1 = pieces.get(id);
            Piece v2 = other.pieces.get(id);
            if (v2 == null) return false;
            if (v1.getX() != v2.getX() || v1.getY() != v2.getY()) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (Piece v : pieces.values()) {
            result = 31 * result + v.getPieceChar();
            result = 31 * result + v.getY();
            result = 31 * result + v.getX();
        }
        return result;
    }

    @Override
    public String toString() {
        // ANSI escape codes untuk warna
        final String RESET = "\033[0m"; // Reset warna
        final String RED = "\033[31m";  // Warna merah untuk primary car
        final String GREEN = "\033[32m"; // Warna hijau untuk exit (K)
        final String BLUE = "\033[34m";  // Warna biru untuk titik kosong

        StringBuilder sb = new StringBuilder();
        sb.append("\n=====================================\n");
        sb.append("  Rush Hour Solver [Jakarta Edition]\n");
        sb.append("=====================================\n\n");

        int exitX = this.getBoard().getExitX();
        int exitY = this.getBoard().getExitY();

        char[][] board = this.getBoard().getBoard();
        int rows = board.length;
        int cols = board[0].length;

        // Pastikan kita mencetak board dengan warna sesuai dengan posisi exit dan primary piece
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (x == exitX && y == exitY) {
                    sb.append(GREEN + " K " + RESET);  // Highlight exit as 'K'
                } else {
                    char piece = board[y][x];
                    if (piece == 'P') {
                        sb.append(RED + " P " + RESET);  // Highlight primary car
                    } else if (piece == '.') {
                        sb.append(BLUE + " . " + RESET);  // Highlight empty spaces
                    } else {
                        sb.append(" " + piece + " ");  // Default piece
                    }
                }
            }
            sb.append("\n");
        }

        sb.append("\n=====================================\n");
        return sb.toString();
    }

    public void saveSolutionToFile(int nodeCount, long executionTime) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the filename to save the solution (ex: test\\test.txt):");
        String filename;
        try {
            filename = reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        List<State> path = new ArrayList<>();
        State current = this;
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        Collections.reverse(path);

        // Saving the solution to a file without color codes
        try (PrintWriter writer = new PrintWriter(filename)) {
            for (State state : path) {
                writer.println("Move: " + state.getMove());
                writer.println(state.toStringWithoutColor());  // Save the plain text version (without color)
            }
            writer.println("Visited nodes: " + nodeCount);
            writer.println("Execution time: " + executionTime + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toStringWithoutColor() {
        int exitX = this.getBoard().getExitX();
        int exitY = this.getBoard().getExitY();

        char[][] board = this.getBoard().getBoard();
        int rows = board.length;
        int cols = board[0].length;

        int minRow = Math.min(0, exitY);
        int maxRow = Math.max(rows - 1, exitY);
        int minCol = Math.min(0, exitX);
        int maxCol = Math.max(cols - 1, exitX);

        StringBuilder sb = new StringBuilder();

        for (int y = minRow; y <= maxRow; y++) {
            for (int x = minCol; x <= maxCol; x++) {
                if (x == exitX && y == exitY) {
                    sb.append("K");  // Exit position labeled 'K'
                } else {
                    if (y >= 0 && y < rows && x >= 0 && x < cols) {
                        sb.append(board[y][x]);  // Regular board display without color codes
                    } else {
                        sb.append(" ");  // Empty spaces outside the board
                    }
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public void saveNoSolutionToFile(int nodeCount, long executionTime) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the filename to save the solution (ex: test\\test.txt):");
        String filename;
        try {
            filename = reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("No solution found.");
            writer.println("Visited nodes: " + nodeCount);
            writer.println("Execution time: " + executionTime + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printSolution() {
        if (parent != null) {
            parent.printSolution();
        }
        System.out.println(move);
        board.printBoard();
        System.out.println();
    }

    public List<State> getPath() {
        List<State> path = new ArrayList<>();
        State current = this;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}