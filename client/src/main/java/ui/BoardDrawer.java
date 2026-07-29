package ui;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import static ui.EscapeSequences.*;

public class BoardDrawer {

    public void draw(ChessBoard board, String side) {
        letters(side);
        if (side.equals("BLACK")) {
            for (int row = 1; row <= 8; row++) row(board, row, side);
        } else {
            for (int row = 8; row >= 1; row--) row(board, row, side);
        }
        letters(side);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void letters(String side) {
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
        if (side.equals("BLACK")) System.out.println("    h   g   f  e   d  c   b  a ");
        else System.out.println("    a   b   c  d   e  f   g   h");
    }

    private void row(ChessBoard board, int row, String side) {
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR + " " + row + " ");
        if (side.equals("BLACK")) {
            for (int col = 8; col >= 1; col--) square(board, row, col);
        } else {
            for (int col = 1; col <= 8; col++) square(board, row, col);
        }
        System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR + " " + row);
    }

    private void square(ChessBoard board, int row, int col) {
        if ((row + col) % 2 == 0) System.out.print(SET_BG_COLOR_DARK_GREEN);
        else System.out.print(SET_BG_COLOR_LIGHT_GREY);
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            System.out.print(RESET_TEXT_COLOR + EMPTY);
            return;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) System.out.print(SET_TEXT_COLOR_RED);
        else System.out.print(SET_TEXT_COLOR_BLUE);
        System.out.print(piece(piece));
    }

    private String piece(ChessPiece piece) {
        boolean white = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        return switch (piece.getPieceType()) {
            case KING -> white ? WHITE_KING : BLACK_KING;
            case QUEEN -> white ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> white ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> white ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> white ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> white ? WHITE_PAWN : BLACK_PAWN;
        };
    }
}