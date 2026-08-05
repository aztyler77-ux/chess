package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Integer, Map<WsContext, String>> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(int gameID, String username, WsContext ctx) {
        remove(ctx);

        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new ConcurrentHashMap<>());
        }

        connections.get(gameID).put(ctx, username);
    }

    public boolean connected(int gameID, String username, WsContext ctx) {
        if (!connections.containsKey(gameID)) {
            return false;
        }

        for (Map.Entry<WsContext, String> entry : connections.get(gameID).entrySet()) {
            WsContext saved = entry.getKey();

            if (sameConnection(saved, ctx)
                    && username.equals(entry.getValue())) {
                return true;
            }
        }

        return false;
    }

    public String remove(int gameID, WsContext ctx) {
        if (ctx == null || !connections.containsKey(gameID)) {
            return null;
        }

        Map<WsContext, String> gameConnections = connections.get(gameID);

        for (WsContext saved : gameConnections.keySet()) {
            if (sameConnection(saved, ctx)) {
                String username = gameConnections.remove(saved);

                if (gameConnections.isEmpty()) {
                    connections.remove(gameID, gameConnections);
                }

                return username;
            }
        }

        return null;
    }

    public void remove(WsContext ctx) {
        if (ctx == null) {
            return;
        }

        for (Integer gameID : connections.keySet()) {
            remove(gameID, ctx);
        }
    }

    public void send(WsContext ctx, ServerMessage message) {
        if (ctx == null) {
            return;
        }

        try {
            if (ctx.session == null || !ctx.session.isOpen()) {
                remove(ctx);
                return;
            }

            ctx.send(gson.toJson(message));

        } catch (Exception e) {
            remove(ctx);
        }
    }

    public void broadcast(int gameID, ServerMessage message, WsContext skip) {
        if (!connections.containsKey(gameID)) {
            return;
        }

        Map<WsContext, String> gameConnections = connections.get(gameID);

        for (WsContext ctx : gameConnections.keySet()) {
            if (ctx.session == null || !ctx.session.isOpen()) {
                remove(gameID, ctx);
            } else if (skip == null || !sameConnection(ctx, skip)) {
                send(ctx, message);
            }
        }
    }

    private boolean sameConnection(WsContext first, WsContext second) {
        if (first == null || second == null) {
            return false;
        }

        return first.session == second.session;
    }
}