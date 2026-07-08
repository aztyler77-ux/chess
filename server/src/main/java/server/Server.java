package server;
import io.javalin.Javalin;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {
    private final GameService gameService;
    private final UserDAO userDAO;
    private final ClearService clearService;
    private final AuthDAO authDAO;
    private final UserService userService;
    private final GameDAO gameDAO;
    private final Javalin javalin;

    public Server() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // endpoints will  be registered here later
    }

    public int run(int targetPort) {
        javalin.start(targetPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}