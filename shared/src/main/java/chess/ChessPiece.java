package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.ArrayList;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();

        if (type == PieceType.ROOK) {
            int[][] rookMoves = {
                    {1,0}, // up
                    {-1,0}, // down
                    {0,-1}, // left
                    {0,1}  // right
            };
            for (int[] move : rookMoves) {
                int myRow = myPosition.getRow();
                int myCol = myPosition.getColumn();
                int rowMove = move[0];
                int colMove = move[1];
                int newRow = rowMove + myRow;
                int newCol = colMove + myCol;
                while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece newPiece = board.getPiece(newPosition);
                    if (newPiece == null) {
                        pieceMoves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                        if (pieceColor != newPiece.getTeamColor()) {
                            pieceMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                    newRow = newRow + rowMove;
                    newCol = newCol + colMove;
                }
            }
        }

        return pieceMoves;
    }
}