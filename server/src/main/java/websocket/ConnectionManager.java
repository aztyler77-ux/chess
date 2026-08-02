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
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new ConcurrentHashMap<>());
        }
        connections.get(gameID).put(ctx, username);
    }

    public String remove(int gameID, WsContext ctx) {
        if (!connections.containsKey(gameID)) {
            return null;
        }
        return connections.get(gameID).remove(ctx);
    }

    public void send(WsContext ctx, ServerMessage message) {
        ctx.send(gson.toJson(message));
    }

    public void broadcast(int gameID, ServerMessage message, WsContext skip) {
        if (!connections.containsKey(gameID)) {
            return;
        }
        for (WsContext ctx : connections.get(gameID).keySet()) {
            if (skip == null || ctx.session != skip.session) {
                send(ctx, message);
            }
        }
    }
}