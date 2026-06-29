package chess;

import java.util.Collection;

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
        throw new RuntimeException("Not implemented");
        /*
        Goal: Return a list containing the legal moves for a friendly piece on the given startPosition
        Get the pieceMoves of the piece at startPosition
        Create a copy of the board and apply each move, checking if it puts the King in check
        If it doesn’t, add it to legalMoves
         */
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
        /*
        Goal: Apply the legal move then change the turn
        Get the piece at the startPosition of the move, then apply it to the original board
        Change teamTurn
         */
    }

    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
        /*
        Goal: Return whether the King is in Check
        Locate and mark a position for teamColor’s king on the board
        Get the list of pieceMoves for all enemy pieces
        If any of those moves end on the marked king position, return true
        Otherwise, return false
         */
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
