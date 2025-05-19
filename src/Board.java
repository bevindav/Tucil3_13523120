import java.io.*;
import java.util.*;

public class Board {
    private char[][] board;
    private int rows;
    private int cols;
    private int nonPieceCount;
    private char mainChar;
    private int exitX;
    private int exitY;
    private List<Piece> nonPieceList = new ArrayList<>();
    private Piece primaryPiece;

    public Board(){
        this.rows = 0;
        this.cols = 0;
        this.mainChar = 'P';
        this.board = new char[rows][cols];
        this.nonPieceCount = 0;
    }

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.mainChar = 'P';
        this.board = new char[rows][cols];
        this.nonPieceCount = 0;
        for (int i = 0; i < rows; i++) {
            Arrays.fill(board[i], ' ');
        }
    }

    public Board(Board board) {
        this.rows = board.rows;
        this.cols = board.cols;
        this.mainChar = board.mainChar;
        this.nonPieceCount = board.nonPieceCount;
        this.board = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(board.board[i], 0, this.board[i], 0, cols);
        }
        this.exitX = board.exitX;
        this.exitY = board.exitY;
        this.nonPieceList = new ArrayList<>(board.nonPieceList);
        this.primaryPiece = board.primaryPiece;
    }

    public void setChar(int row, int col, char c) {
        if (isValid(row, col)) {
            board[row][col] = c;
        }
    }

    public char getChar(int row, int col) {
        return isValid(row, col) ? board[row][col] : ' ';
    }

    public void setMainChar(char c) {
        this.mainChar = c;
    }

    public char getMainChar() {
        return mainChar;
    }

    public void setNonPieceCount(int nonPieceCount) {
        this.nonPieceCount = nonPieceCount;
    }

    public int getNonPieceCount() {
        return nonPieceCount;
    }

    public Piece getPrimaryPiece() {
        return primaryPiece;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public char[][] getBoard() {
        return board;
    }

    public void printBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char cell = board[i][j];
                if (cell == 'P') {
                    // Primary piece (red car) - Print in red in terminal
                    System.out.print("\u001B[31m" + cell + "\u001B[0m ");  // Red color
                } else if (cell == 'K') {
                    // Exit - Print in green in terminal
                    System.out.print("\u001B[32m" + cell + "\u001B[0m ");  // Green color
                } else if (cell != '.') {
                    // Other pieces - Print in blue in terminal
                    System.out.print("\u001B[34m" + cell + "\u001B[0m ");  // Blue color
                } else {
                    // Empty spaces - Default color
                    System.out.print(cell + " ");
                }
            }
            System.out.println();
        }
    }

    public void clearBoard() {
        for (int i = 0; i < rows; i++) {
            Arrays.fill(board[i], ' ');
        }
    }

    public void readFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            line = br.readLine();  // Read dimensions
            if (line != null) {
                String[] dimensions = line.split(" ");
                rows = Integer.parseInt(dimensions[0]);
                cols = Integer.parseInt(dimensions[1]);
                board = new char[rows][cols];
            }

            line = br.readLine();  // Read nonPieceCount
            if (line != null) {
                nonPieceCount = Integer.parseInt(line);
            }

            char[][] tempBoard = new char[rows + 1][cols + 1];
            for (int i = 0; i < rows + 1; i++) {
                Arrays.fill(tempBoard[i], ' ');
            }

            int i = 0;
            while ((line = br.readLine()) != null && i < rows + 1) {
                for (int j = 0; j < cols + 1; j++) {
                    if (j < line.length()) {
                        tempBoard[i][j] = line.charAt(j);
                        if (line.charAt(j) == 'K') {
                            exitX = j;
                            exitY = i;
                            if (exitY == 0 && exitX < cols) exitY = -1;
                            else if (exitX == 0 && exitY < rows) exitX = -1;
                        }
                    }
                }
                i++;
            }

            List<Character> items = new ArrayList<>();
            for (int k = 0; k < rows + 1; k++) {
                for (int j = 0; j < tempBoard[k].length; j++) {
                    char c = tempBoard[k][j];
                    if (c != ' ' && c != 'K') items.add(c);
                }
            }

            for (int k = 0; k < rows; k++) {
                for (int j = 0; j < cols; j++) {
                    board[k][j] = items.get(k * cols + j);
                }
            }

            boolean[][] isChecked = new boolean[rows][cols];
            for (int k = 0; k < rows; k++) {
                for (int j = 0; j < cols; j++) {
                    if (!isChecked[k][j]) {
                        isChecked[k][j] = true;
                        char c = board[k][j];
                        if (c == '.') continue;

                        if (j + 1 < cols && board[k][j + 1] == c) {
                            int count = 1;
                            while (j + count < cols && board[k][j + count] == c) {
                                isChecked[k][j + count] = true;
                                count++;
                            }
                            Piece piece = new Piece(c, count, Piece.Orientation.HORIZONTAL, j, k);
                            if (c == mainChar) {
                                piece.setPrimary(true);
                                primaryPiece = piece;
                            } else {
                                nonPieceList.add(piece);
                            }
                        } else if (k + 1 < rows && board[k + 1][j] == c) {
                            int count = 1;
                            while (k + count < rows && board[k + count][j] == c) {
                                isChecked[k + count][j] = true;
                                count++;
                            }
                            Piece piece = new Piece(c, count, Piece.Orientation.VERTICAL, j, k);
                            if (c == mainChar) {
                                piece.setPrimary(true);
                                primaryPiece = piece;
                            } else {
                                nonPieceList.add(piece);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFull() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == ' ') return false;
            }
        }
        return true;
    }

    public Board copy() {
        Board newBoard = new Board(rows, cols);
        newBoard.setMainChar(mainChar);
        newBoard.setNonPieceCount(nonPieceCount);
        newBoard.setExitX(exitX);
        newBoard.setExitY(exitY);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                newBoard.setChar(i, j, board[i][j]);
            }
        }
        return newBoard;
    }

    public boolean isEmpty(int row, int col) {
        return isValid(row, col) && board[row][col] == '.';
    }

    public void saveToFile(String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    bw.write(board[i][j]);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValid(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public int getExitX() {
        return exitX;
    }

    public int getExitY() {
        return exitY;
    }

    public void setExitX(int exitX) {
        this.exitX = exitX;
    }

    public void setExitY(int exitY) {
        this.exitY = exitY;
    }

    public void addPiece(Piece piece) {
        nonPieceList.add(piece);
    }

    public List<Piece> getNonPieceList() {
        return nonPieceList;
    }

    public void setNonPieceList(List<Piece> nonPieceList) {
        this.nonPieceList = nonPieceList;
    }

    public void clearNonPieceList() {
        nonPieceList.clear();
    }

    public void removePiece(int index) {
        if (index >= 0 && index < nonPieceList.size()) {
            nonPieceList.remove(index);
        }
    }

    public void removePiece(char pieceChar) {
        nonPieceList.removeIf(piece -> piece.getPieceChar() == pieceChar);
    }

    public void printAllPieces() {
        for (Piece piece : nonPieceList) {
            piece.printPiece();
        }
    }

    public Map<Character, Piece> getPieceMap() {
        Map<Character, Piece> pieces = new HashMap<>();
        for (Piece piece : nonPieceList) {
            pieces.put(piece.getPieceChar(), piece);
        }
        pieces.put('P', primaryPiece);
        return pieces;
    }

    public String serializeBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(board[i][j]);
            }
        }
        return sb.toString();
    }
}
