package chess;

public class ChessBoard {
    private ChessPiece[][] board;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        int myRow = position.getRow() - 1;
        int myCol = position.getColumn() - 1;

        board[myRow][myCol] = piece;
    }

    public ChessPiece getPiece(ChessPosition position) {
        int myRow = position.getRow() - 1;
        int myCol = position.getColumn() - 1;

        return board[myRow][myCol];
    }

    public void resetBoard() {
        throw new RuntimeException("Not implemented");
    }
}
