package client.websocket;
import chess.ChessMove;
import client.ResponseException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private final NotificationHandler handler;
    private final Gson gson = new Gson();

    public WebSocketFacade(int port, NotificationHandler handler) throws ResponseException {
        this.handler = handler;
        try {
            String url = "ws://localhost:" + port + "/ws";
            ContainerProvider.getWebSocketContainer().connectToServer(this, URI.create(url));
        } catch (Exception e) {
            throw new ResponseException("Couldn't connect to websocket");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(String.class, message -> {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("serverMessageType").getAsString();
            ServerMessage serverMessage;
            if (type.equals("LOAD_GAME")) {
                serverMessage = gson.fromJson(message, LoadGameMessage.class);
            } else if (type.equals("ERROR")) {
                serverMessage = gson.fromJson(message, ErrorMessage.class);
            } else {
                serverMessage = gson.fromJson(message, NotificationMessage.class);
            }
            handler.notify(serverMessage);
        });
    }

    public void connect(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        send(command);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        send(new MakeMoveCommand(authToken, gameID, move));
    }

    public void leave(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        send(command);
    }

    public void resign(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        send(command);
    }

    private void send(Object command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (Exception e) {
            throw new ResponseException("Couldn't send websocket message");
        }
    }
}