package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void clearSuccess() {
        Assertions.assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    public void clearFail() {
        ServerFacade badFacade = new ServerFacade(1);
        Assertions.assertThrows(ResponseException.class, () -> badFacade.clear());
    }

    @Test
    public void registerSuccess() throws ResponseException {
        var result = facade.register("player1", "password", "email");
        Assertions.assertEquals("player1", result.username());
        Assertions.assertFalse(result.authToken().isEmpty());
    }

    @Test
    public void registerFail() throws ResponseException {
        facade.register("player1", "password", "email");
        Assertions.assertThrows(ResponseException.class, () -> facade.register("player1",
                                                                               "password",
                                                                                  "email"));
    }

    @Test
    public void loginSuccess() throws ResponseException {
        facade.register("player1", "password", "email");
        var result = facade.login("player1", "password");
        Assertions.assertEquals("player1", result.username());
        Assertions.assertFalse(result.authToken().isEmpty());
    }

    @Test
    public void loginFail() throws ResponseException {
        facade.register("player1", "password", "email");
        Assertions.assertThrows(ResponseException.class, () -> facade.login("player1",
                                                                            "wrongpassword"));
    }

    @Test
    public void logoutSuccess() throws ResponseException {
        var result = facade.register("player1", "password", "email");
        Assertions.assertDoesNotThrow(() -> facade.logout(result.authToken()));
    }

    @Test
    public void logoutFail() {
        Assertions.assertThrows(ResponseException.class, () -> facade.logout("bad-token"));
    }

    @Test
    public void createGameSuccess() throws ResponseException {
        var user = facade.register("player1", "password", "email");
        int gameID = facade.createGame(user.authToken(), "My Game");
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    public void createGameFail() {
        Assertions.assertThrows(ResponseException.class,
                () -> facade.createGame("bad-token", "My Game"));
    }

    @Test
    public void listGamesSuccess() throws ResponseException {
        var user = facade.register("player1", "password", "email");
        facade.createGame(user.authToken(), "Game One");
        facade.createGame(user.authToken(), "Game Two");
        var games = facade.listGames(user.authToken());
        Assertions.assertEquals(2, games.size());
    }

    @Test
    public void listGamesFail() {
        Assertions.assertThrows(ResponseException.class,
                () -> facade.listGames("bad-token"));
    }

    @Test
    public void joinGameSuccess() throws ResponseException {
        var user = facade.register("player1", "password", "email");
        int gameID = facade.createGame(user.authToken(), "My Game");
        facade.joinGame(user.authToken(), "WHITE", gameID);
        var games = facade.listGames(user.authToken());
        Assertions.assertEquals("player1", games.get(0).whiteUsername());
    }

    @Test
    public void joinGameFail() throws ResponseException {
        var first = facade.register("player1", "password", "email1");
        int gameID = facade.createGame(first.authToken(), "My Game");
        facade.joinGame(first.authToken(), "WHITE", gameID);
        var second = facade.register("player2", "password", "email2");
        Assertions.assertThrows(ResponseException.class,
                () -> facade.joinGame(second.authToken(), "WHITE", gameID));
        }
}
