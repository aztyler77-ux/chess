package dataaccess;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLGameDAOTest {
    private MySQLGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseSetup.createTables();
        gameDAO = new MySQLGameDAO();
        gameDAO.clear();
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        int gameID = gameDAO.createGame("cool game");

        GameData result = gameDAO.getGame(gameID);

        assertTrue(gameID > 0);
        assertNotNull(result);
        assertEquals("cool game", result.gameName());
        assertNull(result.whiteUsername());
        assertNull(result.blackUsername());
        assertEquals(new ChessGame(), result.game());
    }

    @Test
    public void createGameFail() {
        // game name cant be null
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        int gameID = gameDAO.createGame("test game");

        GameData result = gameDAO.getGame(gameID);

        assertEquals(gameID, result.gameID());
        assertEquals("test game", result.gameName());
        assertNotNull(result.game());
    }

    @Test
    public void getGameFail() throws DataAccessException {
        GameData result = gameDAO.getGame(999999);

        assertNull(result);
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        gameDAO.createGame("game one");
        gameDAO.createGame("game two");

        Collection<GameData> games = gameDAO.listGames();

        assertEquals(2, games.size());
    }

    @Test
    public void listGamesFail() throws DataAccessException {
        // no games to list
        Collection<GameData> games = gameDAO.listGames();

        assertTrue(games.isEmpty());
    }

    @Test
    public void updateGameSuccess() throws DataAccessException, InvalidMoveException {
        int gameID = gameDAO.createGame("test game");
        GameData oldGame = gameDAO.getGame(gameID);

        ChessGame chessGame = oldGame.game();
        ChessMove move = new ChessMove(
                new ChessPosition(2, 1),
                new ChessPosition(4, 1),
                null
        );
        chessGame.makeMove(move);

        GameData changedGame = new GameData(
                gameID,
                "white player",
                "black player",
                "test game",
                chessGame
        );

        gameDAO.updateGame(changedGame);
        GameData result = gameDAO.getGame(gameID);

        assertEquals(changedGame, result);
        assertEquals(ChessGame.TeamColor.BLACK, result.game().getTeamTurn());
        assertNull(result.game().getBoard().getPiece(new ChessPosition(2, 1)));
        assertNotNull(result.game().getBoard().getPiece(new ChessPosition(4, 1)));
    }

    @Test
    public void updateGameFail() {
        GameData missingGame = new GameData(
                999999,
                "white player",
                null,
                "not real",
                new ChessGame()
        );

        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(missingGame));
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        gameDAO.createGame("game one");
        gameDAO.createGame("game two");

        gameDAO.clear();

        assertTrue(gameDAO.listGames().isEmpty());
    }
}