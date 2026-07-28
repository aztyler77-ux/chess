package client;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import model.AuthData;
import model.UserData;
import java.util.Map;
import model.GameData;
import java.util.List;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        UserData user = new UserData(username, password, email);
        var request = buildRequest("POST", "/user", user, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder().uri(URI.create(serverUrl + path));
        if (authToken != null) {
            request.header("authorization", authToken);
        }

        if (body != null) {
            String json = new Gson().toJson(body);
            request.header("Content-Type", "application/json");
            request.method(method, HttpRequest.BodyPublishers.ofString(json));
        } else {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return request.build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception error) {
            throw new ResponseException("Couldn't reach the server");
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass)
            throws ResponseException {
        if (response.statusCode() / 100 != 2) {
            var error = new Gson().fromJson(response.body(), ErrorResponse.class);
            throw new ResponseException(error.message());
        }

        if (responseClass == null) {return null;}
        return new Gson().fromJson(response.body(), responseClass);
    }

    private record ErrorResponse(String message) {
    }

    public AuthData login(String username, String password) throws ResponseException {
        UserData user = new UserData(username, password, null);
        var request = buildRequest("POST", "/session", user, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var gameInfo = Map.of("gameName", gameName);
        var request = buildRequest("POST", "/game", gameInfo, authToken);
        var response = sendRequest(request);
        var result = handleResponse(response, GameID.class);
        return result.gameID();
    }

    private record GameID(int gameID) {}

    public List<GameData> listGames(String authToken) throws ResponseException {
        var request = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(request);
        var result = handleResponse(response, GameList.class);
        return result.games();
    }

    private record GameList(List<GameData> games) {}

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        var joinInfo = Map.of("playerColor", playerColor, "gameID", gameID);
        var request = buildRequest("PUT", "/game", joinInfo, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
    }
}