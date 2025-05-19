public class Piece {
    private char pieceChar;
    private int size;
    
    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    private Orientation orientation;
    private int x; // x and y start from the top-left corner
    private int y;
    private boolean isPrimary;

    public Piece(char pieceChar, int size, Orientation orientation, int x, int y) {
        this.pieceChar = pieceChar;
        this.size = size;
        this.orientation = orientation;
        this.x = x;
        this.y = y;
        this.isPrimary = false;
    }

    public char getPieceChar() {
        return pieceChar;
    }

    public int getSize() {
        return size;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public void setPieceChar(char pieceChar) {
        this.pieceChar = pieceChar;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void moveX(int deltaX) {
        if (orientation == Orientation.HORIZONTAL) {
            this.x += deltaX;
        } else {
            throw new UnsupportedOperationException("Cannot move X for vertical piece");
        }
    }

    public void moveY(int deltaY) {
        if (orientation == Orientation.VERTICAL) {
            this.y += deltaY;
        } else {
            throw new UnsupportedOperationException("Cannot move Y for horizontal piece");
        }
    }

    // Prints the piece in the board representation
    public void printPiece() {
        if (orientation == Orientation.HORIZONTAL) {
            for (int i = 0; i < size; i++) {
                System.out.print(pieceChar);
            }
            System.out.println();
        } else {
            for (int i = 0; i < size; i++) {
                System.out.println(pieceChar);
            }
        }
    }

    public Piece copy() {
        Piece newPiece = new Piece(this.pieceChar, this.size, this.orientation, this.x, this.y);
        newPiece.setPrimary(this.isPrimary);
        return newPiece;
    }
}