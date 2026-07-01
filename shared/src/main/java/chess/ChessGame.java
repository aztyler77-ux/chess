package chess;

import java.util.Collection;
import java.util.ArrayList;

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
        /*
        Goal: Return a list containing the legal moves for a friendly piece on the given startPosition
        Get the pieceMoves of the piece at startPosition
        Create a copy of the board and apply each move, checking if it puts the King in check
        If it doesn’t, add it to legalMoves
         */
        Collection<ChessMove> legalMoves = new ArrayList<>();

        if (startPosition == null) {
            return null;}

        ChessPiece currentPiece = board.getPiece(startPosition);

        if (currentPiece == null) {
            return null;
        }

        Collection<ChessMove> pieceMoves = currentPiece.pieceMoves(board, startPosition);

        for (ChessMove move : pieceMoves) {
            ChessBoard testBoard = new ChessBoard();
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition thisPosition = new ChessPosition(row, col);
                    ChessPiece thisPiece = board.getPiece(thisPosition);
                    if (thisPiece != null) {
                        testBoard.addPiece(new ChessPosition(row, col), new ChessPiece(thisPiece.getTeamColor(), thisPiece.getPieceType()));
                    }
                }
            }

            testBoard.addPiece(move.getStartPosition(), null);
            if (move.getPromotionPiece() != null) {
                testBoard.addPiece(move.getEndPosition(), new ChessPiece(currentPiece.getTeamColor(), move.getPromotionPiece()));
            } else {
                testBoard.addPiece(move.getEndPosition(), currentPiece);
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
        if (move == null ||
            move.getStartPosition() == null ||
            board.getPiece(move.getStartPosition()) == null) {
            throw new InvalidMoveException();
        }

        ChessPiece currentPiece = board.getPiece(move.getStartPosition());

        if (currentPiece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> pieceLegalMoves = validMoves(move.getStartPosition());

        if (pieceLegalMoves.contains(move)) {
            board.addPiece(move.getStartPosition(), null);
            if (move.getPromotionPiece() != null) {
                board.addPiece(move.getEndPosition(), new ChessPiece(currentPiece.getTeamColor(), move.getPromotionPiece()));
            } else {
                board.addPiece(move.getEndPosition(), currentPiece);
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

    public boolean isInCheck(TeamColor teamColor) {
        /*
        Goal: Return whether the King is in Check
        Locate and mark a position for teamColor’s king on the board
        Get the list of pieceMoves for all enemy pieces
        If any of those moves end on the marked king position, return true
        Otherwise, return false
         */
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() == teamColor) {
                        if (currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                            kingPosition = currentPosition;
                        }
                    }
                }
            }
        }
        if (kingPosition == null) {
            throw new IllegalStateException(teamColor + " king was not found!");
        } else {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition currentPosition = new ChessPosition(row, col);
                    ChessPiece currentPiece = board.getPiece(currentPosition);
                    if (currentPiece != null) {
                        if (currentPiece.getTeamColor() != teamColor) {
                            Collection<ChessMove> enemyMoves = currentPiece.pieceMoves(board, currentPosition);
                            for (ChessMove move : enemyMoves) {
                                if (move.getEndPosition().equals(kingPosition)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
        /*
        Goal: Return whether the team is in checkmate (no valid moves)
        Loop through teamColor’s pieces and check validMoves
        If any piece has at least one item, return false
        Otherwise, return true
         */
    }

    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
        /*
        Goal: Return whether there is a stalemate (no valid moves and not in check)
        Same logic as isInCheckmate, but teamColor must not be in check
        Loop through teamColor’s pieces and check validMoves
        If any piece has at least one item, return false
        Otherwise, return true
         */
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }
}
