package ui;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.List;
import static ui.EscapeSequences.*;

public class BoardDrawer {

    public void draw(ChessBoard board, String side) {
        draw(board, side, null);
    }

    public void draw(ChessBoard board, String side, List<ChessPosition> highlights) {
        letters(side);
        if (side.equals("BLACK")) {
            for (int row = 1; row <= 8; row++) {
                row(board, row, side, highlights);
            }
        } else {
            for (int row = 8; row >= 1; row--) {
                row(board, row, side, highlights);
            }
        }
        letters(side);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void letters(String side) {
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
        if (side.equals("BLACK")) {
            System.out.println("    h   g   f  e   d  c   b  a ");
        } else {
            System.out.println("    a   b   c  d   e  f   g   h");
        }
    }

    private void row(ChessBoard board, int row, String side, List<ChessPosition> highlights) {
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR + " " + row + " ");
        if (side.equals("BLACK")) {
            for (int col = 8; col >= 1; col--) {
                square(board, row, col, highlights);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                square(board, row, col, highlights);
            }
        }
        System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR + " " + row);
    }

    private void square(ChessBoard board, int row, int col, List<ChessPosition> highlights) {
        ChessPosition position = new ChessPosition(row, col);
        if (highlights != null && highlights.contains(position)) {
            System.out.print(SET_BG_COLOR_YELLOW);
        } else if ((row + col) % 2 == 0) {
            System.out.print(SET_BG_COLOR_DARK_GREEN);
        } else {
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
        }
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            System.out.print(RESET_TEXT_COLOR + EMPTY);
            return;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            System.out.print(SET_TEXT_COLOR_RED);
        } else {
            System.out.print(SET_TEXT_COLOR_BLUE);
        }
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