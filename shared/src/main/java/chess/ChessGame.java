package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> legalMoves = new ArrayList<>();

        if (startPosition == null) {
            return null;
        }

        ChessPiece currentPiece = board.getPiece(startPosition);

        if (currentPiece == null) {
            return null;
        }

        Collection<ChessMove> pieceMoves =
                currentPiece.pieceMoves(board, startPosition);

        for (ChessMove move : pieceMoves) {
            ChessBoard testBoard = new ChessBoard();

            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition thisPosition =
                            new ChessPosition(row, col);
                    ChessPiece thisPiece =
                            board.getPiece(thisPosition);

                    if (thisPiece != null) {
                        testBoard.addPiece(
                                new ChessPosition(row, col),
                                new ChessPiece(
                                        thisPiece.getTeamColor(),
                                        thisPiece.getPieceType()));
                    }
                }
            }

            testBoard.addPiece(move.getStartPosition(), null);

            if (move.getPromotionPiece() != null) {
                testBoard.addPiece(
                        move.getEndPosition(),
                        new ChessPiece(
                                currentPiece.getTeamColor(),
                                move.getPromotionPiece()));
            } else {
                testBoard.addPiece(
                        move.getEndPosition(),
                        currentPiece);
            }

            ChessBoard saveOriginal = board;
            board = testBoard;

            if (!isInCheck(currentPiece.getTeamColor())) {
                legalMoves.add(move);
            }

            board = saveOriginal;
        }

        return legalMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null
                || move.getEndPosition() == null
                || move.getStartPosition() == null
                || board.getPiece(move.getStartPosition()) == null) {
            throw new InvalidMoveException();
        }

        ChessPiece currentPiece =
                board.getPiece(move.getStartPosition());

        if (currentPiece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> pieceLegalMoves =
                validMoves(move.getStartPosition());

        if (pieceLegalMoves.contains(move)) {
            board.addPiece(move.getStartPosition(), null);

            if (move.getPromotionPiece() != null) {
                board.addPiece(
                        move.getEndPosition(),
                        new ChessPiece(
                                currentPiece.getTeamColor(),
                                move.getPromotionPiece()));
            } else {
                board.addPiece(
                        move.getEndPosition(),
                        currentPiece);
            }

            if (teamTurn == ChessGame.TeamColor.WHITE) {
                teamTurn = ChessGame.TeamColor.BLACK;
            } else {
                teamTurn = ChessGame.TeamColor.WHITE;
            }
        } else {
            throw new InvalidMoveException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChessGame chessGame = (ChessGame) o;

        return Objects.equals(board, chessGame.board)
                && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition =
                        new ChessPosition(row, col);
                ChessPiece currentPiece =
                        board.getPiece(currentPosition);

                if (currentPiece != null
                        && currentPiece.getTeamColor() == teamColor
                        && currentPiece.getPieceType()
                        == ChessPiece.PieceType.KING) {
                    kingPosition = currentPosition;
                }
            }
        }

        if (kingPosition == null) {
            throw new IllegalStateException(
                    teamColor + " king was not found!");
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition =
                        new ChessPosition(row, col);
                ChessPiece currentPiece =
                        board.getPiece(currentPosition);

                if (currentPiece != null
                        && currentPiece.getTeamColor() != teamColor
                        && pieceAttacksPosition(
                        currentPiece,
                        currentPosition,
                        kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pieceAttacksPosition(
            ChessPiece piece,
            ChessPosition piecePosition,
            ChessPosition targetPosition) {

        Collection<ChessMove> moves =
                piece.pieceMoves(board, piecePosition);

        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(targetPosition)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return !hasLegalMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return !hasLegalMoves(teamColor);
    }

    private boolean hasLegalMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition =
                        new ChessPosition(row, col);
                ChessPiece currentPiece =
                        board.getPiece(currentPosition);

                if (currentPiece != null
                        && currentPiece.getTeamColor() == teamColor) {
                    Collection<ChessMove> legalMoves =
                            validMoves(currentPosition);

                    if (legalMoves != null
                            && !legalMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }
}