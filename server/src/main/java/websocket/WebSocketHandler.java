package websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class WebSocketHandler {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void onMessage(WsMessageContext ctx) {
        UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
        if (command.getCommandType() == UserGameCommand.CommandType.CONNECT) {
            connect(ctx, command);
        } else {
            connections.send(ctx, new ErrorMessage("Error: command not added yet"));
        }
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                connections.send(ctx, new ErrorMessage("Error: bad auth token"));
                return;
            }
            GameData game = gameDAO.getGame(command.getGameID());
            if (game == null) {
                connections.send(ctx, new ErrorMessage("Error: game not found"));
                return;
            }
            connections.add(game.gameID(), auth.username(), ctx);
            connections.send(ctx, new LoadGameMessage(game));
            String message;
            if (auth.username().equals(game.whiteUsername())) {
                message = auth.username() + " joined as white";
            } else if (auth.username().equals(game.blackUsername())) {
                message = auth.username() + " joined as black";
            } else {
                message = auth.username() + " joined as an observer";
            }
            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(message),
                    ctx
            );
        } catch (DataAccessException e) {
            connections.send(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }
}