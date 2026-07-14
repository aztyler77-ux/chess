package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;

import model.AuthData;
import model.GameData;

import org.junit.jupiter.api.Test;
import service.exception.AlreadyTakenException;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameServiceTest {
    @Test
    public void createGameSuccess() throws DataAccessException, BadRequestException, UnauthorizedException {
        // fake data
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token", "player1"));
        GameService gameService = new GameService(gameDAO, authDAO);
        CreateGameRequest request = new CreateGameRequest("token", "test game");

        // create game
        CreateGameResult result = gameService.createGame(request);

        // check
        GameData game = gameDAO.getGame(result.gameID());
        assertNotNull(game);
        assertEquals("test game", game.gameName());
    }

    @Test
    public void createGameFail() throws DataAccessException {
        // fake data
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token", "player1"));
        GameService gameService = new GameService(gameDAO, authDAO);

        // failure = empty game name
        CreateGameRequest request = new CreateGameRequest("token", "");
        BadRequestException error = assertThrows(BadRequestException.class, () -> gameService.createGame(request));

        // check
        assertEquals("Error: bad request", error.getMessage());
        assertEquals(0, gameDAO.listGames().size());
    }

    @Test
    public void listGamesSuccess() throws DataAccessException, UnauthorizedException {
        // fake data
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token", "player1"));
        gameDAO.createGame("test game");
        GameService gameService = new GameService(gameDAO, authDAO);
        ListGamesRequest request = new ListGamesRequest("token");

        // list games
        ListGamesResult result = gameService.listGames(request);

        // check
        assertEquals(1, result.games().size());
    }

    @Test
    public void listGamesFail() throws DataAccessException {
        // fake data
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token", "player1"));
        gameDAO.createGame("test game");
        GameService gameService = new GameService(gameDAO, authDAO);

        // failure = token doesn't exist
        ListGamesRequest request = new ListGamesRequest("wrong token");
        UnauthorizedException error = assertThrows(UnauthorizedException.class, () -> gameService.listGames(request));

        // check
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    public void joinGameSuccess() throws DataAccessException, BadRequestException, UnauthorizedException, AlreadyTakenException {
        // fake data
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token", "player1"));
        int gameID = gameDAO.createGame("test game");
        GameService gameService = new GameService(gameDAO, authDAO);
        JoinGameRequest request = new JoinGameRequest("token", "WHITE", gameID);

        // join game
        gameService.joinGame(request);

        // check
        GameData game = gameDAO.getGame(gameID);
        assertEquals("player1", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    public void joinGameFail() throws DataAccessException, BadRequestException, UnauthorizedException, AlreadyTakenException {
        // fake data
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("player1 token", "player1"));
        authDAO.createAuth(new AuthData("player2 token", "player2"));
        int gameID = gameDAO.createGame("test game");
        GameService gameService = new GameService(gameDAO, authDAO);

        JoinGameRequest firstRequest = new JoinGameRequest("player1 token", "WHITE", gameID);
        gameService.joinGame(firstRequest);

        // failure = white taken
        JoinGameRequest secondRequest = new JoinGameRequest("player2 token", "WHITE", gameID);
        AlreadyTakenException error = assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(secondRequest));

        // check
        assertEquals("Error: already taken", error.getMessage());
        assertEquals("player1", gameDAO.getGame(gameID).whiteUsername());
    }
}