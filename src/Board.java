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

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public char[][] getBoard() {
        return board;
    }

    public void printBoard() {
        if (exitY == -1 && exitX >= 0 && exitX < cols) {
            for (int j = 0; j < cols; j++) {
                if (j == exitX) {
                    System.out.print("\u001B[32mK\u001B[0m");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }

        for (int i = 0; i < rows; i++) {
            if (exitX == -1 && exitY == i) {
                System.out.print("\u001B[32mK\u001B[0m");
            } else if (exitX == -1) {
                System.out.print(" ");
            }

            for (int j = 0; j < cols; j++) {
                char cell = board[i][j];
                if (cell == 'P') {
                    System.out.print("\u001B[31m" + cell + "\u001B[0m");
                } else if (cell == '.') {
                    System.out.print(cell);
                } else {
                    System.out.print("\u001B[34m" + cell + "\u001B[0m");
                }
            }

            if (exitX == cols && exitY == i) {
                System.out.print("\u001B[32mK\u001B[0m");
            }
            System.out.println();
        }

        if (exitY == rows && exitX >= 0 && exitX < cols) {
            for (int j = 0; j < cols; j++) {
                if (j == exitX) {
                    System.out.print("\u001B[32mK\u001B[0m");
                } else {
                    System.out.print(" ");
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
            line = br.readLine();  // dimensi
            if (line != null) {
                String[] dimensions = line.split(" ");
                try {
                    rows = Integer.parseInt(dimensions[0]);
                    cols = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid dimension format. Make sure its in valid format :)");
                }

                if (rows <= 0 || cols <= 0) {
                    throw new IllegalArgumentException("Invalid dimension values. Rows and columns must be positive integers :)");
                }
                
                List<String> inputLines = new ArrayList<>();
                int maxLength = 0;
                int kRow = -1, kCol = -1;
                
                // nonPieceCount
                line = br.readLine();
                if (line != null) {
                    nonPieceCount = Integer.parseInt(line);
                }
                
                int rowCount = 0;
                while ((line = br.readLine()) != null && rowCount < rows + 1) { // +1 untuk check K
                    inputLines.add(line);
                    maxLength = Math.max(maxLength, line.length());
                    
                    for (int i = 0; i < line.length(); i++) {
                        if (line.charAt(i) == 'K') {
                            kRow = rowCount;
                            kCol = i;
                        }
                    }
                    rowCount++;
                }
                
                board = new char[rows][cols];
                for (int i = 0; i < rows; i++) {
                    Arrays.fill(board[i], '.');
                }
                
                for (int i = 0; i < Math.min(inputLines.size(), rows); i++) {
                    String currentLine = inputLines.get(i);
                    for (int j = 0; j < Math.min(currentLine.length(), cols); j++) {
                        board[i][j] = currentLine.charAt(j);
                    }
                }
                
                if (kRow != -1 && kCol != -1) {
                    exitY = kRow;
                    exitX = kCol;
                }
            }

            boolean[][] isChecked = new boolean[rows][cols];
            nonPieceList.clear();

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!isChecked[i][j]) {
                        isChecked[i][j] = true;
                        char c = board[i][j];
                        
                        if (c == '.' || c == 'K') continue;

                        if (j + 1 < cols && board[i][j + 1] == c) {
                            int length = 1;
                            while (j + length < cols && board[i][j + length] == c) {
                                isChecked[i][j + length] = true;
                                length++;
                            }
                            Piece piece = new Piece(c, length, Piece.Orientation.HORIZONTAL, j, i);
                            if (c == mainChar) {
                                piece.setPrimary(true);
                                primaryPiece = piece;
                            } else {
                                nonPieceList.add(piece);
                            }
                        } 
                        else if (i + 1 < rows && board[i + 1][j] == c) {
                            int length = 1;
                            while (i + length < rows && board[i + length][j] == c) {
                                isChecked[i + length][j] = true;
                                length++;
                            }
                            Piece piece = new Piece(c, length, Piece.Orientation.VERTICAL, j, i);
                            if (c == mainChar) {
                                piece.setPrimary(true);
                                primaryPiece = piece;
                            } else {
                                nonPieceList.add(piece);
                            }
                        }
                        else if (c != '.') {
                            Piece piece = new Piece(c, 1, Piece.Orientation.HORIZONTAL, j, i);
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
            System.err.println("Error reading the file, make sure the file is in valid format :)");
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
        Board newBoard = new Board();
        newBoard.rows = this.rows;
        newBoard.cols = this.cols;
        newBoard.mainChar = this.mainChar;
        newBoard.nonPieceCount = this.nonPieceCount;
        newBoard.exitX = this.exitX;
        newBoard.exitY = this.exitY;

        newBoard.board = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                newBoard.board[i][j] = this.board[i][j];
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