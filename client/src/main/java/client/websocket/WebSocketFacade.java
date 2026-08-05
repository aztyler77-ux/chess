package client.websocket;

import chess.ChessMove;
import client.ResponseException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.websocket.CloseReason;
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

    private volatile Session session;
    private volatile boolean closingOnPurpose;
    private final NotificationHandler handler;
    private final Gson gson = new Gson();

    public WebSocketFacade(int port, NotificationHandler handler) throws ResponseException {
        this.handler = handler;

        try {
            String url = "ws://localhost:" + port + "/ws";

            session = ContainerProvider
                    .getWebSocketContainer()
                    .connectToServer(this, URI.create(url));

            if (session == null || !session.isOpen()) {
                throw new ResponseException("Couldn't connect to websocket");
            }

        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException("Couldn't connect to websocket");
        }
    }

    @Override
    public void onOpen(Session openedSession, EndpointConfig config) {
        session = openedSession;
        closingOnPurpose = false;

        openedSession.addMessageHandler(String.class, message -> {
            try {
                JsonObject json = gson.fromJson(message, JsonObject.class);

                if (json == null || !json.has("serverMessageType")) {
                    handler.notify(new ErrorMessage("Error: bad message from server"));
                    return;
                }

                String type = json.get("serverMessageType").getAsString();
                ServerMessage serverMessage;

                if (type.equals("LOAD_GAME")) {
                    serverMessage = gson.fromJson(message, LoadGameMessage.class);
                } else if (type.equals("ERROR")) {
                    serverMessage = gson.fromJson(message, ErrorMessage.class);
                } else if (type.equals("NOTIFICATION")) {
                    serverMessage = gson.fromJson(message, NotificationMessage.class);
                } else {
                    handler.notify(new ErrorMessage("Error: unknown message from server"));
                    return;
                }

                handler.notify(serverMessage);

            } catch (Exception e) {
                handler.notify(new ErrorMessage("Error: couldn't read websocket message"));
            }
        });
    }

    @Override
    public void onClose(Session closedSession, CloseReason reason) {
        if (session == closedSession) {
            session = null;
        }

        if (closingOnPurpose) {
            return;
        }

        String reasonText = "";

        if (reason != null && reason.getReasonPhrase() != null) {
            reasonText = reason.getReasonPhrase();
        }

        System.out.println();

        if (reasonText.isBlank()) {
            System.out.println("Websocket disconnected");
        } else {
            System.out.println("Websocket disconnected: " + reasonText);
        }
    }

    @Override
    public void onError(Session badSession, Throwable error) {
        if (closingOnPurpose) {
            return;
        }

        String message = "unknown websocket error";

        if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
            message = error.getMessage();
        }

        System.out.println();
        System.out.println("Websocket error: " + message);
    }

    public void connect(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameID
        );

        send(command);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        MakeMoveCommand command = new MakeMoveCommand(
                authToken,
                gameID,
                move
        );

        send(command);
    }

    public void leave(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.LEAVE,
                authToken,
                gameID
        );

        send(command);
    }

    public void resign(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.RESIGN,
                authToken,
                gameID
        );

        send(command);
    }

    private synchronized void send(Object command) throws ResponseException {
        Session currentSession = session;

        if (currentSession == null) {
            throw new ResponseException("Websocket is not connected");
        }

        if (!currentSession.isOpen()) {
            throw new ResponseException("Websocket connection closed");
        }

        try {
            String json = gson.toJson(command);
            currentSession.getBasicRemote().sendText(json);
        } catch (Exception e) {
            throw new ResponseException("Couldn't send websocket message");
        }
    }

    public void close() {
        closingOnPurpose = true;

        Session currentSession = session;
        session = null;

        try {
            if (currentSession != null && currentSession.isOpen()) {
                currentSession.close();
            }
        } catch (Exception e) {
            // we were closing it anyway so this does not really matter
        }
    }
}