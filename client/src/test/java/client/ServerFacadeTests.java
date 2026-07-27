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

}
