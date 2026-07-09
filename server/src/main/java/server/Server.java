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
import dataaccess.DataAccessException;
import java.util.Map;
import service.RegisterRequest;
import service.BadRequestException;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import service.AlreadyTakenException;




public class Server {
    // data stores for server
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    // Services for app logic
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    // Gson to convert between JSON & Java
    private final Gson gson;
    // Javalin for HTTP server / routing
    private final Javalin javalin;

    public Server() {
        // memory DAO group
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        // services connect to DOAs
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        gson = new Gson();

        // jav serves web files
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // http endpoints
        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                ctx.status(200).json(Map.of());
            } catch (DataAccessException error) {
                ctx.status(500).json(Map.of("message", "Error: " + error.getMessage()));
            }

        });

        javalin.post("/user", ctx -> {
            try {
                RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
                ctx.status(200).json(userService.register(request));
            } catch (BadRequestException error) {ctx.status(400).json(Map.of("message", error.getMessage()));
            } catch (DataAccessException error) {ctx.status(500).json(Map.of("message", "Error: " + error.getMessage()));
            } catch (AlreadyTakenException error) {ctx.status(403).json(Map.of("message", error.getMessage()));
            }
        });
    }
    // start & stop server
    public int run(int targetPort) {
        javalin.start(targetPort);
        return javalin.port();
    }
    public void stop() {
        javalin.stop();
    }
}