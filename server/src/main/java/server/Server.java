package server;
import io.javalin.Javalin;
import io.javalin.http.Context;
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
import com.google.gson.Gson;
import service.AlreadyTakenException;
import service.LoginRequest;
import service.UnauthorizedException;
import service.LogoutRequest;
import service.ListGamesRequest;
import service.CreateGameRequest;
import service.JoinGameRequest;

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
        // clear all
        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                sendJson(ctx, 200, Map.of());
            } catch (DataAccessException error) {
                sendJson(ctx, 500, Map.of("message", "Error: " + error.getMessage()));
            }

        });

        // register
        javalin.post("/user", ctx -> {
            try {
                RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
                sendJson(ctx, 200, userService.register(request));}
            catch (BadRequestException error) {sendJson(ctx, 400, Map.of("message", error.getMessage()));}
            catch (DataAccessException error) {sendJson(ctx, 500, Map.of("message", "Error: " + error.getMessage()));}
            catch (AlreadyTakenException error) {sendJson(ctx, 403, Map.of("message", error.getMessage()));}
        });

        // login
        javalin.post("/session", ctx -> {
            try {
                LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
                sendJson(ctx, 200, userService.login(request));}
            catch (BadRequestException error) {sendJson(ctx, 400, Map.of("message", error.getMessage()));}
            catch (UnauthorizedException error) {sendJson(ctx, 401, Map.of("message", error.getMessage()));}
            catch (DataAccessException error) {sendJson(ctx, 500, Map.of("message", "Error: " + error.getMessage()));}
        });

        // logout
        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                LogoutRequest request = new LogoutRequest(authToken);
                userService.logout(request);
                sendJson(ctx, 200, Map.of());}
            catch (UnauthorizedException error) {sendJson(ctx, 401, Map.of("message", error.getMessage()));}
            catch (DataAccessException error) {sendJson(ctx, 500, Map.of("message", "Error: " + error.getMessage()));}
        });

        // list games
        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                ListGamesRequest request = new ListGamesRequest(authToken);
                sendJson(ctx, 200, gameService.listGames(request));}
            catch (UnauthorizedException error) {sendJson(ctx, 401, Map.of("message", error.getMessage()));}
            catch (DataAccessException error) {sendJson(ctx, 500, Map.of("message", "Error: " + error.getMessage()));}
        });

        // create game
        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                CreateGameRequest bodyRequest = gson.fromJson(ctx.body(), CreateGameRequest.class);
                String gameName = null;
                if (bodyRequest != null) {
                    gameName = bodyRequest.gameName();
                }
                CreateGameRequest request = new CreateGameRequest(authToken, gameName);
                sendJson(ctx, 200, gameService.createGame(request));}
            catch (BadRequestException error) {sendJson(ctx, 400, Map.of("message", error.getMessage()));}
            catch (UnauthorizedException error) {sendJson(ctx, 401, Map.of("message", error.getMessage()));}
            catch (DataAccessException error) {sendJson(ctx, 500, Map.of("message", "Error: " + error.getMessage()));}
        });

        // join game
        javalin.put("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                JoinGameRequest bodyRequest = gson.fromJson(ctx.body(), JoinGameRequest.class);
                String playerColor = null;
                Integer gameID = null;
                if (bodyRequest != null) {
                    playerColor = bodyRequest.playerColor();
                    gameID = bodyRequest.gameID();
                }
                JoinGameRequest request = new JoinGameRequest(authToken, playerColor, gameID);
                gameService.joinGame(request);
                sendJson(ctx, 200, Map.of());}
            catch (BadRequestException error) {sendJson(ctx, 400, Map.of("message", error.getMessage()));}
            catch (UnauthorizedException error) {sendJson(ctx, 401, Map.of("message", error.getMessage()));}
            catch (AlreadyTakenException error) {sendJson(ctx, 403, Map.of("message", error.getMessage()));}
            catch (DataAccessException error) {sendJson(ctx, 500, Map.of("message", "Error: " + error.getMessage()));}
        });
    }

    // javalin configured object mapper
    private void sendJson(Context ctx, int statusCode, Object body) {
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
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