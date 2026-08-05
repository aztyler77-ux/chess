package server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DatabaseSetup;
import dataaccess.MySQLAuthDAO;
import dataaccess.MySQLGameDAO;
import dataaccess.MySQLUserDAO;
import dataaccess.UserDAO;
import service.ClearService;
import service.GameService;
import service.UserService;
import dataaccess.DataAccessException;

import java.time.Duration;
import java.util.Map;

import service.RegisterRequest;
import service.exception.BadRequestException;
import com.google.gson.Gson;
import service.exception.AlreadyTakenException;
import service.LoginRequest;
import service.exception.UnauthorizedException;
import service.LogoutRequest;
import service.ListGamesRequest;
import service.CreateGameRequest;
import service.JoinGameRequest;
import websocket.WebSocketHandler;

public class Server {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson;
    private final Javalin javalin;

    public Server() {
        try {
            DatabaseSetup.createTables();
        } catch (DataAccessException e) {
            throw new RuntimeException("Could not set up database", e);
        }

        userDAO = new MySQLUserDAO();
        authDAO = new MySQLAuthDAO();
        gameDAO = new MySQLGameDAO();

        WebSocketHandler webSocketHandler = new WebSocketHandler(
                authDAO,
                gameDAO
        );

        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        gson = new Gson();

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");

            config.jetty.modifyWebSocketServletFactory(factory ->
                    factory.setIdleTimeout(Duration.ofHours(1)));
        });

        javalin.ws("/ws", ws -> {
            ws.onMessage(webSocketHandler::onMessage);
            ws.onClose(webSocketHandler::onClose);
        });

        registerClearRoute();

        javalin.post("/user", ctx -> {
            try {
                RegisterRequest request = gson.fromJson(
                        ctx.body(),
                        RegisterRequest.class
                );

                sendJson(ctx, 200, userService.register(request));

            } catch (BadRequestException error) {
                sendJson(
                        ctx,
                        400,
                        Map.of("message", error.getMessage())
                );

            } catch (DataAccessException error) {
                sendJson(
                        ctx,
                        500,
                        Map.of("message", "Error: " + error.getMessage())
                );

            } catch (AlreadyTakenException error) {
                sendJson(
                        ctx,
                        403,
                        Map.of("message", error.getMessage())
                );
            }
        });

        javalin.post("/session", ctx -> {
            try {
                LoginRequest request = gson.fromJson(
                        ctx.body(),
                        LoginRequest.class
                );

                sendJson(ctx, 200, userService.login(request));

            } catch (BadRequestException error) {
                sendJson(
                        ctx,
                        400,
                        Map.of("message", error.getMessage())
                );

            } catch (UnauthorizedException error) {
                sendJson(
                        ctx,
                        401,
                        Map.of("message", error.getMessage())
                );

            } catch (DataAccessException error) {
                sendJson(
                        ctx,
                        500,
                        Map.of("message", "Error: " + error.getMessage())
                );
            }
        });

        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                LogoutRequest request = new LogoutRequest(authToken);

                userService.logout(request);
                sendJson(ctx, 200, Map.of());

            } catch (UnauthorizedException error) {
                sendJson(
                        ctx,
                        401,
                        Map.of("message", error.getMessage())
                );

            } catch (DataAccessException error) {
                sendJson(
                        ctx,
                        500,
                        Map.of("message", "Error: " + error.getMessage())
                );
            }
        });

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                ListGamesRequest request = new ListGamesRequest(authToken);

                sendJson(ctx, 200, gameService.listGames(request));

            } catch (UnauthorizedException error) {
                sendJson(
                        ctx,
                        401,
                        Map.of("message", error.getMessage())
                );

            } catch (DataAccessException error) {
                sendJson(
                        ctx,
                        500,
                        Map.of("message", "Error: " + error.getMessage())
                );
            }
        });

        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");

                CreateGameRequest bodyRequest = gson.fromJson(
                        ctx.body(),
                        CreateGameRequest.class
                );

                String gameName = null;

                if (bodyRequest != null) {
                    gameName = bodyRequest.gameName();
                }

                CreateGameRequest request = new CreateGameRequest(
                        authToken,
                        gameName
                );

                sendJson(ctx, 200, gameService.createGame(request));

            } catch (BadRequestException error) {
                sendJson(
                        ctx,
                        400,
                        Map.of("message", error.getMessage())
                );

            } catch (UnauthorizedException error) {
                sendJson(
                        ctx,
                        401,
                        Map.of("message", error.getMessage())
                );

            } catch (DataAccessException error) {
                sendJson(
                        ctx,
                        500,
                        Map.of("message", "Error: " + error.getMessage())
                );
            }
        });

        javalin.put("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");

                JoinGameRequest bodyRequest = gson.fromJson(
                        ctx.body(),
                        JoinGameRequest.class
                );

                String playerColor = null;
                Integer gameID = null;

                if (bodyRequest != null) {
                    playerColor = bodyRequest.playerColor();
                    gameID = bodyRequest.gameID();
                }

                JoinGameRequest request = new JoinGameRequest(
                        authToken,
                        playerColor,
                        gameID
                );

                gameService.joinGame(request);
                sendJson(ctx, 200, Map.of());

            } catch (BadRequestException error) {
                sendJson(
                        ctx,
                        400,
                        Map.of("message", error.getMessage())
                );

            } catch (UnauthorizedException error) {
                sendJson(
                        ctx,
                        401,
                        Map.of("message", error.getMessage())
                );

            } catch (AlreadyTakenException error) {
                sendJson(
                        ctx,
                        403,
                        Map.of("message", error.getMessage())
                );

            } catch (DataAccessException error) {
                sendJson(
                        ctx,
                        500,
                        Map.of("message", "Error: " + error.getMessage())
                );
            }
        });
    }

    private void registerClearRoute() {
        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                sendJson(ctx, 200, Map.of());

            } catch (DataAccessException error) {
                sendJson(
                        ctx,
                        500,
                        Map.of("message", "Error: " + error.getMessage())
                );
            }
        });
    }

    private void sendJson(Context ctx, int statusCode, Object body) {
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
    }

    public int run(int targetPort) {
        javalin.start(targetPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}